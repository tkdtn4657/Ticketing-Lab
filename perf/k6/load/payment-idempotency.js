import { check } from "k6";
import { Counter } from "k6/metrics";
import {
  createAdminFixture,
  getJson,
  login,
  parseJson,
  postJson,
  signupAndLogin,
  uniqueSuffix,
} from "../lib/api.js";
import { reportSummary } from "../lib/summary.js";

const PAYMENT_REQUESTS = Number(__ENV.PAYMENT_REQUESTS || 10);

export const options = {
  summaryTrendStats: ["min", "avg", "med", "p(90)", "p(95)", "p(99)", "max"],
  scenarios: {
    payment_same_idempotency_key: {
      executor: "shared-iterations",
      vus: PAYMENT_REQUESTS,
      iterations: PAYMENT_REQUESTS,
      maxDuration: "30s",
    },
  },
  thresholds: {
    payment_approved: [`count==${PAYMENT_REQUESTS}`],
    ticket_count_ok: ["count==1"],
    checks: ["rate>0.95"],
  },
};

const paymentApproved = new Counter("payment_approved");
const paymentUnexpected = new Counter("payment_unexpected");
const ticketCountOk = new Counter("ticket_count_ok");

export function setup() {
  const adminEmail = __ENV.ADMIN_EMAIL || "admin@example.com";
  const adminPassword = __ENV.ADMIN_PASSWORD || "admin1234";
  const userPassword = __ENV.USER_PASSWORD || "perf1234";

  const adminToken = login(adminEmail, adminPassword).accessToken;
  const fixture = createAdminFixture({
    adminToken,
    prefix: "payment-idempotency",
    seatCount: 1,
    price: 10000,
  });

  const userEmail = `${uniqueSuffix("payment-user")}@example.com`;
  const userToken = signupAndLogin(userEmail, userPassword).accessToken;

  const holdResponse = postJson(
    "/api/holds",
    {
      showId: fixture.showId,
      items: [{ seatId: fixture.seatIds[0] }],
    },
    userToken,
  );
  check(holdResponse, {
    "setup hold created": (res) => res.status === 200,
  });
  const hold = parseJson(holdResponse, "setup hold");

  const reservationResponse = postJson(
    "/api/reservations",
    { holdId: hold.holdId },
    userToken,
  );
  check(reservationResponse, {
    "setup reservation created": (res) => res.status === 200,
  });
  const reservation = parseJson(reservationResponse, "setup reservation");

  return {
    token: userToken,
    reservationId: reservation.reservationId,
    amount: fixture.price,
    idempotencyKey: uniqueSuffix("payment-key"),
  };
}

export default function (data) {
  const response = postJson(
    "/api/payments/confirm",
    {
      reservationId: data.reservationId,
      amount: data.amount,
    },
    data.token,
    {
      "Idempotency-Key": data.idempotencyKey,
    },
  );

  const ok = check(response, {
    "payment confirm status is 200": (res) => res.status === 200,
    "payment is approved": (res) => parseJson(res, "payment confirm").status === "APPROVED",
  });

  if (ok) {
    paymentApproved.add(1);
  } else {
    paymentUnexpected.add(1);
  }
}

export function teardown(data) {
  const response = getJson("/api/me/tickets?page=0&size=20", data.token);
  const body = parseJson(response, "list tickets");
  const issuedTickets = body.tickets.filter((ticket) => ticket.reservationId === data.reservationId);

  if (issuedTickets.length === 1) {
    ticketCountOk.add(1);
  }

  check(response, {
    "only one ticket issued for idempotent payment": () => issuedTickets.length === 1,
  });
}

export function handleSummary(data) {
  return reportSummary(data, "payment-idempotency");
}
