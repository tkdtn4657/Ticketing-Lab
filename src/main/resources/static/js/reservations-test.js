const API_BASE = window.location.port === "9090" || window.location.port === ""
    ? ""
    : `${window.location.protocol}//${window.location.hostname}:9090`;

const STORAGE_KEY = "ticketinglab.reservations.console.state";

const state = {
    accessToken: "",
    holdId: "",
    reservationId: "",
    selectedShowId: null,
    reservation: null,
    items: [],
    reservations: [],
    listPage: 0,
    listSize: 20,
    listStatus: "",
    totalElements: 0,
    totalPages: 0,
    seats: [],
    sections: []
};

const dom = {
    accessTokenInput: document.getElementById("access-token-input"),
    saveTokenButton: document.getElementById("save-token-button"),
    tokenStatusBadge: document.getElementById("token-status-badge"),
    holdIdInput: document.getElementById("hold-id-input"),
    createEndpointPreview: document.getElementById("create-endpoint-preview"),
    createReservationButton: document.getElementById("create-reservation-button"),
    openHoldsButton: document.getElementById("open-holds-button"),
    reservationIdInput: document.getElementById("reservation-id-input"),
    detailEndpointPreview: document.getElementById("detail-endpoint-preview"),
    pageInput: document.getElementById("page-input"),
    sizeInput: document.getElementById("size-input"),
    statusFilterSelect: document.getElementById("status-filter-select"),
    listEndpointPreview: document.getElementById("list-endpoint-preview"),
    getReservationButton: document.getElementById("get-reservation-button"),
    listReservationsButton: document.getElementById("list-reservations-button"),
    loadAvailabilityButton: document.getElementById("load-availability-button"),
    reservationItemListView: document.getElementById("reservation-item-list-view"),
    reservationListView: document.getElementById("reservation-list-view"),
    seatListView: document.getElementById("seat-list-view"),
    sectionListView: document.getElementById("section-list-view"),
    reservationSummaryBadge: document.getElementById("reservation-summary-badge"),
    reservationsSummaryBadge: document.getElementById("reservations-summary-badge"),
    seatsSummaryBadge: document.getElementById("seats-summary-badge"),
    sectionsSummaryBadge: document.getElementById("sections-summary-badge"),
    openShowsButton: document.getElementById("open-shows-button"),
    clearStateButton: document.getElementById("clear-state-button"),
    clearLogButton: document.getElementById("clear-log-button"),
    consoleStatus: document.getElementById("console-status"),
    stateTokenStatus: document.getElementById("state-token-status"),
    stateHoldId: document.getElementById("state-hold-id"),
    stateReservationId: document.getElementById("state-reservation-id"),
    stateShowId: document.getElementById("state-show-id"),
    stateReservationStatus: document.getElementById("state-reservation-status"),
    stateItemCount: document.getElementById("state-item-count"),
    stateTotalAmount: document.getElementById("state-total-amount"),
    stateListTotal: document.getElementById("state-list-total"),
    stateExpiresAt: document.getElementById("state-expires-at"),
    stateCreatedAt: document.getElementById("state-created-at"),
    stateReservationSnapshot: document.getElementById("state-reservation-snapshot"),
    stateListSnapshot: document.getElementById("state-list-snapshot"),
    stateAvailabilitySnapshot: document.getElementById("state-availability-snapshot"),
    lastAction: document.getElementById("last-action"),
    lastStatus: document.getElementById("last-status"),
    resultView: document.getElementById("result-view")
};

function nowText() {
    return new Date().toLocaleTimeString("ko-KR", { hour12: false });
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function parseBody(text) {
    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
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
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
        accessToken: state.accessToken,
        holdId: state.holdId,
        reservationId: state.reservationId,
        selectedShowId: state.selectedShowId,
        listPage: state.listPage,
        listSize: state.listSize,
        listStatus: state.listStatus
    }));
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
}

function updateQuery(showId, holdId, reservationId) {
    const url = new URL(window.location.href);

    if (showId) {
        url.searchParams.set("showId", String(showId));
    } else {
        url.searchParams.delete("showId");
    }

    if (holdId) {
        url.searchParams.set("holdId", holdId);
    } else {
        url.searchParams.delete("holdId");
    }

    if (reservationId) {
        url.searchParams.set("reservationId", reservationId);
    } else {
        url.searchParams.delete("reservationId");
    }

    window.history.replaceState({}, "", url);
}

function setState(patch) {
    Object.assign(state, patch);
    persistState();
    updateQuery(state.selectedShowId, state.holdId, state.reservationId);
    syncView();
}

function collectHeaders(response) {
    const headers = {};
    response.headers.forEach((value, key) => {
        headers[key] = value;
    });
    return headers;
}

function rememberAccessToken(token) {
    if (token && state.accessToken !== token) {
        state.accessToken = token;
        persistState();
    }
}

function requireAccessToken() {
    const token = dom.accessTokenInput.value.trim() || state.accessToken;
    if (!token) {
        renderLog("Reservations Console", "Missing token", {
            error: "USER access token을 먼저 입력해 주세요."
        });
        return null;
    }

    rememberAccessToken(token);
    return token;
}

async function callApi({ title, endpoint, method = "GET", body, accessTokenRequired = false, silent = false }) {
    const headers = { Accept: "application/json" };

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    if (accessTokenRequired) {
        const accessToken = requireAccessToken();
        if (!accessToken) {
            return null;
        }
        headers.Authorization = `Bearer ${accessToken}`;
    }

    if (!silent) {
        renderLog(title, `Request started at ${nowText()}`, {
            request: {
                method,
                endpoint,
                headers,
                body
            }
        });
    }

    try {
        const response = await fetch(endpoint, {
            method,
            headers,
            credentials: API_BASE ? "omit" : "same-origin",
            body: body !== undefined ? JSON.stringify(body) : undefined
        });

        const bodyText = await response.text();
        const responseBody = parseBody(bodyText);
        const responseHeaders = collectHeaders(response);

        if (!silent) {
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
        }

        return {
            ok: response.ok,
            status: response.status,
            body: responseBody,
            headers: responseHeaders
        };
    } catch (error) {
        if (!silent) {
            renderLog(title, "Network error", { error: error.message });
        }
        throw error;
    }
}

function setLoading(button, loading) {
    button.disabled = loading;
}

function readPositiveNumber(input, label, { min = 0 } = {}) {
    const value = Number(input.value);
    if (!Number.isInteger(value) || value < min) {
        renderLog("Reservations Console", "Invalid input", {
            error: `${label}는 ${min} 이상의 정수여야 합니다.`
        });
        return null;
    }
    return value;
}

function buildCreateEndpoint() {
    return `${API_BASE}/api/reservations`;
}

function buildDetailEndpoint(reservationId) {
    return reservationId
        ? `${API_BASE}/api/reservations/${reservationId}`
        : `${API_BASE}/api/reservations/{reservationId}`;
}

function buildListEndpoint(page, size, status) {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", String(size));
    if (status) {
        params.set("status", status);
    }
    return `${API_BASE}/api/me/reservations?${params.toString()}`;
}

function buildAvailabilityEndpoint(showId) {
    return showId
        ? `${API_BASE}/api/shows/${showId}/availability`
        : `${API_BASE}/api/shows/{showId}/availability`;
}

function readPositiveQueryNumber(name) {
    const params = new URLSearchParams(window.location.search);
    const value = Number(params.get(name));
    return Number.isInteger(value) && value > 0 ? value : null;
}

function readQueryText(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name) || "";
}

function reservationBadgeClass(status) {
    if (status === "PAID") {
        return "ready";
    }
    if (status === "PENDING_PAYMENT") {
        return "warn";
    }
    if (status === "EXPIRED" || status === "CANCELED") {
        return "danger";
    }
    return "idle";
}

function reservationTokenClass(status) {
    if (status === "PAID") {
        return "available";
    }
    if (status === "PENDING_PAYMENT") {
        return "section-remaining";
    }
    return "unavailable";
}

function reservationCardClass(status) {
    if (status === "PAID") {
        return "available";
    }
    if (status === "PENDING_PAYMENT") {
        return "section-card";
    }
    return "unavailable";
}

function syncEndpointPreview() {
    const reservationId = dom.reservationIdInput.value.trim() || state.reservationId;
    const page = dom.pageInput.value.trim() || String(state.listPage);
    const size = dom.sizeInput.value.trim() || String(state.listSize);
    const status = dom.statusFilterSelect.value.trim() || "";

    dom.createEndpointPreview.value = buildCreateEndpoint();
    dom.detailEndpointPreview.value = buildDetailEndpoint(reservationId);
    dom.listEndpointPreview.value = buildListEndpoint(page, size, status).replace(API_BASE, "");
}

function syncSummary() {
    const reservation = state.reservation;
    const tokenSaved = Boolean(state.accessToken);
    const loaded = Boolean(reservation || state.reservations.length || state.seats.length || state.sections.length);

    if (state.accessToken && dom.accessTokenInput.value.trim() !== state.accessToken) {
        dom.accessTokenInput.value = state.accessToken;
    }
    if (state.holdId) {
        dom.holdIdInput.value = state.holdId;
    }
    if (state.reservationId) {
        dom.reservationIdInput.value = state.reservationId;
    }
    dom.pageInput.value = String(state.listPage);
    dom.sizeInput.value = String(state.listSize);
    dom.statusFilterSelect.value = state.listStatus;

    dom.tokenStatusBadge.textContent = tokenSaved ? "TOKEN SAVED" : "TOKEN EMPTY";
    dom.tokenStatusBadge.className = `status ${tokenSaved ? "ready" : "idle"}`;
    dom.stateTokenStatus.textContent = tokenSaved ? "SAVED" : "EMPTY";
    dom.stateHoldId.textContent = state.holdId || "-";
    dom.stateReservationId.textContent = state.reservationId || "-";
    dom.stateShowId.textContent = state.selectedShowId != null ? String(state.selectedShowId) : "-";
    dom.stateReservationStatus.textContent = reservation?.status || "-";
    dom.stateItemCount.textContent = String(state.items.length);
    dom.stateTotalAmount.textContent = String(reservation?.totalAmount ?? 0);
    dom.stateListTotal.textContent = String(state.totalElements);
    dom.stateExpiresAt.textContent = formatDateTime(reservation?.expiresAt);
    dom.stateCreatedAt.textContent = formatDateTime(reservation?.createdAt);
    dom.stateReservationSnapshot.value = JSON.stringify({ reservation: state.reservation, items: state.items }, null, 2);
    dom.stateListSnapshot.value = JSON.stringify({
        page: state.listPage,
        size: state.listSize,
        status: state.listStatus || null,
        totalElements: state.totalElements,
        totalPages: state.totalPages,
        reservations: state.reservations
    }, null, 2);
    dom.stateAvailabilitySnapshot.value = JSON.stringify({
        showId: state.selectedShowId,
        seats: state.seats,
        sections: state.sections
    }, null, 2);

    dom.reservationSummaryBadge.textContent = reservation
        ? `${reservation.status} / ${state.items.length} item${state.items.length > 1 ? "s" : ""}`
        : "No Reservation Loaded";
    dom.reservationSummaryBadge.className = `status ${reservation ? reservationBadgeClass(reservation.status) : "idle"}`;

    dom.reservationsSummaryBadge.textContent = state.reservations.length
        ? `${state.totalElements} reservation${state.totalElements > 1 ? "s" : ""} / page ${state.listPage + 1}`
        : "No Reservations Loaded";
    dom.reservationsSummaryBadge.className = `status ${state.reservations.length ? "ready" : "idle"}`;

    const availableSeatCount = state.seats.filter((seat) => seat.available).length;
    dom.seatsSummaryBadge.textContent = state.seats.length
        ? `${availableSeatCount}/${state.seats.length} Seats Available`
        : "No Seats Loaded";
    dom.seatsSummaryBadge.className = `status ${state.seats.length ? "ready" : "idle"}`;

    dom.sectionsSummaryBadge.textContent = state.sections.length
        ? `${state.sections.length} Section${state.sections.length > 1 ? "s" : ""} Loaded`
        : "No Sections Loaded";
    dom.sectionsSummaryBadge.className = `status ${state.sections.length ? "ready" : "idle"}`;

    dom.consoleStatus.textContent = loaded ? "Loaded" : "Ready";
    dom.consoleStatus.className = `status ${loaded ? "ready" : "idle"}`;
}

function renderEmptyReservationItems() {
    dom.reservationItemListView.innerHTML = `
        <article class="empty-panel">
            <strong>아직 조회된 예약이 없습니다.</strong>
            <p class="placeholder-copy">예약을 생성하거나 reservationId로 조회하면 이 영역에 seat/section item과 단가 정보가 표시됩니다.</p>
        </article>
    `;
}

function renderReservationItems() {
    if (!state.items.length) {
        renderEmptyReservationItems();
        return;
    }

    dom.reservationItemListView.innerHTML = state.items.map((item) => {
        const isSeat = item.type === "SEAT";
        const targetId = isSeat ? item.seatId : item.sectionId;
        return `
            <article class="inventory-card ${isSeat ? "available" : "section-card"}">
                <div class="inventory-head">
                    <div>
                        <h3>${escapeHtml(item.type)}</h3>
                        <p class="muted">${isSeat ? "seatId" : "sectionId"} ${escapeHtml(targetId)}</p>
                    </div>
                    <span class="status-token ${isSeat ? "available" : "section-remaining"}">${escapeHtml(item.type)}</span>
                </div>
                <div class="token-row">
                    <span class="token">qty ${escapeHtml(item.qty)}</span>
                    <span class="token">unitPrice ${escapeHtml(item.unitPrice)}</span>
                    <span class="token">${isSeat ? `seatId ${escapeHtml(item.seatId)}` : `sectionId ${escapeHtml(item.sectionId)}`}</span>
                </div>
            </article>
        `;
    }).join("");
}

function renderEmptyReservationList() {
    dom.reservationListView.innerHTML = `
        <article class="empty-panel">
            <strong>아직 불러온 예약 목록이 없습니다.</strong>
            <p class="placeholder-copy">내 예약 목록 조회를 실행하면 이 영역에 reservationId, 상태, 총액, 만료 시각이 표시됩니다.</p>
        </article>
    `;
}

function renderReservationList() {
    if (!state.reservations.length) {
        renderEmptyReservationList();
        return;
    }

    dom.reservationListView.innerHTML = state.reservations.map((reservation) => `
        <article class="inventory-card ${reservationCardClass(reservation.status)}">
            <div class="inventory-head">
                <div>
                    <h3>${escapeHtml(reservation.reservationId)}</h3>
                    <p class="muted">showId ${escapeHtml(reservation.showId)}</p>
                </div>
                <span class="status-token ${reservationTokenClass(reservation.status)}">${escapeHtml(reservation.status)}</span>
            </div>
            <div class="token-row">
                <span class="token">totalAmount ${escapeHtml(reservation.totalAmount)}</span>
                <span class="token">expiresAt ${escapeHtml(formatDateTime(reservation.expiresAt))}</span>
                <span class="token">createdAt ${escapeHtml(formatDateTime(reservation.createdAt))}</span>
            </div>
            <div class="utility-inline">
                <button type="button" class="button button-inline pick-reservation-button" data-reservation-id="${escapeHtml(reservation.reservationId)}">상세 불러오기</button>
            </div>
        </article>
    `).join("");
}

function renderEmptySeatList() {
    dom.seatListView.innerHTML = `
        <article class="empty-panel">
            <strong>불러온 좌석 정보가 없습니다.</strong>
            <p class="placeholder-copy">예약 상세를 불러온 뒤 가용성 조회를 실행하면 이 영역에 좌석별 상태가 표시됩니다.</p>
        </article>
    `;
}

function renderSeatList() {
    if (!state.seats.length) {
        renderEmptySeatList();
        return;
    }

    dom.seatListView.innerHTML = state.seats.map((seat) => `
        <article class="inventory-card ${seat.available ? "available" : "unavailable"}">
            <div class="inventory-head">
                <div>
                    <h3>${escapeHtml(seat.label || `Seat #${seat.seatId}`)}</h3>
                    <p class="muted">seatId ${escapeHtml(seat.seatId)}</p>
                </div>
                <span class="status-token ${seat.available ? "available" : "unavailable"}">${seat.available ? "AVAILABLE" : "UNAVAILABLE"}</span>
            </div>
            <div class="meta-row">
                <span class="token">rowNo ${escapeHtml(seat.rowNo ?? "-")}</span>
                <span class="token">colNo ${escapeHtml(seat.colNo ?? "-")}</span>
                <span class="token">price ${escapeHtml(seat.price)}</span>
            </div>
        </article>
    `).join("");
}

function renderEmptySectionList() {
    dom.sectionListView.innerHTML = `
        <article class="empty-panel">
            <strong>불러온 구역 정보가 없습니다.</strong>
            <p class="placeholder-copy">예약 상세를 불러온 뒤 가용성 조회를 실행하면 이 영역에 구역별 remainingQty가 표시됩니다.</p>
        </article>
    `;
}

function renderSectionList() {
    if (!state.sections.length) {
        renderEmptySectionList();
        return;
    }

    dom.sectionListView.innerHTML = state.sections.map((section) => `
        <article class="inventory-card section-card">
            <div class="inventory-head">
                <div>
                    <h3>${escapeHtml(section.name || `Section #${section.sectionId}`)}</h3>
                    <p class="muted">sectionId ${escapeHtml(section.sectionId)}</p>
                </div>
                <span class="status-token section-remaining">remaining ${escapeHtml(section.remainingQty)}</span>
            </div>
            <div class="meta-row">
                <span class="token">price ${escapeHtml(section.price)}</span>
                <span class="token">remainingQty ${escapeHtml(section.remainingQty)}</span>
            </div>
        </article>
    `).join("");
}

function syncView() {
    syncEndpointPreview();
    syncSummary();
    renderReservationItems();
    renderReservationList();
    renderSeatList();
    renderSectionList();
}

async function fetchReservationDetail(reservationId, { silent = false } = {}) {
    const result = await callApi({
        title: "RES-002 Reservation Detail",
        endpoint: buildDetailEndpoint(reservationId),
        accessTokenRequired: true,
        silent
    });

    if (result?.ok && result.body) {
        setState({
            reservationId,
            selectedShowId: result.body.reservation?.showId ?? state.selectedShowId,
            reservation: result.body.reservation ?? null,
            items: Array.isArray(result.body.items) ? result.body.items : []
        });
    }

    return result;
}

async function fetchMyReservations({ silent = false } = {}) {
    const page = readPositiveNumber(dom.pageInput, "Page", { min: 0 });
    const size = readPositiveNumber(dom.sizeInput, "Size", { min: 1 });
    if (page == null || size == null) {
        return null;
    }

    const status = dom.statusFilterSelect.value.trim();
    const result = await callApi({
        title: "RES-003 My Reservations",
        endpoint: buildListEndpoint(page, size, status),
        accessTokenRequired: true,
        silent
    });

    if (result?.ok && result.body) {
        setState({
            listPage: result.body.page ?? page,
            listSize: result.body.size ?? size,
            listStatus: status,
            totalElements: result.body.totalElements ?? 0,
            totalPages: result.body.totalPages ?? 0,
            reservations: Array.isArray(result.body.reservations) ? result.body.reservations : []
        });
    }

    return result;
}

async function fetchAvailability(showId, { silent = false } = {}) {
    const result = await callApi({
        title: "SHW-001 Show Availability",
        endpoint: buildAvailabilityEndpoint(showId),
        silent
    });

    if (result?.ok && result.body) {
        setState({
            selectedShowId: showId,
            seats: Array.isArray(result.body.seats) ? result.body.seats : [],
            sections: Array.isArray(result.body.sections) ? result.body.sections : []
        });
    } else if (result) {
        setState({
            selectedShowId: showId,
            seats: [],
            sections: []
        });
    }

    return result;
}

async function createReservation() {
    const holdId = dom.holdIdInput.value.trim() || state.holdId;
    if (!holdId) {
        renderLog("RES-001 Reservation Create", "Invalid input", {
            error: "holdId를 입력해 주세요."
        });
        return;
    }

    setLoading(dom.createReservationButton, true);
    try {
        const result = await callApi({
            title: "RES-001 Reservation Create",
            endpoint: buildCreateEndpoint(),
            method: "POST",
            body: { holdId },
            accessTokenRequired: true
        });

        if (result?.ok && result.body?.reservationId) {
            setState({ holdId, reservationId: result.body.reservationId });
            await fetchReservationDetail(result.body.reservationId, { silent: true });
            await fetchMyReservations({ silent: true });
            if (state.selectedShowId != null) {
                await fetchAvailability(state.selectedShowId, { silent: true });
            }
        }
    } finally {
        setLoading(dom.createReservationButton, false);
    }
}

async function loadReservation() {
    const reservationId = dom.reservationIdInput.value.trim() || state.reservationId;
    if (!reservationId) {
        renderLog("RES-002 Reservation Detail", "Invalid input", {
            error: "reservationId를 입력해 주세요."
        });
        return;
    }

    setLoading(dom.getReservationButton, true);
    try {
        const result = await fetchReservationDetail(reservationId);
        if (result?.ok) {
            await fetchMyReservations({ silent: true });
            if (state.selectedShowId != null) {
                await fetchAvailability(state.selectedShowId, { silent: true });
            }
        }
    } finally {
        setLoading(dom.getReservationButton, false);
    }
}

async function loadMyReservations() {
    setLoading(dom.listReservationsButton, true);
    try {
        const result = await fetchMyReservations();
        if (result?.ok && state.reservationId && state.selectedShowId != null) {
            await fetchAvailability(state.selectedShowId, { silent: true });
        }
    } finally {
        setLoading(dom.listReservationsButton, false);
    }
}

async function loadAvailability() {
    const showId = state.selectedShowId;
    if (showId == null) {
        renderLog("SHW-001 Show Availability", "Invalid state", {
            error: "reservation 상세를 먼저 조회해 showId를 확보해 주세요."
        });
        return;
    }

    setLoading(dom.loadAvailabilityButton, true);
    try {
        await fetchAvailability(showId);
    } finally {
        setLoading(dom.loadAvailabilityButton, false);
    }
}

function resetState({ preserveQuery = false } = {}) {
    localStorage.removeItem(STORAGE_KEY);
    state.accessToken = "";
    state.holdId = "";
    state.reservationId = "";
    state.selectedShowId = null;
    state.reservation = null;
    state.items = [];
    state.reservations = [];
    state.listPage = 0;
    state.listSize = 20;
    state.listStatus = "";
    state.totalElements = 0;
    state.totalPages = 0;
    state.seats = [];
    state.sections = [];

    dom.accessTokenInput.value = "";
    dom.holdIdInput.value = "";
    dom.reservationIdInput.value = "";
    dom.pageInput.value = "0";
    dom.sizeInput.value = "20";
    dom.statusFilterSelect.value = "";

    if (!preserveQuery) {
        updateQuery(null, "", "");
    }

    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Reservations API Test Console loaded"
    });

    syncView();
}

dom.saveTokenButton.addEventListener("click", () => {
    const token = dom.accessTokenInput.value.trim();
    if (!token) {
        renderLog("Reservations Console", "Invalid input", {
            error: "저장할 access token을 입력해 주세요."
        });
        return;
    }

    setState({ accessToken: token });
    renderLog("Reservations Console", "Token saved", {
        saved: true,
        message: "USER access token을 로컬 상태에 저장했습니다."
    });
});

dom.holdIdInput.addEventListener("input", syncEndpointPreview);
dom.reservationIdInput.addEventListener("input", syncEndpointPreview);
dom.pageInput.addEventListener("input", syncEndpointPreview);
dom.sizeInput.addEventListener("input", syncEndpointPreview);
dom.statusFilterSelect.addEventListener("change", syncEndpointPreview);
dom.createReservationButton.addEventListener("click", createReservation);
dom.getReservationButton.addEventListener("click", loadReservation);
dom.listReservationsButton.addEventListener("click", loadMyReservations);
dom.loadAvailabilityButton.addEventListener("click", loadAvailability);
dom.openHoldsButton.addEventListener("click", () => {
    const holdId = dom.holdIdInput.value.trim() || state.holdId;
    const showId = state.selectedShowId != null ? String(state.selectedShowId) : "";
    const params = new URLSearchParams();
    if (showId) {
        params.set("showId", showId);
    }
    if (holdId) {
        params.set("holdId", holdId);
    }
    const suffix = params.toString() ? `?${params.toString()}` : "";
    window.open(`${API_BASE}/holds-test.html${suffix}`, "_blank", "noopener,noreferrer");
});
dom.openShowsButton.addEventListener("click", () => {
    if (state.selectedShowId == null) {
        renderLog("Reservations Console", "Invalid state", {
            error: "showId를 확보하려면 reservation 상세를 먼저 조회해 주세요."
        });
        return;
    }

    window.open(`${API_BASE}/shows-test.html?showId=${encodeURIComponent(state.selectedShowId)}`, "_blank", "noopener,noreferrer");
});
dom.clearStateButton.addEventListener("click", () => {
    resetState();
});
dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Reservations API Test Console loaded"
    });
});
dom.reservationListView.addEventListener("click", async (event) => {
    const button = event.target.closest(".pick-reservation-button");
    if (!button) {
        return;
    }

    const reservationId = button.dataset.reservationId;
    if (!reservationId) {
        return;
    }

    dom.reservationIdInput.value = reservationId;
    syncEndpointPreview();
    await loadReservation();
});

(function init() {
    const saved = readStorage();
    if (saved) {
        Object.assign(state, saved);
    }

    const queryShowId = readPositiveQueryNumber("showId");
    const queryHoldId = readQueryText("holdId");
    const queryReservationId = readQueryText("reservationId");
    if (queryShowId) {
        state.selectedShowId = queryShowId;
    }
    if (queryHoldId) {
        state.holdId = queryHoldId;
    }
    if (queryReservationId) {
        state.reservationId = queryReservationId;
    }

    syncView();
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Reservations API Test Console loaded"
    });

    if (state.reservationId) {
        fetchReservationDetail(state.reservationId, { silent: true }).then((result) => {
            if (result?.ok && state.selectedShowId != null) {
                fetchAvailability(state.selectedShowId, { silent: true });
            }
        });
    } else if (state.selectedShowId != null) {
        fetchAvailability(state.selectedShowId, { silent: true });
    }

    if (state.accessToken) {
        fetchMyReservations({ silent: true });
    }
})();