const API_BASE = window.location.port === "9090" || window.location.port === ""
    ? ""
    : `${window.location.protocol}//${window.location.hostname}:9090`;

const STORAGE_KEY = "ticketinglab.payments.console.state";

const state = {
    accessToken: "",
    reservationId: "",
    amount: 0,
    idempotencyKey: "",
    paymentId: null,
    paymentStatus: "",
    reservationStatus: "",
    approvedAt: null,
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    tickets: []
};

const dom = {
    accessTokenInput: document.getElementById("access-token-input"),
    saveTokenButton: document.getElementById("save-token-button"),
    tokenStatusBadge: document.getElementById("token-status-badge"),
    reservationIdInput: document.getElementById("reservation-id-input"),
    amountInput: document.getElementById("amount-input"),
    idempotencyKeyInput: document.getElementById("idempotency-key-input"),
    confirmEndpointPreview: document.getElementById("confirm-endpoint-preview"),
    confirmPaymentButton: document.getElementById("confirm-payment-button"),
    generateKeyButton: document.getElementById("generate-key-button"),
    openReservationsButton: document.getElementById("open-reservations-button"),
    pageInput: document.getElementById("page-input"),
    sizeInput: document.getElementById("size-input"),
    ticketsEndpointPreview: document.getElementById("tickets-endpoint-preview"),
    paymentIdPreview: document.getElementById("payment-id-preview"),
    listTicketsButton: document.getElementById("list-tickets-button"),
    clearStateButton: document.getElementById("clear-state-button"),
    openAuthButton: document.getElementById("open-auth-button"),
    clearLogButton: document.getElementById("clear-log-button"),
    ticketListView: document.getElementById("ticket-list-view"),
    ticketsSummaryBadge: document.getElementById("tickets-summary-badge"),
    consoleStatus: document.getElementById("console-status"),
    stateTokenStatus: document.getElementById("state-token-status"),
    stateReservationId: document.getElementById("state-reservation-id"),
    stateAmount: document.getElementById("state-amount"),
    stateIdempotencyKey: document.getElementById("state-idempotency-key"),
    statePaymentId: document.getElementById("state-payment-id"),
    statePaymentStatus: document.getElementById("state-payment-status"),
    stateReservationStatus: document.getElementById("state-reservation-status"),
    stateTicketCount: document.getElementById("state-ticket-count"),
    stateApprovedAt: document.getElementById("state-approved-at"),
    statePaymentSnapshot: document.getElementById("state-payment-snapshot"),
    stateTicketsSnapshot: document.getElementById("state-tickets-snapshot"),
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
        reservationId: state.reservationId,
        amount: state.amount,
        idempotencyKey: state.idempotencyKey,
        paymentId: state.paymentId,
        paymentStatus: state.paymentStatus,
        reservationStatus: state.reservationStatus,
        approvedAt: state.approvedAt,
        page: state.page,
        size: state.size
    }));
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
}

function updateQuery(reservationId, amount) {
    const url = new URL(window.location.href);

    if (reservationId) {
        url.searchParams.set("reservationId", reservationId);
    } else {
        url.searchParams.delete("reservationId");
    }

    if (amount > 0) {
        url.searchParams.set("amount", String(amount));
    } else {
        url.searchParams.delete("amount");
    }

    window.history.replaceState({}, "", url);
}

function setState(patch) {
    Object.assign(state, patch);
    persistState();
    updateQuery(state.reservationId, state.amount);
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
        renderLog("Payments Console", "Missing token", {
            error: "USER access token을 먼저 입력해 주세요."
        });
        return null;
    }

    rememberAccessToken(token);
    return token;
}

async function callApi({ title, endpoint, method = "GET", body, accessTokenRequired = false, idempotencyKey, silent = false }) {
    const headers = { Accept: "application/json" };

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    if (idempotencyKey) {
        headers["Idempotency-Key"] = idempotencyKey;
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
}

function setLoading(button, loading) {
    button.disabled = loading;
}

function readPositiveNumber(input, label, { min = 0 } = {}) {
    const value = Number(input.value);
    if (!Number.isInteger(value) || value < min) {
        renderLog("Payments Console", "Invalid input", {
            error: `${label}는 ${min} 이상의 정수여야 합니다.`
        });
        return null;
    }
    return value;
}

function buildConfirmEndpoint() {
    return `${API_BASE}/api/payments/confirm`;
}

function buildTicketsEndpoint(page, size) {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", String(size));
    return `${API_BASE}/api/me/tickets?${params.toString()}`;
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

function paymentBadgeClass(status) {
    if (status === "APPROVED") {
        return "ready";
    }
    return "idle";
}

function ticketCardClass(type) {
    return type === "SEAT" ? "available" : "section-card";
}

function syncEndpointPreview() {
    const page = dom.pageInput.value.trim() || String(state.page);
    const size = dom.sizeInput.value.trim() || String(state.size);
    dom.confirmEndpointPreview.value = buildConfirmEndpoint().replace(API_BASE, "");
    dom.ticketsEndpointPreview.value = buildTicketsEndpoint(page, size).replace(API_BASE, "");
    dom.paymentIdPreview.value = state.paymentId != null ? String(state.paymentId) : "-";
}

function syncSummary() {
    const tokenSaved = Boolean(state.accessToken);
    const loaded = Boolean(state.paymentId || state.tickets.length);

    if (state.accessToken && dom.accessTokenInput.value.trim() !== state.accessToken) {
        dom.accessTokenInput.value = state.accessToken;
    }
    if (state.reservationId) {
        dom.reservationIdInput.value = state.reservationId;
    }
    if (state.amount > 0) {
        dom.amountInput.value = String(state.amount);
    }
    if (state.idempotencyKey) {
        dom.idempotencyKeyInput.value = state.idempotencyKey;
    }
    dom.pageInput.value = String(state.page);
    dom.sizeInput.value = String(state.size);

    dom.tokenStatusBadge.textContent = tokenSaved ? "TOKEN SAVED" : "TOKEN EMPTY";
    dom.tokenStatusBadge.className = `status ${tokenSaved ? "ready" : "idle"}`;
    dom.stateTokenStatus.textContent = tokenSaved ? "SAVED" : "EMPTY";
    dom.stateReservationId.textContent = state.reservationId || "-";
    dom.stateAmount.textContent = String(state.amount || 0);
    dom.stateIdempotencyKey.textContent = state.idempotencyKey || "-";
    dom.statePaymentId.textContent = state.paymentId != null ? String(state.paymentId) : "-";
    dom.statePaymentStatus.textContent = state.paymentStatus || "-";
    dom.stateReservationStatus.textContent = state.reservationStatus || "-";
    dom.stateTicketCount.textContent = String(state.tickets.length);
    dom.stateApprovedAt.textContent = formatDateTime(state.approvedAt);
    dom.statePaymentSnapshot.value = JSON.stringify({
        paymentId: state.paymentId,
        reservationId: state.reservationId,
        amount: state.amount,
        idempotencyKey: state.idempotencyKey,
        paymentStatus: state.paymentStatus,
        reservationStatus: state.reservationStatus,
        approvedAt: state.approvedAt
    }, null, 2);
    dom.stateTicketsSnapshot.value = JSON.stringify({
        page: state.page,
        size: state.size,
        totalElements: state.totalElements,
        totalPages: state.totalPages,
        tickets: state.tickets
    }, null, 2);

    dom.ticketsSummaryBadge.textContent = state.tickets.length
        ? `${state.totalElements} ticket${state.totalElements > 1 ? "s" : ""} / page ${state.page + 1}`
        : "No Tickets Loaded";
    dom.ticketsSummaryBadge.className = `status ${state.tickets.length ? paymentBadgeClass(state.paymentStatus) : "idle"}`;

    dom.consoleStatus.textContent = loaded ? "Loaded" : "Ready";
    dom.consoleStatus.className = `status ${loaded ? "ready" : "idle"}`;
}

function renderEmptyTickets() {
    dom.ticketListView.innerHTML = `
        <article class="empty-panel">
            <strong>아직 불러온 티켓이 없습니다.</strong>
            <p class="placeholder-copy">결제 확정 이후 내 티켓 조회를 실행하면 이 영역에 발급된 seat/section 티켓이 표시됩니다.</p>
        </article>
    `;
}

function renderTickets() {
    if (!state.tickets.length) {
        renderEmptyTickets();
        return;
    }

    dom.ticketListView.innerHTML = state.tickets.map((ticket) => `
        <article class="inventory-card ${ticketCardClass(ticket.type)}">
            <div class="inventory-head">
                <div>
                    <h3>${escapeHtml(ticket.ticketId)}</h3>
                    <p class="muted">reservationId ${escapeHtml(ticket.reservationId)}</p>
                </div>
                <span class="status-token ${ticket.type === "SEAT" ? "available" : "section-remaining"}">${escapeHtml(ticket.type)}</span>
            </div>
            <div class="token-row">
                <span class="token">serial ${escapeHtml(ticket.serial)}</span>
                <span class="token">status ${escapeHtml(ticket.status)}</span>
                <span class="token">showId ${escapeHtml(ticket.showId)}</span>
                <span class="token">${ticket.type === "SEAT"
                    ? `seatId ${escapeHtml(ticket.seatId)}`
                    : `sectionId ${escapeHtml(ticket.sectionId)}`}</span>
                <span class="token">createdAt ${escapeHtml(formatDateTime(ticket.createdAt))}</span>
            </div>
        </article>
    `).join("");
}

function syncView() {
    syncEndpointPreview();
    syncSummary();
    renderTickets();
}

async function fetchTickets({ silent = false } = {}) {
    const page = readPositiveNumber(dom.pageInput, "Page", { min: 0 });
    const size = readPositiveNumber(dom.sizeInput, "Size", { min: 1 });
    if (page == null || size == null) {
        return null;
    }

    const result = await callApi({
        title: "TKT-001 My Tickets",
        endpoint: buildTicketsEndpoint(page, size),
        accessTokenRequired: true,
        silent
    });

    if (result?.ok && result.body) {
        setState({
            page: result.body.page ?? page,
            size: result.body.size ?? size,
            totalElements: result.body.totalElements ?? 0,
            totalPages: result.body.totalPages ?? 0,
            tickets: Array.isArray(result.body.tickets) ? result.body.tickets : []
        });
    }

    return result;
}

async function confirmPayment() {
    const reservationId = dom.reservationIdInput.value.trim() || state.reservationId;
    const amount = readPositiveNumber(dom.amountInput, "Amount", { min: 1 });
    const idempotencyKey = dom.idempotencyKeyInput.value.trim() || state.idempotencyKey;

    if (!reservationId) {
        renderLog("PAY-001 Payment Confirm", "Invalid input", {
            error: "reservationId를 입력해 주세요."
        });
        return;
    }
    if (amount == null) {
        return;
    }
    if (!idempotencyKey) {
        renderLog("PAY-001 Payment Confirm", "Invalid input", {
            error: "Idempotency-Key를 입력하거나 생성해 주세요."
        });
        return;
    }

    setLoading(dom.confirmPaymentButton, true);
    try {
        const result = await callApi({
            title: "PAY-001 Payment Confirm",
            endpoint: buildConfirmEndpoint(),
            method: "POST",
            body: { reservationId, amount },
            accessTokenRequired: true,
            idempotencyKey
        });

        if (result?.ok && result.body) {
            setState({
                reservationId,
                amount,
                idempotencyKey,
                paymentId: result.body.paymentId ?? null,
                paymentStatus: result.body.status ?? "",
                reservationStatus: result.body.reservationStatus ?? "",
                approvedAt: result.body.approvedAt ?? null
            });
            await fetchTickets({ silent: true });
        }
    } finally {
        setLoading(dom.confirmPaymentButton, false);
    }
}

function createRandomKey() {
    const idempotencyKey = window.crypto?.randomUUID ? window.crypto.randomUUID() : `idem-${Date.now()}`;
    dom.idempotencyKeyInput.value = idempotencyKey;
    setState({ idempotencyKey });
    renderLog("Payments Console", "Idempotency key generated", {
        idempotencyKey
    });
}

function resetState({ preserveQuery = false } = {}) {
    localStorage.removeItem(STORAGE_KEY);
    state.accessToken = "";
    state.reservationId = "";
    state.amount = 0;
    state.idempotencyKey = "";
    state.paymentId = null;
    state.paymentStatus = "";
    state.reservationStatus = "";
    state.approvedAt = null;
    state.page = 0;
    state.size = 20;
    state.totalElements = 0;
    state.totalPages = 0;
    state.tickets = [];

    dom.accessTokenInput.value = "";
    dom.reservationIdInput.value = "";
    dom.amountInput.value = "0";
    dom.idempotencyKeyInput.value = "";
    dom.pageInput.value = "0";
    dom.sizeInput.value = "20";

    if (!preserveQuery) {
        updateQuery("", 0);
    }

    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Payments API Test Console loaded"
    });
    syncView();
}

dom.saveTokenButton.addEventListener("click", () => {
    const token = dom.accessTokenInput.value.trim();
    if (!token) {
        renderLog("Payments Console", "Invalid input", {
            error: "저장할 access token을 입력해 주세요."
        });
        return;
    }

    setState({ accessToken: token });
    renderLog("Payments Console", "Token saved", {
        saved: true,
        message: "USER access token을 로컬 상태에 저장했습니다."
    });
});

dom.reservationIdInput.addEventListener("input", syncEndpointPreview);
dom.amountInput.addEventListener("input", syncEndpointPreview);
dom.idempotencyKeyInput.addEventListener("input", syncEndpointPreview);
dom.pageInput.addEventListener("input", syncEndpointPreview);
dom.sizeInput.addEventListener("input", syncEndpointPreview);
dom.confirmPaymentButton.addEventListener("click", confirmPayment);
dom.generateKeyButton.addEventListener("click", createRandomKey);
dom.listTicketsButton.addEventListener("click", async () => {
    setLoading(dom.listTicketsButton, true);
    try {
        await fetchTickets();
    } finally {
        setLoading(dom.listTicketsButton, false);
    }
});
dom.openReservationsButton.addEventListener("click", () => {
    const reservationId = dom.reservationIdInput.value.trim() || state.reservationId;
    const params = new URLSearchParams();
    if (reservationId) {
        params.set("reservationId", reservationId);
    }
    const suffix = params.toString() ? `?${params.toString()}` : "";
    window.open(`${API_BASE}/reservations-test.html${suffix}`, "_blank", "noopener,noreferrer");
});
dom.clearStateButton.addEventListener("click", () => {
    resetState();
});
dom.openAuthButton.addEventListener("click", () => {
    window.open(`${API_BASE}/auth-test.html`, "_blank", "noopener,noreferrer");
});
dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Payments API Test Console loaded"
    });
});

(function init() {
    const saved = readStorage();
    if (saved) {
        Object.assign(state, saved);
    }

    const queryReservationId = readQueryText("reservationId");
    const queryAmount = readPositiveQueryNumber("amount");
    if (queryReservationId) {
        state.reservationId = queryReservationId;
    }
    if (queryAmount) {
        state.amount = queryAmount;
    }

    syncView();
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Payments API Test Console loaded"
    });

    if (state.accessToken) {
        fetchTickets({ silent: true });
    }
})();