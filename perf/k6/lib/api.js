import http from "k6/http";
import { check, fail } from "k6";

export const BASE_URL = __ENV.BASE_URL || "http://127.0.0.1:18080";

export function uniqueSuffix(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1_000_000)}`;
}

export function headers(token = null, extra = {}) {
  const result = {
    "Content-Type": "application/json",
    Accept: "application/json",
    ...extra,
  };

  if (token) {
    result.Authorization = `Bearer ${token}`;
  }

  return result;
}

export function getJson(path, token = null) {
  return http.get(`${BASE_URL}${path}`, {
    headers: headers(token),
  });
}

export function postJson(path, body, token = null, extraHeaders = {}) {
  return http.post(`${BASE_URL}${path}`, JSON.stringify(body), {
    headers: headers(token, extraHeaders),
  });
}

export function deleteJson(path, token = null) {
  return http.del(`${BASE_URL}${path}`, null, {
    headers: headers(token),
  });
}

export function parseJson(response, context) {
  try {
    return response.json();
  } catch (error) {
    fail(`${context} JSON parse failed: status=${response.status}, body=${response.body}`);
  }
}

export function ensureStatus(response, expected, context) {
  const ok = check(response, {
    [`${context} status ${expected}`]: (res) => res.status === expected,
  });

  if (!ok) {
    fail(`${context} failed: expected=${expected}, actual=${response.status}, body=${response.body}`);
  }
}

export function login(email, password) {
  const response = postJson("/api/auth/login", { email, password });
  ensureStatus(response, 200, `login ${email}`);

  return parseJson(response, `login ${email}`);
}

export function signupIfNeeded(email, password) {
  const response = postJson("/api/auth/signup", { email, password });

  if (response.status !== 200 && response.status !== 409) {
    fail(`signup ${email} failed: status=${response.status}, body=${response.body}`);
  }
}

export function signupAndLogin(email, password) {
  signupIfNeeded(email, password);
  return login(email, password);
}

export function signupManyIfNeeded(emails, password, batchSize = emails.length) {
  for (const emailChunk of chunks(emails, batchSize)) {
    const responses = http.batch(
      emailChunk.map((email) => ({
        method: "POST",
        url: `${BASE_URL}/api/auth/signup`,
        body: JSON.stringify({ email, password }),
        params: { headers: headers() },
      })),
    );

    responses.forEach((response, index) => {
      if (response.status !== 200 && response.status !== 409) {
        const email = emailChunk[index];
        fail(`signup ${email} failed: status=${response.status}, body=${response.body}`);
      }
    });
  }
}

export function loginMany(emails, password, batchSize = emails.length) {
  const tokenPairs = [];

  for (const emailChunk of chunks(emails, batchSize)) {
    const responses = http.batch(
      emailChunk.map((email) => ({
        method: "POST",
        url: `${BASE_URL}/api/auth/login`,
        body: JSON.stringify({ email, password }),
        params: { headers: headers() },
      })),
    );

    responses.forEach((response, index) => {
      const email = emailChunk[index];
      ensureStatus(response, 200, `login ${email}`);
      tokenPairs.push(parseJson(response, `login ${email}`));
    });
  }

  return tokenPairs;
}

export function signupAndLoginMany(emails, password, batchSize = emails.length) {
  signupManyIfNeeded(emails, password, batchSize);
  return loginMany(emails, password, batchSize);
}

export function createAdminFixture({
  adminToken,
  prefix = "perf",
  seatCount = 1,
  price = 10000,
}) {
  const suffix = uniqueSuffix(prefix);
  const venueCode = `VENUE-${suffix}`;

  const venueResponse = postJson(
    "/api/admin/venues/upsert",
    {
      code: venueCode,
      name: `성능 테스트 공연장 ${suffix}`,
      address: "서울시 테스트구",
    },
    adminToken,
  );
  ensureStatus(venueResponse, 200, "create venue");
  const venueId = parseJson(venueResponse, "create venue").venueId;

  const sectionResponse = postJson(
    `/api/admin/venues/${venueId}/sections`,
    {
      sections: [{ name: "A구역" }],
    },
    adminToken,
  );
  ensureStatus(sectionResponse, 200, "create section");

  const sectionsResponse = getJson(`/api/admin/venues/${venueId}/sections`, adminToken);
  ensureStatus(sectionsResponse, 200, "list sections");
  const sectionId = parseJson(sectionsResponse, "list sections").sections[0].sectionId;

  const seats = Array.from({ length: seatCount }, (_, index) => ({
    label: `A${index + 1}`,
    rowNo: 1,
    colNo: index + 1,
    sectionId,
  }));

  const seatResponse = postJson(
    `/api/admin/venues/${venueId}/seats`,
    { seats },
    adminToken,
  );
  ensureStatus(seatResponse, 200, "create seats");

  const seatsResponse = getJson(`/api/admin/venues/${venueId}/seats`, adminToken);
  ensureStatus(seatsResponse, 200, "list seats");
  const seatIds = parseJson(seatsResponse, "list seats").seats
    .filter((seat) => seat.label.startsWith("A"))
    .map((seat) => seat.seatId)
    .slice(0, seatCount);

  const eventResponse = postJson(
    "/api/admin/events",
    {
      title: `성능 테스트 이벤트 ${suffix}`,
      desc: "k6 테스트용 이벤트",
      status: "PUBLISHED",
    },
    adminToken,
  );
  ensureStatus(eventResponse, 200, "create event");
  const eventId = parseJson(eventResponse, "create event").eventId;

  const showResponse = postJson(
    "/api/admin/shows",
    {
      eventId,
      venueId,
      startAt: "2026-12-31T19:30:00",
    },
    adminToken,
  );
  ensureStatus(showResponse, 200, "create show");
  const showId = parseJson(showResponse, "create show").showId;

  const showSeatsResponse = postJson(
    `/api/admin/shows/${showId}/show-seats`,
    {
      items: seatIds.map((seatId) => ({ seatId, price })),
    },
    adminToken,
  );
  ensureStatus(showSeatsResponse, 200, "create show seats");

  return {
    suffix,
    venueId,
    sectionId,
    eventId,
    showId,
    seatIds,
    price,
  };
}

function chunks(items, size) {
  const chunkSize = Math.max(1, Math.floor(size || items.length || 1));
  const result = [];

  for (let start = 0; start < items.length; start += chunkSize) {
    result.push(items.slice(start, start + chunkSize));
  }

  return result;
}
