CREATE TABLE `shows` (
                         `id`	BIGINT	NOT NULL,
                         `start_at`	DATETIME	NOT NULL,
                         `status`	VARCHAR(20)	NOT NULL	DEFAULT 'SCHEDULED',
                         `created_at`	DATETIME	NOT NULL,
                         `event_id`	BIGINT	NOT NULL,
                         `venue_id`	BIGINT	NOT NULL
);

CREATE TABLE `events` (
                          `id`	BIGINT	NOT NULL,
                          `title`	VARCHAR(200)	NOT NULL,
                          `description`	TEXT	NULL,
                          `status`	VARCHAR(20)	NOT NULL	DEFAULT 'DRAFT',
                          `created_at`	DATETIME	NOT NULL
);

CREATE TABLE `reservations` (
                                `id`	CHAR(36)	NOT NULL,
                                `status`	VARCHAR(30)	NOT NULL	DEFAULT 'PENDING_PAYMENT',
                                `total_amount`	INT	NOT NULL,
                                `expires_at`	DATETIME	NULL,
                                `created_at`	DATETIME	NOT NULL,
                                `user_id`	BIGINT	NOT NULL,
                                `show_id`	BIGINT	NOT NULL
);

CREATE TABLE `hold_items` (
                              `id`	BIGINT	NOT NULL,
                              `type`	VARCHAR(20)	NOT NULL,
                              `seat_id`	BIGINT	NULL,
                              `section_id`	BIGINT	NULL,
                              `qty`	INT	NOT NULL,
                              `unit_price`	INT	NOT NULL,
                              `hold_id`	CHAR(36)	NOT NULL
);

CREATE TABLE `show_section_inventories` (
                                            `show_id`	BIGINT	NOT NULL,
                                            `section_id`	BIGINT	NOT NULL,
                                            `price`	INT	NOT NULL,
                                            `capacity`	INT	NOT NULL,
                                            `sold_qty`	INT	NOT NULL,
                                            `hold_qty`	INT	NOT NULL,
                                            `version`	INT	NOT NULL
);

CREATE TABLE `sections` (
                            `id`	BIGINT	NOT NULL,
                            `name`	VARCHAR(50)	NOT NULL,
                            `created_at`	DATETIME	NOT NULL,
                            `venue_id`	BIGINT	NOT NULL
);

CREATE TABLE `users` (
                         `id`	BIGINT	NOT NULL,
                         `email`	VARCHAR(255)	NOT NULL,
                         `password_hash`	VARCHAR(255)	NOT NULL,
                         `role`	VARCHAR(20)	NOT NULL	DEFAULT 'USER',
                         `created_at`	DATETIME	NOT NULL
);

CREATE TABLE `seats` (
                         `id`	BIGINT	NOT NULL,
                         `label`	VARCHAR(50)	NOT NULL,
                         `row_no`	INT	NULL,
                         `col_no`	INT	NULL,
                         `created_at`	DATETIME	NOT NULL,
                         `venue_id`	BIGINT	NOT NULL
);

CREATE TABLE `holds` (
                         `id`	CHAR(36)	NOT NULL,
                         `status`	VARCHAR(20)	NOT NULL	DEFAULT 'ACTIVE',
                         `expires_at`	DATETIME	NOT NULL,
                         `created_at`	DATETIME	NOT NULL,
                         `user_id`	BIGINT	NOT NULL,
                         `show_id`	BIGINT	NOT NULL
);

CREATE TABLE `reservation_items` (
                                     `id`	BIGINT	NOT NULL,
                                     `type`	VARCHAR(20)	NOT NULL,
                                     `seat_id`	BIGINT	NULL,
                                     `section_id`	BIGINT	NULL,
                                     `qty`	INT	NOT NULL,
                                     `unit_price`	INT	NOT NULL,
                                     `reservation_id`	CHAR(36)	NOT NULL
);

CREATE TABLE `venues` (
                          `id`	BIGINT	NOT NULL,
                          `code`	VARCHAR(50)	NOT NULL,
                          `name`	VARCHAR(200)	NOT NULL,
                          `address`	VARCHAR(300)	NULL,
                          `created_at`	DATETIME	NOT NULL,
                          `updated_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `show_seats` (
                              `seat_id`	BIGINT	NOT NULL,
                              `show_id`	BIGINT	NOT NULL,
                              `price`	INT	NOT NULL,
                              `status`	VARCHAR(20)	NOT NULL	DEFAULT 'AVAILABLE',
                              `version`	INT	NOT NULL
);

CREATE TABLE `payments` (
                            `id`	BIGINT	NOT NULL,
                            `provider`	VARCHAR(30)	NOT NULL	DEFAULT 'SIMULATOR',
                            `idempotency_key`	CHAR(36)	NOT NULL,
                            `status`	VARCHAR(20)	NOT NULL	DEFAULT 'REQUESTED',
                            `amount`	INT	NOT NULL,
                            `approved_at`	DATETIME	NULL,
                            `raw_payload`	JSON	NULL,
                            `created_at`	DATETIME	NOT NULL,
                            `reservation_id`	CHAR(36)	NOT NULL
);

CREATE TABLE `tickets` (
                           `id`	CHAR(36)	NOT NULL,
                           `serial`	VARCHAR(40)	NOT NULL,
                           `qr_token`	VARCHAR(80)	NOT NULL,
                           `status`	VARCHAR(20)	NOT NULL	DEFAULT 'ISSUED',
                           `used_at`	DATETIME	NULL,
                           `created_at`	DATETIME	NOT NULL,
                           `reservation_item_id`	BIGINT	NOT NULL
);

ALTER TABLE `shows` ADD CONSTRAINT `PK_SHOWS` PRIMARY KEY (
                                                           `id`
    );

ALTER TABLE `events` ADD CONSTRAINT `PK_EVENTS` PRIMARY KEY (
                                                             `id`
    );

ALTER TABLE `reservations` ADD CONSTRAINT `PK_RESERVATIONS` PRIMARY KEY (
                                                                         `id`
    );

ALTER TABLE `hold_items` ADD CONSTRAINT `PK_HOLD_ITEMS` PRIMARY KEY (
                                                                     `id`
    );

ALTER TABLE `show_section_inventories` ADD CONSTRAINT `PK_SHOW_SECTION_INVENTORIES` PRIMARY KEY (
                                                                                                 `show_id`,
                                                                                                 `section_id`
    );

ALTER TABLE `sections` ADD CONSTRAINT `PK_SECTIONS` PRIMARY KEY (
                                                                 `id`
    );

ALTER TABLE `users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (
                                                           `id`
    );

ALTER TABLE `seats` ADD CONSTRAINT `PK_SEATS` PRIMARY KEY (
                                                           `id`
    );

ALTER TABLE `holds` ADD CONSTRAINT `PK_HOLDS` PRIMARY KEY (
                                                           `id`
    );

ALTER TABLE `reservation_items` ADD CONSTRAINT `PK_RESERVATION_ITEMS` PRIMARY KEY (
                                                                                   `id`
    );

ALTER TABLE `venues` ADD CONSTRAINT `PK_VENUES` PRIMARY KEY (
                                                             `id`
    );

ALTER TABLE `show_seats` ADD CONSTRAINT `PK_SHOW_SEATS` PRIMARY KEY (
                                                                     `seat_id`,
                                                                     `show_id`
    );

ALTER TABLE `payments` ADD CONSTRAINT `PK_PAYMENTS` PRIMARY KEY (
                                                                 `id`
    );

ALTER TABLE `tickets` ADD CONSTRAINT `PK_TICKETS` PRIMARY KEY (
                                                               `id`
    );

ALTER TABLE `show_seats` ADD CONSTRAINT `FK_seats_TO_show_seats_1` FOREIGN KEY (
                                                                                `seat_id`
    )
    REFERENCES `seats` (
                        `id`
        );

ALTER TABLE `show_seats` ADD CONSTRAINT `FK_shows_TO_show_seats_1` FOREIGN KEY (
                                                                                `show_id`
    )
    REFERENCES `shows` (
                        `id`
        );

