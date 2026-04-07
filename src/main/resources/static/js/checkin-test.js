const API_BASE = window.location.port === "9090" || window.location.port === ""
    ? ""
    : `${window.location.protocol}//${window.location.hostname}:9090`;

const STORAGE_KEY = "ticketinglab.checkin.console.state";

const state = {
    accessToken: "",
    qrToken: "",
    ticketId: "",
    reservationId: "",
    showId: null,
    reservationItemId: null,
    type: "",
    seatId: null,
    sectionId: null,
    serial: "",
    status: "",
    usedAt: null
};

const dom = {
    accessTokenInput: document.getElementById("access-token-input"),
    saveTokenButton: document.getElementById("save-token-button"),
    tokenStatusBadge: document.getElementById("token-status-badge"),
    qrTokenInput: document.getElementById("qr-token-input"),
    checkinEndpointPreview: document.getElementById("checkin-endpoint-preview"),
    checkinButton: document.getElementById("checkin-button"),
    openAdminButton: document.getElementById("open-admin-button"),
    openPaymentsButton: document.getElementById("open-payments-button"),
    checkinSummaryBadge: document.getElementById("checkin-summary-badge"),
    checkinTicketView: document.getElementById("checkin-ticket-view"),
    openAuthButton: document.getElementById("open-auth-button"),
    clearStateButton: document.getElementById("clear-state-button"),
    clearLogButton: document.getElementById("clear-log-button"),
    consoleStatus: document.getElementById("console-status"),
    stateTokenStatus: document.getElementById("state-token-status"),
    stateQrToken: document.getElementById("state-qr-token"),
    stateTicketId: document.getElementById("state-ticket-id"),
    stateReservationId: document.getElementById("state-reservation-id"),
    stateShowId: document.getElementById("state-show-id"),
    stateType: document.getElementById("state-type"),
    stateResource: document.getElementById("state-resource"),
    stateStatus: document.getElementById("state-status"),
    stateUsedAt: document.getElementById("state-used-at"),
    stateSerial: document.getElementById("state-serial"),
    stateCheckinSnapshot: document.getElementById("state-checkin-snapshot"),
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
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function updateQuery(qrToken) {
    const url = new URL(window.location.href);
    if (qrToken) {
        url.searchParams.set("qrToken", qrToken);
    } else {
        url.searchParams.delete("qrToken");
    }
    window.history.replaceState({}, "", url);
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
}

function setState(patch) {
    Object.assign(state, patch);
    persistState();
    updateQuery(state.qrToken);
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
        renderLog("Check-in Console", "Missing token", {
            error: "ADMIN access token을 먼저 입력해 주세요."
        });
        return null;
    }

    rememberAccessToken(token);
    return token;
}

async function callApi({ title, endpoint, method = "GET", body, accessTokenRequired = false }) {
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

    renderLog(title, `Request started at ${nowText()}`, {
        request: {
            method,
            endpoint,
            headers,
            body
        }
    });

    const response = await fetch(endpoint, {
        method,
        headers,
        credentials: API_BASE ? "omit" : "same-origin",
        body: body !== undefined ? JSON.stringify(body) : undefined
    });

    const bodyText = await response.text();
    const responseBody = parseBody(bodyText);
    const responseHeaders = collectHeaders(response);

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
        body: responseBody,
        headers: responseHeaders
    };
}

function buildCheckinEndpoint() {
    return `${API_BASE}/api/checkin`;
}

function setLoading(button, loading) {
    button.disabled = loading;
}

function resourceText(ticket) {
    if (ticket.type === "SEAT") {
        return ticket.seatId != null ? `seatId ${ticket.seatId}` : "seatId -";
    }
    return ticket.sectionId != null ? `sectionId ${ticket.sectionId}` : "sectionId -";
}

function renderEmptyTicket() {
    dom.checkinTicketView.innerHTML = `
        <article class="empty-panel">
            <strong>아직 체크인한 티켓이 없습니다.</strong>
            <p class="placeholder-copy">qrToken을 입력하고 체크인을 실행하면 최근 처리된 티켓이 이 영역에 표시됩니다.</p>
        </article>
    `;
}

function renderTicket() {
    if (!state.ticketId) {
        renderEmptyTicket();
        return;
    }

    dom.checkinTicketView.innerHTML = `
        <article class="inventory-card available">
            <div class="inventory-head">
                <div>
                    <h3>${escapeHtml(state.ticketId)}</h3>
                    <p class="muted">reservationId ${escapeHtml(state.reservationId)}</p>
                </div>
                <span class="status-token available">${escapeHtml(state.status || "USED")}</span>
            </div>
            <div class="token-row">
                <span class="token">type ${escapeHtml(state.type)}</span>
                <span class="token">${escapeHtml(resourceText(state))}</span>
                <span class="token">showId ${escapeHtml(state.showId)}</span>
                <span class="token">serial ${escapeHtml(state.serial)}</span>
                <span class="token">usedAt ${escapeHtml(formatDateTime(state.usedAt))}</span>
            </div>
            <pre>${escapeHtml(JSON.stringify({
                qrToken: state.qrToken,
                reservationItemId: state.reservationItemId,
                status: state.status,
                usedAt: state.usedAt
            }, null, 2))}</pre>
        </article>
    `;
}

function syncSummary() {
    const tokenSaved = Boolean(state.accessToken);
    const loaded = Boolean(state.ticketId);

    if (state.accessToken && dom.accessTokenInput.value.trim() !== state.accessToken) {
        dom.accessTokenInput.value = state.accessToken;
    }
    if (state.qrToken) {
        dom.qrTokenInput.value = state.qrToken;
    }

    dom.checkinEndpointPreview.value = buildCheckinEndpoint().replace(API_BASE, "");
    dom.tokenStatusBadge.textContent = tokenSaved ? "TOKEN SAVED" : "TOKEN EMPTY";
    dom.tokenStatusBadge.className = `status ${tokenSaved ? "ready" : "idle"}`;
    dom.checkinSummaryBadge.textContent = loaded ? `${state.status} / ${state.type}` : "No Check-in Yet";
    dom.checkinSummaryBadge.className = `status ${loaded ? "ready" : "idle"}`;
    dom.consoleStatus.textContent = loaded ? "Checked In" : "Ready";
    dom.consoleStatus.className = `status ${loaded ? "ready" : "idle"}`;

    dom.stateTokenStatus.textContent = tokenSaved ? "SAVED" : "EMPTY";
    dom.stateQrToken.textContent = state.qrToken || "-";
    dom.stateTicketId.textContent = state.ticketId || "-";
    dom.stateReservationId.textContent = state.reservationId || "-";
    dom.stateShowId.textContent = state.showId != null ? String(state.showId) : "-";
    dom.stateType.textContent = state.type || "-";
    dom.stateResource.textContent = state.ticketId ? resourceText(state) : "-";
    dom.stateStatus.textContent = state.status || "-";
    dom.stateUsedAt.textContent = formatDateTime(state.usedAt);
    dom.stateSerial.textContent = state.serial || "-";
    dom.stateCheckinSnapshot.value = JSON.stringify(state, null, 2);
}

function syncView() {
    syncSummary();
    renderTicket();
}

function readQueryText(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name) || "";
}

async function checkinTicket() {
    const qrToken = dom.qrTokenInput.value.trim() || state.qrToken;
    if (!qrToken) {
        renderLog("CHK-001 Check-in", "Invalid input", {
            error: "qrToken을 입력해 주세요."
        });
        return;
    }

    setLoading(dom.checkinButton, true);
    try {
        const result = await callApi({
            title: "CHK-001 Check-in",
            endpoint: buildCheckinEndpoint(),
            method: "POST",
            body: { qrToken },
            accessTokenRequired: true
        });

        if (result?.ok && result.body) {
            setState({
                qrToken,
                ticketId: result.body.ticketId ?? "",
                reservationId: result.body.reservationId ?? "",
                showId: result.body.showId ?? null,
                reservationItemId: result.body.reservationItemId ?? null,
                type: result.body.type ?? "",
                seatId: result.body.seatId ?? null,
                sectionId: result.body.sectionId ?? null,
                serial: result.body.serial ?? "",
                status: result.body.status ?? "",
                usedAt: result.body.usedAt ?? null
            });
        }
    } finally {
        setLoading(dom.checkinButton, false);
    }
}

function resetState() {
    localStorage.removeItem(STORAGE_KEY);
    state.accessToken = "";
    state.qrToken = "";
    state.ticketId = "";
    state.reservationId = "";
    state.showId = null;
    state.reservationItemId = null;
    state.type = "";
    state.seatId = null;
    state.sectionId = null;
    state.serial = "";
    state.status = "";
    state.usedAt = null;

    dom.accessTokenInput.value = "";
    dom.qrTokenInput.value = "";

    updateQuery("");
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Check-in API Test Console loaded"
    });
    syncView();
}

dom.saveTokenButton.addEventListener("click", () => {
    const token = dom.accessTokenInput.value.trim();
    if (!token) {
        renderLog("Check-in Console", "Invalid input", {
            error: "저장할 ADMIN access token을 입력해 주세요."
        });
        return;
    }

    setState({ accessToken: token });
    renderLog("Check-in Console", "Token saved", {
        saved: true,
        message: "ADMIN access token을 로컬 상태에 저장했습니다."
    });
});

dom.checkinButton.addEventListener("click", checkinTicket);
dom.openAdminButton.addEventListener("click", () => {
    window.open(`${API_BASE}/admin-test.html`, "_blank", "noopener,noreferrer");
});
dom.openPaymentsButton.addEventListener("click", () => {
    const qrToken = dom.qrTokenInput.value.trim() || state.qrToken;
    const suffix = qrToken ? `?qrToken=${encodeURIComponent(qrToken)}` : "";
    window.open(`${API_BASE}/payments-test.html${suffix}`, "_blank", "noopener,noreferrer");
});
dom.openAuthButton.addEventListener("click", () => {
    window.open(`${API_BASE}/auth-test.html`, "_blank", "noopener,noreferrer");
});
dom.clearStateButton.addEventListener("click", resetState);
dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Check-in API Test Console loaded"
    });
});

(function init() {
    const saved = readStorage();
    if (saved) {
        Object.assign(state, saved);
    }

    const queryQrToken = readQueryText("qrToken");
    if (queryQrToken) {
        state.qrToken = queryQrToken;
    }

    syncView();
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Check-in API Test Console loaded"
    });
})();
