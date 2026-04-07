const API_BASE = window.location.port === "9090" || window.location.port === ""
    ? ""
    : `${window.location.protocol}//${window.location.hostname}:9090`;

const STORAGE_KEY = "ticketinglab.admin.console.state";

const defaultSeatItems = JSON.stringify([
    { label: "A1", rowNo: 1, colNo: 1 },
    { label: "A2", rowNo: 1, colNo: 2 },
    { label: "A3", rowNo: 1, colNo: 3 }
], null, 2);

const defaultSectionItems = JSON.stringify([
    { name: "VIP" },
    { name: "R" }
], null, 2);

const defaultShowSeatItems = JSON.stringify([
    { seatId: 1, price: 150000 }
], null, 2);

const defaultSectionInventoryItems = JSON.stringify([
    { sectionId: 1, price: 120000, capacity: 100 }
], null, 2);

const state = {
    accessToken: "",
    venueId: null,
    eventId: null,
    showId: null,
    seats: [],
    sections: []
};

const dom = {
    accessTokenInput: document.getElementById("access-token-input"),
    saveTokenButton: document.getElementById("save-token-button"),
    apiBaseBadge: document.getElementById("api-base-badge"),
    venueCodeInput: document.getElementById("venue-code-input"),
    venueNameInput: document.getElementById("venue-name-input"),
    venueAddressInput: document.getElementById("venue-address-input"),
    referenceVenueIdInput: document.getElementById("reference-venue-id-input"),
    seatItemsInput: document.getElementById("seat-items-input"),
    sectionItemsInput: document.getElementById("section-items-input"),
    upsertVenueButton: document.getElementById("upsert-venue-button"),
    loadSeatsButton: document.getElementById("load-seats-button"),
    createSeatsButton: document.getElementById("create-seats-button"),
    loadSectionsButton: document.getElementById("load-sections-button"),
    createSectionsButton: document.getElementById("create-sections-button"),
    eventTitleInput: document.getElementById("event-title-input"),
    eventStatusInput: document.getElementById("event-status-input"),
    eventDescInput: document.getElementById("event-desc-input"),
    createEventButton: document.getElementById("create-event-button"),
    showEventIdInput: document.getElementById("show-event-id-input"),
    showVenueIdInput: document.getElementById("show-venue-id-input"),
    showStartAtInput: document.getElementById("show-start-at-input"),
    createShowButton: document.getElementById("create-show-button"),
    inventoryShowIdInput: document.getElementById("inventory-show-id-input"),
    showSeatItemsInput: document.getElementById("show-seat-items-input"),
    sectionInventoryItemsInput: document.getElementById("section-inventory-items-input"),
    createShowSeatsButton: document.getElementById("create-show-seats-button"),
    createSectionInventoriesButton: document.getElementById("create-section-inventories-button"),
    openAvailabilityButton: document.getElementById("open-availability-button"),
    openHoldsButton: document.getElementById("open-holds-button"),
    openReservationsButton: document.getElementById("open-reservations-button"),
    checkinQrTokenInput: document.getElementById("checkin-qr-token-input"),
    checkinEndpointPreview: document.getElementById("checkin-endpoint-preview"),
    checkinButton: document.getElementById("checkin-button"),
    openPaymentsButton: document.getElementById("open-payments-button"),
    openCheckinPageButton: document.getElementById("open-checkin-page-button"),
    clearStateButton: document.getElementById("clear-state-button"),
    clearLogButton: document.getElementById("clear-log-button"),
    consoleStatus: document.getElementById("console-status"),
    stateVenueId: document.getElementById("state-venue-id"),
    stateEventId: document.getElementById("state-event-id"),
    stateShowId: document.getElementById("state-show-id"),
    stateSeatCount: document.getElementById("state-seat-count"),
    stateSectionCount: document.getElementById("state-section-count"),
    stateTokenStatus: document.getElementById("state-token-status"),
    stateAccessToken: document.getElementById("state-access-token"),
    seatsSummaryBadge: document.getElementById("seats-summary-badge"),
    sectionsSummaryBadge: document.getElementById("sections-summary-badge"),
    seatListView: document.getElementById("seat-list-view"),
    sectionListView: document.getElementById("section-list-view"),
    lastAction: document.getElementById("last-action"),
    lastStatus: document.getElementById("last-status"),
    resultView: document.getElementById("result-view")
};

function nowText() {
    return new Date().toLocaleTimeString("ko-KR", { hour12: false });
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function readStorage() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

function persistState() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function setState(patch) {
    Object.assign(state, patch);
    persistState();
    syncView();
}

function collectHeaders(response) {
    const headers = {};
    response.headers.forEach((value, key) => {
        headers[key] = value;
    });
    return headers;
}

async function parseBody(response) {
    const text = await response.text();
    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
}

function buildEndpoint(path) {
    return `${API_BASE}${path}`;
}

function requireAccessToken() {
    const token = dom.accessTokenInput.value.trim() || state.accessToken;
    if (!token) {
        renderLog("Admin Console", "Missing token", {
            error: "ADMIN access token을 먼저 입력해 주세요."
        });
        return null;
    }
    return token;
}

async function callApi({ title, endpoint, method = "GET", body }) {
    const accessToken = requireAccessToken();
    if (!accessToken) {
        return null;
    }

    const headers = {
        Accept: "application/json",
        Authorization: `Bearer ${accessToken}`
    };
    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    renderLog(title, `Request started at ${nowText()}`, {
        request: {
            method,
            endpoint,
            headers,
            body
        }
    });

    try {
        const response = await fetch(endpoint, {
            method,
            headers,
            credentials: API_BASE ? "omit" : "same-origin",
            body: body !== undefined ? JSON.stringify(body) : undefined
        });

        const responseHeaders = collectHeaders(response);
        const responseBody = await parseBody(response);

        renderLog(title, `${response.status} ${response.statusText}`, {
            request: {
                method,
                endpoint,
                headers,
                body
            },
            response: {
                status: response.status,
                statusText: response.statusText,
                headers: responseHeaders,
                body: responseBody
            }
        });

        return {
            ok: response.ok,
            status: response.status,
            body: responseBody
        };
    } catch (error) {
        renderLog(title, "Network error", { error: error.message });
        throw error;
    }
}

function parseJsonInput(text, fieldName) {
    try {
        return JSON.parse(text);
    } catch {
        renderLog(fieldName, "Invalid JSON", {
            error: `${fieldName} 입력값이 올바른 JSON이 아닙니다.`
        });
        return null;
    }
}

function readPositiveNumber(input, fieldName) {
    const value = Number(input.value);
    if (!Number.isInteger(value) || value < 1) {
        renderLog(fieldName, "Invalid input", {
            error: `${fieldName}은 1 이상의 정수여야 합니다.`
        });
        return null;
    }
    return value;
}

function seedInventoryPayloads() {
    const canOverwriteSeats = !dom.showSeatItemsInput.value.trim() || dom.showSeatItemsInput.value.trim() === defaultShowSeatItems;
    if (canOverwriteSeats && state.seats.length) {
        dom.showSeatItemsInput.value = JSON.stringify(
            state.seats.slice(0, 5).map((seat) => ({
                seatId: seat.seatId,
                price: 150000
            })),
            null,
            2
        );
    }

    const canOverwriteSections = !dom.sectionInventoryItemsInput.value.trim() || dom.sectionInventoryItemsInput.value.trim() === defaultSectionInventoryItems;
    if (canOverwriteSections && state.sections.length) {
        dom.sectionInventoryItemsInput.value = JSON.stringify(
            state.sections.slice(0, 3).map((section) => ({
                sectionId: section.sectionId,
                price: 120000,
                capacity: 100
            })),
            null,
            2
        );
    }
}

function renderEmptyList(target, title, description) {
    target.innerHTML = `
        <article class="empty-panel">
            <strong>${escapeHtml(title)}</strong>
            <p class="placeholder-copy">${escapeHtml(description)}</p>
        </article>
    `;
}

function renderSeatList() {
    if (!state.seats.length) {
        renderEmptyList(dom.seatListView, "좌석 정보가 없습니다.", "좌석 조회를 실행하면 seatId와 좌석 라벨이 표시됩니다.");
        return;
    }

    dom.seatListView.innerHTML = state.seats.map((seat) => `
        <article class="inventory-entry">
            <div class="entry-head">
                <strong>${escapeHtml(seat.label)}</strong>
                <span class="token">seatId ${escapeHtml(seat.seatId)}</span>
            </div>
            <div class="entry-meta">
                <span class="token">row ${escapeHtml(seat.rowNo ?? "-")}</span>
                <span class="token">col ${escapeHtml(seat.colNo ?? "-")}</span>
            </div>
        </article>
    `).join("");
}

function renderSectionList() {
    if (!state.sections.length) {
        renderEmptyList(dom.sectionListView, "구역 정보가 없습니다.", "구역 조회를 실행하면 sectionId와 이름이 표시됩니다.");
        return;
    }

    dom.sectionListView.innerHTML = state.sections.map((section) => `
        <article class="inventory-entry">
            <div class="entry-head">
                <strong>${escapeHtml(section.name)}</strong>
                <span class="token">sectionId ${escapeHtml(section.sectionId)}</span>
            </div>
        </article>
    `).join("");
}

function syncView() {
    dom.apiBaseBadge.textContent = API_BASE ? `API ${API_BASE}` : "API same-origin";

    dom.stateVenueId.textContent = state.venueId ?? "-";
    dom.stateEventId.textContent = state.eventId ?? "-";
    dom.stateShowId.textContent = state.showId ?? "-";
    dom.stateSeatCount.textContent = String(state.seats.length);
    dom.stateSectionCount.textContent = String(state.sections.length);
    dom.stateTokenStatus.textContent = state.accessToken ? "SAVED" : "EMPTY";
    dom.stateAccessToken.value = state.accessToken || "";
    dom.consoleStatus.textContent = state.accessToken ? "READY" : "TOKEN NEEDED";
    dom.consoleStatus.className = `status ${state.accessToken ? "ready" : "idle"}`;

    dom.referenceVenueIdInput.value = state.venueId ?? "";
    dom.showVenueIdInput.value = state.venueId ?? "";
    dom.showEventIdInput.value = state.eventId ?? "";
    dom.inventoryShowIdInput.value = state.showId ?? "";
    dom.accessTokenInput.value = state.accessToken || dom.accessTokenInput.value;

    dom.seatsSummaryBadge.textContent = state.seats.length ? `${state.seats.length} Seats` : "NO DATA";
    dom.sectionsSummaryBadge.textContent = state.sections.length ? `${state.sections.length} Sections` : "NO DATA";
    dom.seatsSummaryBadge.className = `status ${state.seats.length ? "ready" : "idle"}`;
    dom.sectionsSummaryBadge.className = `status ${state.sections.length ? "ready" : "idle"}`;

    renderSeatList();
    renderSectionList();
}

function resetState() {
    state.accessToken = "";
    state.venueId = null;
    state.eventId = null;
    state.showId = null;
    state.seats = [];
    state.sections = [];
    persistState();

    dom.accessTokenInput.value = "";
    dom.referenceVenueIdInput.value = "";
    dom.showVenueIdInput.value = "";
    dom.showEventIdInput.value = "";
    dom.inventoryShowIdInput.value = "";
    dom.checkinQrTokenInput.value = "";
    dom.showSeatItemsInput.value = defaultShowSeatItems;
    dom.sectionInventoryItemsInput.value = defaultSectionInventoryItems;

    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Admin API Test Console loaded"
    });
    syncView();
}

async function loadSeats() {
    const venueId = readPositiveNumber(dom.referenceVenueIdInput, "Venue ID");
    if (!venueId) {
        return;
    }

    const result = await callApi({
        title: "ADM-008 List Venue Seats",
        endpoint: buildEndpoint(`/api/admin/venues/${venueId}/seats`)
    });

    if (result?.ok) {
        setState({
            venueId,
            seats: Array.isArray(result.body?.seats) ? result.body.seats : []
        });
        seedInventoryPayloads();
    }
}

async function loadSections() {
    const venueId = readPositiveNumber(dom.referenceVenueIdInput, "Venue ID");
    if (!venueId) {
        return;
    }

    const result = await callApi({
        title: "ADM-009 List Venue Sections",
        endpoint: buildEndpoint(`/api/admin/venues/${venueId}/sections`)
    });

    if (result?.ok) {
        setState({
            venueId,
            sections: Array.isArray(result.body?.sections) ? result.body.sections : []
        });
        seedInventoryPayloads();
    }
}

dom.saveTokenButton.addEventListener("click", () => {
    const accessToken = dom.accessTokenInput.value.trim();
    setState({ accessToken });
    renderLog("Admin Token", accessToken ? "Token saved" : "Token cleared", {
        saved: Boolean(accessToken)
    });
});

dom.upsertVenueButton.addEventListener("click", async () => {
    const result = await callApi({
        title: "ADM-001 Venue Upsert",
        endpoint: buildEndpoint("/api/admin/venues/upsert"),
        method: "POST",
        body: {
            code: dom.venueCodeInput.value.trim(),
            name: dom.venueNameInput.value.trim(),
            address: dom.venueAddressInput.value.trim()
        }
    });

    if (result?.ok && result.body?.venueId) {
        setState({ venueId: result.body.venueId });
    }
});

dom.createSeatsButton.addEventListener("click", async () => {
    const venueId = readPositiveNumber(dom.referenceVenueIdInput, "Venue ID");
    if (!venueId) {
        return;
    }

    const seats = parseJsonInput(dom.seatItemsInput.value, "Seats JSON");
    if (!Array.isArray(seats)) {
        return;
    }

    const result = await callApi({
        title: "ADM-002 Create Venue Seats",
        endpoint: buildEndpoint(`/api/admin/venues/${venueId}/seats`),
        method: "POST",
        body: { seats }
    });

    if (result?.ok) {
        setState({ venueId });
        await loadSeats();
    }
});

dom.createSectionsButton.addEventListener("click", async () => {
    const venueId = readPositiveNumber(dom.referenceVenueIdInput, "Venue ID");
    if (!venueId) {
        return;
    }

    const sections = parseJsonInput(dom.sectionItemsInput.value, "Sections JSON");
    if (!Array.isArray(sections)) {
        return;
    }

    const result = await callApi({
        title: "ADM-003 Create Venue Sections",
        endpoint: buildEndpoint(`/api/admin/venues/${venueId}/sections`),
        method: "POST",
        body: { sections }
    });

    if (result?.ok) {
        setState({ venueId });
        await loadSections();
    }
});

dom.loadSeatsButton.addEventListener("click", loadSeats);
dom.loadSectionsButton.addEventListener("click", loadSections);

dom.createEventButton.addEventListener("click", async () => {
    const result = await callApi({
        title: "ADM-004 Create Event",
        endpoint: buildEndpoint("/api/admin/events"),
        method: "POST",
        body: {
            title: dom.eventTitleInput.value.trim(),
            desc: dom.eventDescInput.value.trim(),
            status: dom.eventStatusInput.value
        }
    });

    if (result?.ok && result.body?.eventId) {
        setState({ eventId: result.body.eventId });
    }
});

dom.createShowButton.addEventListener("click", async () => {
    const eventId = readPositiveNumber(dom.showEventIdInput, "Event ID");
    const venueId = readPositiveNumber(dom.showVenueIdInput, "Venue ID");
    if (!eventId || !venueId) {
        return;
    }

    const startAt = dom.showStartAtInput.value;
    if (!startAt) {
        renderLog("ADM-005 Create Show", "Invalid input", {
            error: "Start At을 입력해 주세요."
        });
        return;
    }

    const result = await callApi({
        title: "ADM-005 Create Show",
        endpoint: buildEndpoint("/api/admin/shows"),
        method: "POST",
        body: {
            eventId,
            venueId,
            startAt
        }
    });

    if (result?.ok && result.body?.showId) {
        setState({ showId: result.body.showId, eventId, venueId });
    }
});

dom.createShowSeatsButton.addEventListener("click", async () => {
    const showId = readPositiveNumber(dom.inventoryShowIdInput, "Show ID");
    if (!showId) {
        return;
    }

    const items = parseJsonInput(dom.showSeatItemsInput.value, "Show Seats JSON");
    if (!Array.isArray(items)) {
        return;
    }

    const result = await callApi({
        title: "ADM-006 Create Show Seats",
        endpoint: buildEndpoint(`/api/admin/shows/${showId}/show-seats`),
        method: "POST",
        body: { items }
    });

    if (result?.ok) {
        setState({ showId });
    }
});

dom.createSectionInventoriesButton.addEventListener("click", async () => {
    const showId = readPositiveNumber(dom.inventoryShowIdInput, "Show ID");
    if (!showId) {
        return;
    }

    const items = parseJsonInput(dom.sectionInventoryItemsInput.value, "Section Inventories JSON");
    if (!Array.isArray(items)) {
        return;
    }

    const result = await callApi({
        title: "ADM-007 Create Section Inventories",
        endpoint: buildEndpoint(`/api/admin/shows/${showId}/section-inventories`),
        method: "POST",
        body: { items }
    });

    if (result?.ok) {
        setState({ showId });
    }
});

dom.openAvailabilityButton.addEventListener("click", () => {
    const showId = readPositiveNumber(dom.inventoryShowIdInput, "Show ID");
    if (!showId) {
        return;
    }

    window.open(buildEndpoint(`/shows-test.html?showId=${showId}`), "_blank", "noopener,noreferrer");
});


dom.openHoldsButton.addEventListener("click", () => {
    const showId = readPositiveNumber(dom.inventoryShowIdInput, "Show ID");
    if (!showId) {
        return;
    }

    window.open(buildEndpoint(`/holds-test.html?showId=${showId}`), "_blank", "noopener,noreferrer");
});

dom.openReservationsButton.addEventListener("click", () => {
    const showId = readPositiveNumber(dom.inventoryShowIdInput, "Show ID");
    if (!showId) {
        return;
    }

    window.open(buildEndpoint(`/reservations-test.html?showId=${showId}`), "_blank", "noopener,noreferrer");
});
dom.checkinButton.addEventListener("click", async () => {
    const qrToken = dom.checkinQrTokenInput.value.trim();
    if (!qrToken) {
        renderLog("CHK-001 Check-in", "Invalid input", {
            error: "qrToken을 입력해 주세요."
        });
        return;
    }

    await callApi({
        title: "CHK-001 Check-in",
        endpoint: buildEndpoint("/api/checkin"),
        method: "POST",
        body: { qrToken }
    });
});

dom.openPaymentsButton.addEventListener("click", () => {
    window.open(buildEndpoint("/payments-test.html"), "_blank", "noopener,noreferrer");
});

dom.openCheckinPageButton.addEventListener("click", () => {
    const qrToken = dom.checkinQrTokenInput.value.trim();
    const suffix = qrToken ? `?qrToken=${encodeURIComponent(qrToken)}` : "";
    window.open(buildEndpoint(`/checkin-test.html${suffix}`), "_blank", "noopener,noreferrer");
});
dom.clearStateButton.addEventListener("click", resetState);

dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Admin API Test Console loaded"
    });
});

(function init() {
    const saved = readStorage();
    if (saved) {
        Object.assign(state, saved);
    }

    dom.seatItemsInput.value = defaultSeatItems;
    dom.sectionItemsInput.value = defaultSectionItems;
    dom.showSeatItemsInput.value = defaultShowSeatItems;
    dom.sectionInventoryItemsInput.value = defaultSectionInventoryItems;
    dom.showStartAtInput.value = new Date(Date.now() + 86400000).toISOString().slice(0, 16);
    dom.checkinEndpointPreview.value = buildEndpoint("/api/checkin").replace(API_BASE, "");

    if (state.accessToken) {
        dom.accessTokenInput.value = state.accessToken;
    }

    syncView();
    seedInventoryPayloads();
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Admin API Test Console loaded"
    });
})();
