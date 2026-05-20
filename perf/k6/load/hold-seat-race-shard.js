import http from "k6/http";
import { check, fail } from "k6";
import { Counter } from "k6/metrics";
import { sleep } from "k6";
import {
  postJson,
  signupAndLoginMany,
  uniqueSuffix,
} from "../lib/api.js";
import { reportSummary } from "../lib/summary.js";

const RACE_USERS = Number(__ENV.RACE_USERS || 10);
const MAX_DURATION = __ENV.MAX_DURATION || "5m";
const SETUP_TIMEOUT = __ENV.SETUP_TIMEOUT || "30m";
const SETUP_BATCH_SIZE = positiveNumber(__ENV.SETUP_BATCH_SIZE, 100);
const START_AT_EPOCH_MS = Number(__ENV.START_AT_EPOCH_MS || 0);
const SHOW_ID = Number(__ENV.SHOW_ID || 0);
const SEAT_ID = Number(__ENV.SEAT_ID || 0);
const SHARD_INDEX = __ENV.SHARD_INDEX || "0";
const USER_PASSWORD = __ENV.USER_PASSWORD || "perf1234";
const REPORT_NAME = __ENV.REPORT_NAME || `hold-seat-race-shard-${SHARD_INDEX}`;

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
    hold_unexpected: ["count==0"],
    "http_req_failed{scenario:hold_same_seat_race}": ["rate==0"],
    "checks{scenario:hold_same_seat_race}": ["rate>0.95"],
  },
};

const holdSuccess = new Counter("hold_success");
const holdConflict = new Counter("hold_conflict");
const holdFastFail = new Counter("hold_fast_fail");
const holdRejected = new Counter("hold_rejected");
const holdUnexpected = new Counter("hold_unexpected");

export function setup() {
  if (!Number.isFinite(SHOW_ID) || SHOW_ID <= 0) {
    fail("SHOW_ID is required for sharded hold race");
  }
  if (!Number.isFinite(SEAT_ID) || SEAT_ID <= 0) {
    fail("SEAT_ID is required for sharded hold race");
  }

  const userPrefix = __ENV.USER_PREFIX || uniqueSuffix(`race-shard-${SHARD_INDEX}`);
  const emails = Array.from(
    { length: RACE_USERS },
    (_, index) => `${userPrefix}-${index + 1}@example.com`,
  );
  const tokens = signupAndLoginMany(emails, USER_PASSWORD, SETUP_BATCH_SIZE)
    .map((tokenPair) => tokenPair.accessToken);

  return {
    showId: SHOW_ID,
    seatId: SEAT_ID,
    tokens,
    startAtEpochMs: START_AT_EPOCH_MS,
  };
}

export default function (data) {
  waitUntil(data.startAtEpochMs);

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
  return reportSummary(data, REPORT_NAME);
}

function waitUntil(startAtEpochMs) {
  if (!Number.isFinite(startAtEpochMs) || startAtEpochMs <= 0) {
    return;
  }

  let remainingSeconds = (startAtEpochMs - Date.now()) / 1000;
  while (remainingSeconds > 0) {
    sleep(Math.min(remainingSeconds, 1));
    remainingSeconds = (startAtEpochMs - Date.now()) / 1000;
  }
}

function positiveNumber(value, defaultValue) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : defaultValue;
}
