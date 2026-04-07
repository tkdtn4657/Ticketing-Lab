package com.ticketinglab.config.openapi;

public final class OpenApiExamples {

    public static final String AUTHORIZATION_HEADER = "Bearer eyJhbGciOiJIUzI1NiJ9.example-access-token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh-token=refresh-token-example; Path=/; Max-Age=1209600; HttpOnly; SameSite=Strict";
    public static final String CLEARED_REFRESH_TOKEN_COOKIE = "refresh-token=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict";
    public static final String IDEMPOTENCY_KEY = "0d5c5f6d-6a83-4de8-b4a8-c0ea2c941234";

    public static final String AUTH_SIGNUP_REQUEST = """
            {
              "email": "user@example.com",
              "password": "password123"
            }
            """;

    public static final String AUTH_SIGNUP_RESPONSE = """
            {
              "userId": 1
            }
            """;

    public static final String AUTH_LOGIN_REQUEST = """
            {
              "email": "user@example.com",
              "password": "password123"
            }
            """;

    public static final String AUTH_TOKEN_RESPONSE = """
            {
              "accessToken": "eyJhbGciOiJIUzI1NiJ9.example-access-token",
              "refreshToken": "refresh-token-example"
            }
            """;

    public static final String AUTH_REFRESH_REQUEST = """
            {
              "refreshToken": "refresh-token-example"
            }
            """;

    public static final String AUTH_ME_RESPONSE = """
            {
              "userId": 1,
              "email": "user@example.com",
              "role": "USER"
            }
            """;

    public static final String EVENT_LIST_RESPONSE = """
            {
              "events": [
                {
                  "eventId": 10,
                  "title": "Spring Festival",
                  "description": "Outdoors concert event",
                  "status": "PUBLISHED",
                  "createdAt": "2026-04-01T12:00:00"
                }
              ]
            }
            """;

    public static final String EVENT_DETAIL_RESPONSE = """
            {
              "event": {
                "eventId": 11,
                "title": "Indie Night",
                "description": "Live house performance",
                "status": "PUBLISHED",
                "createdAt": "2026-04-01T10:00:00"
              },
              "shows": [
                {
                  "showId": 101,
                  "startAt": "2026-04-01T19:00:00",
                  "status": "SCHEDULED",
                  "venueId": 101
                },
                {
                  "showId": 102,
                  "startAt": "2026-04-02T19:00:00",
                  "status": "SOLD_OUT",
                  "venueId": 101
                }
              ]
            }
            """;

    public static final String SHOW_AVAILABILITY_RESPONSE = """
            {
              "seats": [
                {
                  "seatId": 201,
                  "label": "A1",
                  "rowNo": 1,
                  "colNo": 1,
                  "price": 150000,
                  "available": true
                },
                {
                  "seatId": 202,
                  "label": "A2",
                  "rowNo": 1,
                  "colNo": 2,
                  "price": 150000,
                  "available": false
                }
              ],
              "sections": [
                {
                  "sectionId": 301,
                  "name": "R",
                  "price": 120000,
                  "remainingQty": 75
                }
              ]
            }
            """;

    public static final String HOLD_CREATE_REQUEST = """
            {
              "showId": 701,
              "items": [
                {
                  "seatId": 11
                },
                {
                  "sectionId": 21,
                  "qty": 3
                }
              ]
            }
            """;

    public static final String HOLD_CREATE_RESPONSE = """
            {
              "holdId": "06f0cd71-93bf-40b6-850e-e5593f8e7a44",
              "expiresAt": "2026-06-01T19:05:00"
            }
            """;

    public static final String HOLD_DETAIL_RESPONSE = """
            {
              "hold": {
                "holdId": "06f0cd71-93bf-40b6-850e-e5593f8e7a44",
                "showId": 701,
                "status": "ACTIVE",
                "expiresAt": "2026-06-01T19:05:00",
                "createdAt": "2026-06-01T19:00:00"
              },
              "items": [
                {
                  "type": "SEAT",
                  "seatId": 11,
                  "sectionId": null,
                  "qty": 1,
                  "unitPrice": 150000
                },
                {
                  "type": "SECTION",
                  "seatId": null,
                  "sectionId": 21,
                  "qty": 2,
                  "unitPrice": 120000
                }
              ]
            }
            """;

    public static final String RESERVATION_CREATE_REQUEST = """
            {
              "holdId": "06f0cd71-93bf-40b6-850e-e5593f8e7a44"
            }
            """;

    public static final String RESERVATION_CREATE_RESPONSE = """
            {
              "reservationId": "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4",
              "status": "PENDING_PAYMENT"
            }
            """;

    public static final String RESERVATION_DETAIL_RESPONSE = """
            {
              "reservation": {
                "reservationId": "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4",
                "showId": 801,
                "status": "PENDING_PAYMENT",
                "totalAmount": 390000,
                "expiresAt": "2026-07-01T19:15:00",
                "createdAt": "2026-07-01T19:00:00"
              },
              "items": [
                {
                  "type": "SEAT",
                  "seatId": 11,
                  "sectionId": null,
                  "qty": 1,
                  "unitPrice": 150000
                },
                {
                  "type": "SECTION",
                  "seatId": null,
                  "sectionId": 21,
                  "qty": 2,
                  "unitPrice": 120000
                }
              ]
            }
            """;

    public static final String RESERVATION_LIST_RESPONSE = """
            {
              "page": 0,
              "size": 10,
              "totalElements": 1,
              "totalPages": 1,
              "reservations": [
                {
                  "reservationId": "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4",
                  "showId": 801,
                  "status": "EXPIRED",
                  "totalAmount": 230000,
                  "expiresAt": "2026-07-01T18:59:00",
                  "createdAt": "2026-07-01T18:44:00"
                }
              ]
            }
            """;

    public static final String PAYMENT_CONFIRM_REQUEST = """
            {
              "reservationId": "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4",
              "amount": 390000
            }
            """;

    public static final String PAYMENT_CONFIRM_RESPONSE = """
            {
              "paymentId": 1,
              "reservationId": "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4",
              "status": "APPROVED",
              "reservationStatus": "PAID",
              "approvedAt": "2026-07-01T19:02:30"
            }
            """;

    public static final String TICKET_LIST_RESPONSE = """
            {
              "page": 0,
              "size": 10,
              "totalElements": 3,
              "totalPages": 1,
              "tickets": [
                {
                  "ticketId": "TICKET-001",
                  "reservationId": "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4",
                  "showId": 901,
                  "reservationItemId": 501,
                  "type": "SEAT",
                  "seatId": 11,
                  "sectionId": null,
                  "serial": "TK-20260701-0001",
                  "qrToken": "qr-token-001",
                  "status": "ISSUED",
                  "usedAt": null,
                  "createdAt": "2026-07-01T19:02:31"
                }
              ]
            }
            """;

    public static final String CHECKIN_REQUEST = """
            {
              "qrToken": "qr-token-001"
            }
            """;

    public static final String CHECKIN_RESPONSE = """
            {
              "ticketId": "TICKET-001",
              "reservationId": "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4",
              "showId": 1301,
              "reservationItemId": 501,
              "type": "SEAT",
              "seatId": 11,
              "sectionId": null,
              "serial": "TK-20260815-0001",
              "qrToken": "qr-token-001",
              "status": "USED",
              "usedAt": "2026-08-15T18:40:00"
            }
            """;

    public static final String ADMIN_VENUE_UPSERT_REQUEST = """
            {
              "code": "SEOUL-HALL",
              "name": "Seoul Hall",
              "address": "Gangnam"
            }
            """;

    public static final String ADMIN_VENUE_UPSERT_RESPONSE = """
            {
              "venueId": 1
            }
            """;

    public static final String ADMIN_REGISTER_SEATS_REQUEST = """
            {
              "seats": [
                {
                  "label": "A1",
                  "rowNo": 1,
                  "colNo": 1
                },
                {
                  "label": "A2",
                  "rowNo": 1,
                  "colNo": 2
                }
              ]
            }
            """;

    public static final String ADMIN_VENUE_SEATS_RESPONSE = """
            {
              "seats": [
                {
                  "seatId": 11,
                  "label": "A1",
                  "rowNo": 1,
                  "colNo": 1
                },
                {
                  "seatId": 12,
                  "label": "A2",
                  "rowNo": 1,
                  "colNo": 2
                }
              ]
            }
            """;

    public static final String ADMIN_REGISTER_SECTIONS_REQUEST = """
            {
              "sections": [
                {
                  "name": "R"
                },
                {
                  "name": "S"
                }
              ]
            }
            """;

    public static final String ADMIN_VENUE_SECTIONS_RESPONSE = """
            {
              "sections": [
                {
                  "sectionId": 21,
                  "name": "R"
                },
                {
                  "sectionId": 22,
                  "name": "S"
                }
              ]
            }
            """;

    public static final String ADMIN_CREATE_EVENT_REQUEST = """
            {
              "title": "Jazz Night",
              "desc": "Late night live",
              "status": "PUBLISHED"
            }
            """;

    public static final String ADMIN_CREATE_EVENT_RESPONSE = """
            {
              "eventId": 100
            }
            """;

    public static final String ADMIN_CREATE_SHOW_REQUEST = """
            {
              "eventId": 100,
              "venueId": 1,
              "startAt": "2026-04-20T19:30:00"
            }
            """;

    public static final String ADMIN_CREATE_SHOW_RESPONSE = """
            {
              "showId": 200
            }
            """;

    public static final String ADMIN_CREATED_COUNT_RESPONSE = """
            {
              "createdCount": 2
            }
            """;

    public static final String ADMIN_CREATE_SHOW_SEATS_REQUEST = """
            {
              "items": [
                {
                  "seatId": 11,
                  "price": 150000
                }
              ]
            }
            """;

    public static final String ADMIN_CREATE_SECTION_INVENTORIES_REQUEST = """
            {
              "items": [
                {
                  "sectionId": 21,
                  "price": 120000,
                  "capacity": 100
                }
              ]
            }
            """;

    private OpenApiExamples() {
    }
}