import { check, sleep } from "k6";
import { Counter } from "k6/metrics";
import { createAdminFixture, getJson, login, parseJson } from "../lib/api.js";
import { reportSummary } from "../lib/summary.js";

const VUS = Number(__ENV.VUS || 20);
const DURATION = __ENV.DURATION || "30s";
const SEAT_COUNT = Number(__ENV.SEAT_COUNT || 20);
const SHOW_ID = __ENV.SHOW_ID;

export const options = {
  summaryTrendStats: ["min", "avg", "med", "p(90)", "p(95)", "p(99)", "max"],
  scenarios: {
    show_availability_load: {
      executor: "constant-vus",
      vus: VUS,
      duration: DURATION,
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500", "p(99)<1000"],
    checks: ["rate>0.99"],
  },
};

const availabilityOk = new Counter("availability_ok");

export function setup() {
  if (SHOW_ID) {
    return { showId: SHOW_ID };
  }

  const adminEmail = __ENV.ADMIN_EMAIL || "admin@example.com";
  const adminPassword = __ENV.ADMIN_PASSWORD || "admin1234";
  const adminToken = login(adminEmail, adminPassword).accessToken;
  const fixture = createAdminFixture({
    adminToken,
    prefix: "availability",
    seatCount: SEAT_COUNT,
    price: 10000,
  });

  return { showId: fixture.showId };
}

export default function (data) {
  const response = getJson(`/api/shows/${data.showId}/availability`);
  const ok = check(response, {
    "availability status is 200": (res) => res.status === 200,
    "availability has seats": (res) => {
      const body = parseJson(res, "availability");
      return Array.isArray(body.seats) && body.seats.length > 0;
    },
  });

  if (ok) {
    availabilityOk.add(1);
  }

  sleep(0.2);
}

export function handleSummary(data) {
  return reportSummary(data, "show-availability");
}
