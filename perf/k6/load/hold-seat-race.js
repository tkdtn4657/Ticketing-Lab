import http from "k6/http";
import { check } from "k6";
import { Counter } from "k6/metrics";
import { sleep } from "k6";
import {
  createAdminFixture,
  login,
  postJson,
  signupAndLoginMany,
  uniqueSuffix,
} from "../lib/api.js";
import { reportSummary } from "../lib/summary.js";

const RACE_USERS = Number(__ENV.RACE_USERS || 10);
const MAX_DURATION = __ENV.MAX_DURATION || "30s";
const SETUP_TIMEOUT = __ENV.SETUP_TIMEOUT || "10m";
const SETUP_BATCH_SIZE = positiveNumber(__ENV.SETUP_BATCH_SIZE, 50);
const SETUP_SETTLE_SECONDS = positiveNumber(__ENV.SETUP_SETTLE_SECONDS, 0);

http.setResponseCallback(http.expectedStatuses(200, 409, 429));

export const options = {
  batch: SETUP_BATCH_SIZE,
  batchPerHost: SETUP_BATCH_SIZE,
  summaryTrendStats: ["min", "avg", "med", "p(90)", "p(95)", "p(99)", "max"],
  setupTimeout: SETUP_TIMEOUT,
  scenarios: {
    hold_same_seat_race: {
      executor: "per-vu-iterations",
      vus: RACE_USERS,
      iterations: 1,
      maxDuration: MAX_DURATION,
    },
  },
  thresholds: {
    hold_success: ["count==1"],
    hold_rejected: [`count>=${RACE_USERS - 1}`],
    hold_unexpected: ["count==0"],
    "http_req_failed{scenario:hold_same_seat_race}": ["rate==0"],
    "http_req_duration{scenario:hold_same_seat_race}": ["p(95)<3000", "p(99)<10000"],
    "checks{scenario:hold_same_seat_race}": ["rate>0.95"],
  },
};

const holdSuccess = new Counter("hold_success");
const holdConflict = new Counter("hold_conflict");
const holdFastFail = new Counter("hold_fast_fail");
const holdRejected = new Counter("hold_rejected");
const holdUnexpected = new Counter("hold_unexpected");

export function setup() {
  const adminEmail = __ENV.ADMIN_EMAIL || "admin@example.com";
  const adminPassword = __ENV.ADMIN_PASSWORD || "admin1234";
  const userPassword = __ENV.USER_PASSWORD || "perf1234";

  const adminToken = login(adminEmail, adminPassword).accessToken;
  const fixture = createAdminFixture({
    adminToken,
    prefix: "hold-race",
    seatCount: 1,
    price: 10000,
  });

  const userPrefix = uniqueSuffix("race-user");
  const emails = Array.from(
    { length: RACE_USERS },
    (_, index) => `${userPrefix}-${index + 1}@example.com`,
  );
  const tokens = signupAndLoginMany(emails, userPassword, SETUP_BATCH_SIZE)
    .map((tokenPair) => tokenPair.accessToken);

  if (SETUP_SETTLE_SECONDS > 0) {
    sleep(SETUP_SETTLE_SECONDS);
  }

  return {
    showId: fixture.showId,
    seatId: fixture.seatIds[0],
    tokens,
  };
}

export default function (data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];
  const response = postJson(
    "/api/holds",
    {
      showId: data.showId,
      items: [{ seatId: data.seatId }],
    },
    token,
  );

  if (response.status === 200) {
    holdSuccess.add(1);
  } else if (response.status === 409) {
    holdConflict.add(1);
    holdRejected.add(1);
  } else if (response.status === 429) {
    holdFastFail.add(1);
    holdRejected.add(1);
  } else {
    holdUnexpected.add(1);
    console.warn(`unexpected hold response: status=${response.status}, body=${response.body}`);
  }

  check(response, {
    "hold race returns 200, 409, or 429": (res) => [200, 409, 429].includes(res.status),
  });
}

export function handleSummary(data) {
  return reportSummary(data, "hold-seat-race");
}

function positiveNumber(value, defaultValue) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : defaultValue;
}
