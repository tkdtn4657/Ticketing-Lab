import http from "k6/http";
import { check } from "k6";
import { Counter } from "k6/metrics";
import {
  BASE_URL,
  createAdminFixture,
  headers,
  login,
  parseJson,
} from "../lib/api.js";
import { reportSummary } from "../lib/summary.js";

const VUS = Number(__ENV.VUS || 1000);
const SEAT_COUNT = Number(__ENV.SEAT_COUNT || 1000);
const MAX_DURATION = __ENV.MAX_DURATION || "1m";
const SHOW_ID = __ENV.SHOW_ID;
const VERIFY_BODY = (__ENV.VERIFY_BODY || "false").toLowerCase() === "true";

export const options = {
  summaryTrendStats: ["min", "avg", "med", "p(90)", "p(95)", "p(99)", "max"],
  scenarios: {
    show_availability_burst: {
      executor: "per-vu-iterations",
      vus: VUS,
      iterations: 1,
      maxDuration: MAX_DURATION,
    },
  },
  thresholds: {
    "http_req_failed{scenario:show_availability_burst}": ["rate<0.05"],
    "http_req_duration{scenario:show_availability_burst}": ["p(95)<1000", "p(99)<3000"],
    "checks{scenario:show_availability_burst}": ["rate>0.95"],
    availability_burst_ok: [`count>=${Math.floor(VUS * 0.95)}`],
  },
};

const availabilityBurstOk = new Counter("availability_burst_ok");

export function setup() {
  if (SHOW_ID) {
    return { showId: SHOW_ID };
  }

  const adminEmail = __ENV.ADMIN_EMAIL || "admin@example.com";
  const adminPassword = __ENV.ADMIN_PASSWORD || "admin1234";
  const adminToken = login(adminEmail, adminPassword).accessToken;
  const fixture = createAdminFixture({
    adminToken,
    prefix: "availability-burst",
    seatCount: SEAT_COUNT,
    price: 10000,
  });

  return { showId: fixture.showId };
}

export default function (data) {
  const response = http.get(`${BASE_URL}/api/shows/${data.showId}/availability`, {
    headers: headers(),
    tags: {
      name: "/api/shows/{showId}/availability",
    },
  });

  const checks = {
    "burst availability status is 200": (res) => res.status === 200,
  };

  if (VERIFY_BODY) {
    checks["burst availability has seats"] = (res) => {
      const body = parseJson(res, "burst availability");
      return Array.isArray(body.seats) && body.seats.length > 0;
    };
  }

  if (check(response, checks)) {
    availabilityBurstOk.add(1);
  }
}

export function handleSummary(data) {
  return reportSummary(data, "show-availability-burst");
}
