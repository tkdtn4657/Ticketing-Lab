const API_BASE = window.location.port === "9090" || window.location.port === ""
    ? ""
    : `${window.location.protocol}//${window.location.hostname}:9090`;

const STORAGE_KEY = "ticketinglab.holds.console.state";
const defaultHoldItems = JSON.stringify([
    { seatId: 1 },
    { sectionId: 1, qty: 2 }
], null, 2);

const state = {
    accessToken: "",
    selectedShowId: null,
    holdId: "",
    hold: null,
    items: [],
    seats: [],
    sections: []
};

const dom = {
    accessTokenInput: document.getElementById("access-token-input"),
    saveTokenButton: document.getElementById("save-token-button"),
    tokenStatusBadge: document.getElementById("token-status-badge"),
    showIdInput: document.getElementById("show-id-input"),
    createEndpointPreview: document.getElementById("create-endpoint-preview"),
    holdItemsInput: document.getElementById("hold-items-input"),
    createHoldButton: document.getElementById("create-hold-button"),
    holdIdInput: document.getElementById("hold-id-input"),
    detailEndpointPreview: document.getElementById("detail-endpoint-preview"),
    deleteEndpointPreview: document.getElementById("delete-endpoint-preview"),
    loadAvailabilityButton: document.getElementById("load-availability-button"),
    getHoldButton: document.getElementById("get-hold-button"),
    cancelHoldButton: document.getElementById("cancel-hold-button"),
    openShowsButton: document.getElementById("open-shows-button"),
    openReservationsButton: document.getElementById("open-reservations-button"),
    clearStateButton: document.getElementById("clear-state-button"),
    clearLogButton: document.getElementById("clear-log-button"),
    holdItemListView: document.getElementById("hold-item-list-view"),
    seatListView: document.getElementById("seat-list-view"),
    sectionListView: document.getElementById("section-list-view"),
    holdSummaryBadge: document.getElementById("hold-summary-badge"),
    seatsSummaryBadge: document.getElementById("seats-summary-badge"),
    sectionsSummaryBadge: document.getElementById("sections-summary-badge"),
    consoleStatus: document.getElementById("console-status"),
    stateTokenStatus: document.getElementById("state-token-status"),
    stateShowId: document.getElementById("state-show-id"),
    stateHoldId: document.getElementById("state-hold-id"),
    stateHoldStatus: document.getElementById("state-hold-status"),
    stateItemCount: document.getElementById("state-item-count"),
    stateExpiresAt: document.getElementById("state-expires-at"),
    stateCreatedAt: document.getElementById("state-created-at"),
    stateHoldSnapshot: document.getElementById("state-hold-snapshot"),
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
        selectedShowId: state.selectedShowId,
        holdId: state.holdId
    }));
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
}

function setState(patch) {
    Object.assign(state, patch);
    persistState();
    updateQuery(state.selectedShowId, state.holdId);
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
        renderLog("Holds Console", "Missing token", {
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

function parseJsonInput(value, label) {
    try {
        return JSON.parse(value);
    } catch (error) {
        renderLog("Holds Console", "Invalid JSON", {
            error: `${label} JSON 파싱에 실패했습니다.`,
            detail: error.message
        });
        return null;
    }
}

function buildCreateEndpoint() {
    return `${API_BASE}/api/holds`;
}

function buildDetailEndpoint(holdId) {
    return holdId
        ? `${API_BASE}/api/holds/${holdId}`
        : `${API_BASE}/api/holds/{holdId}`;
}

function buildAvailabilityEndpoint(showId) {
    return showId
        ? `${API_BASE}/api/shows/${showId}/availability`
        : `${API_BASE}/api/shows/{showId}/availability`;
}

function readPositiveNumber(input, label) {
    const value = Number(input.value);
    if (!Number.isInteger(value) || value < 1) {
        renderLog("Holds Console", "Invalid input", {
            error: `${label}는 1 이상의 정수여야 합니다.`
        });
        return null;
    }
    return value;
}

function readShowIdFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const value = Number(params.get("showId"));
    return Number.isInteger(value) && value > 0 ? value : null;
}

function readHoldIdFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const value = params.get("holdId");
    return value ? value : "";
}

function updateQuery(showId, holdId) {
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

    window.history.replaceState({}, "", url);
}

function statusToBadge(status) {
    if (status === "ACTIVE") {
        return "ready";
    }
    if (status === "EXPIRED") {
        return "warn";
    }
    if (status === "CANCELED") {
        return "danger";
    }
    return "idle";
}

function syncEndpointPreview() {
    const holdId = dom.holdIdInput.value.trim() || state.holdId;
    dom.createEndpointPreview.value = buildCreateEndpoint();
    dom.detailEndpointPreview.value = buildDetailEndpoint(holdId);
    dom.deleteEndpointPreview.value = buildDetailEndpoint(holdId);
}

function syncSummary() {
    const hold = state.hold;
    const tokenSaved = Boolean(state.accessToken);
    const loaded = Boolean(hold || state.seats.length || state.sections.length);

    if (state.selectedShowId != null) {
        dom.showIdInput.value = String(state.selectedShowId);
    }
    if (state.holdId) {
        dom.holdIdInput.value = state.holdId;
    }
    if (state.accessToken && dom.accessTokenInput.value.trim() !== state.accessToken) {
        dom.accessTokenInput.value = state.accessToken;
    }

    dom.tokenStatusBadge.textContent = tokenSaved ? "TOKEN SAVED" : "TOKEN EMPTY";
    dom.tokenStatusBadge.className = `status ${tokenSaved ? "ready" : "idle"}`;
    dom.stateTokenStatus.textContent = tokenSaved ? "SAVED" : "EMPTY";
    dom.stateShowId.textContent = state.selectedShowId != null ? String(state.selectedShowId) : "-";
    dom.stateHoldId.textContent = state.holdId || "-";
    dom.stateHoldStatus.textContent = hold?.status || "-";
    dom.stateItemCount.textContent = String(state.items.length);
    dom.stateExpiresAt.textContent = formatDateTime(hold?.expiresAt);
    dom.stateCreatedAt.textContent = formatDateTime(hold?.createdAt);
    dom.stateHoldSnapshot.value = JSON.stringify({ hold: state.hold, items: state.items }, null, 2);
    dom.stateAvailabilitySnapshot.value = JSON.stringify({
        showId: state.selectedShowId,
        seats: state.seats,
        sections: state.sections
    }, null, 2);

    dom.holdSummaryBadge.textContent = hold
        ? `${hold.status} / ${state.items.length} item${state.items.length > 1 ? "s" : ""}`
        : "No Hold Loaded";
    dom.holdSummaryBadge.className = `status ${hold ? statusToBadge(hold.status) : "idle"}`;

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

function renderEmptyHoldItems() {
    dom.holdItemListView.innerHTML = `
        <article class="empty-panel">
            <strong>아직 조회된 홀드가 없습니다.</strong>
            <p class="placeholder-copy">홀드를 생성하거나 holdId로 조회하면 이 영역에 seat/section item과 단가 정보가 표시됩니다.</p>
        </article>
    `;
}

function renderHoldItems() {
    if (!state.items.length) {
        renderEmptyHoldItems();
        return;
    }

    dom.holdItemListView.innerHTML = state.items.map((item) => {
        const isSeat = item.type === "SEAT";
        const targetId = isSeat ? item.seatId : item.sectionId;
        return `
            <article class="inventory-card hold-item-card ${isSeat ? "seat-card" : "section-card"}">
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
                    ${isSeat ? `<span class="token">seatId ${escapeHtml(item.seatId)}</span>` : `<span class="token">sectionId ${escapeHtml(item.sectionId)}</span>`}
                </div>
            </article>
        `;
    }).join("");
}

function renderEmptySeatList() {
    dom.seatListView.innerHTML = `
        <article class="empty-panel">
            <strong>불러온 좌석 정보가 없습니다.</strong>
            <p class="placeholder-copy">가용성 조회를 실행하면 이 영역에 좌석별 available 상태가 표시됩니다.</p>
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
                <span class="status-token ${seat.available ? "available" : "unavailable"}">${seat.available ? "AVAILABLE" : "HELD"}</span>
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
            <p class="placeholder-copy">가용성 조회를 실행하면 이 영역에 구역별 remainingQty가 표시됩니다.</p>
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
    renderHoldItems();
    renderSeatList();
    renderSectionList();
}

async function fetchHoldDetail(holdId, { silent = false } = {}) {
    const result = await callApi({
        title: "HLD-002 Hold Detail",
        endpoint: buildDetailEndpoint(holdId),
        accessTokenRequired: true,
        silent
    });

    if (result?.ok && result.body) {
        setState({
            holdId,
            selectedShowId: result.body.hold?.showId ?? state.selectedShowId,
            hold: result.body.hold ?? null,
            items: Array.isArray(result.body.items) ? result.body.items : []
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

async function createHold() {
    const showId = readPositiveNumber(dom.showIdInput, "Show ID");
    if (!showId) {
        return;
    }

    const items = parseJsonInput(dom.holdItemsInput.value, "Hold Items");
    if (!Array.isArray(items)) {
        renderLog("HLD-001 Hold Create", "Invalid input", {
            error: "items는 JSON 배열이어야 합니다."
        });
        return;
    }

    setLoading(dom.createHoldButton, true);
    try {
        const result = await callApi({
            title: "HLD-001 Hold Create",
            endpoint: buildCreateEndpoint(),
            method: "POST",
            body: { showId, items },
            accessTokenRequired: true
        });

        if (result?.ok && result.body?.holdId) {
            setState({ selectedShowId: showId, holdId: result.body.holdId });
            await fetchHoldDetail(result.body.holdId, { silent: true });
            await fetchAvailability(showId, { silent: true });
        }
    } finally {
        setLoading(dom.createHoldButton, false);
    }
}

async function loadHold() {
    const holdId = dom.holdIdInput.value.trim() || state.holdId;
    if (!holdId) {
        renderLog("HLD-002 Hold Detail", "Invalid input", {
            error: "holdId를 입력해 주세요."
        });
        return;
    }

    setLoading(dom.getHoldButton, true);
    try {
        const result = await fetchHoldDetail(holdId);
        if (result?.ok && state.selectedShowId != null) {
            await fetchAvailability(state.selectedShowId, { silent: true });
        }
    } finally {
        setLoading(dom.getHoldButton, false);
    }
}

async function cancelHold() {
    const holdId = dom.holdIdInput.value.trim() || state.holdId;
    if (!holdId) {
        renderLog("HLD-003 Hold Cancel", "Invalid input", {
            error: "holdId를 입력해 주세요."
        });
        return;
    }

    setLoading(dom.cancelHoldButton, true);
    try {
        const result = await callApi({
            title: "HLD-003 Hold Cancel",
            endpoint: buildDetailEndpoint(holdId),
            method: "DELETE",
            accessTokenRequired: true
        });

        if (result?.ok) {
            await fetchHoldDetail(holdId, { silent: true });
            if (state.selectedShowId != null) {
                await fetchAvailability(state.selectedShowId, { silent: true });
            }
        }
    } finally {
        setLoading(dom.cancelHoldButton, false);
    }
}

function resetState({ preserveQuery = false } = {}) {
    localStorage.removeItem(STORAGE_KEY);
    state.accessToken = "";
    state.selectedShowId = null;
    state.holdId = "";
    state.hold = null;
    state.items = [];
    state.seats = [];
    state.sections = [];

    dom.accessTokenInput.value = "";
    dom.showIdInput.value = "";
    dom.holdIdInput.value = "";
    dom.holdItemsInput.value = defaultHoldItems;

    if (!preserveQuery) {
        updateQuery(null, "");
    }

    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Holds API Test Console loaded"
    });

    syncView();
}

dom.saveTokenButton.addEventListener("click", () => {
    const token = dom.accessTokenInput.value.trim();
    if (!token) {
        renderLog("Holds Console", "Invalid input", {
            error: "저장할 access token을 입력해 주세요."
        });
        return;
    }

    setState({ accessToken: token });
    renderLog("Holds Console", "Token saved", {
        saved: true,
        message: "USER access token을 로컬 상태에 저장했습니다."
    });
});

dom.showIdInput.addEventListener("input", syncEndpointPreview);
dom.holdIdInput.addEventListener("input", syncEndpointPreview);
dom.createHoldButton.addEventListener("click", createHold);
dom.loadAvailabilityButton.addEventListener("click", async () => {
    const showId = readPositiveNumber(dom.showIdInput, "Show ID");
    if (!showId) {
        return;
    }

    setLoading(dom.loadAvailabilityButton, true);
    try {
        await fetchAvailability(showId);
    } finally {
        setLoading(dom.loadAvailabilityButton, false);
    }
});
dom.getHoldButton.addEventListener("click", loadHold);
dom.cancelHoldButton.addEventListener("click", cancelHold);
dom.openShowsButton.addEventListener("click", () => {
    const showId = dom.showIdInput.value.trim() || (state.selectedShowId != null ? String(state.selectedShowId) : "");
    if (!showId) {
        renderLog("Holds Console", "Invalid input", {
            error: "showId를 먼저 입력하거나 홀드를 조회해 주세요."
        });
        return;
    }

    window.open(`${API_BASE}/shows-test.html?showId=${encodeURIComponent(showId)}`, "_blank", "noopener,noreferrer");
});
dom.openReservationsButton.addEventListener("click", () => {
    const showId = dom.showIdInput.value.trim() || (state.selectedShowId != null ? String(state.selectedShowId) : "");
    const holdId = dom.holdIdInput.value.trim() || state.holdId;
    if (!showId && !holdId) {
        renderLog("Holds Console", "Invalid input", {
            error: "showId나 holdId를 먼저 입력하거나 홀드를 조회해 주세요."
        });
        return;
    }

    const params = new URLSearchParams();
    if (showId) {
        params.set("showId", showId);
    }
    if (holdId) {
        params.set("holdId", holdId);
    }

    window.open(`${API_BASE}/reservations-test.html?${params.toString()}`, "_blank", "noopener,noreferrer");
});
dom.clearStateButton.addEventListener("click", () => {
    resetState();
});
dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Holds API Test Console loaded"
    });
});

(function init() {
    const saved = readStorage();
    if (saved) {
        Object.assign(state, saved);
    }

    dom.holdItemsInput.value = defaultHoldItems;

    const queryShowId = readShowIdFromQuery();
    const queryHoldId = readHoldIdFromQuery();
    if (queryShowId) {
        state.selectedShowId = queryShowId;
    }
    if (queryHoldId) {
        state.holdId = queryHoldId;
    }

    syncView();
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Holds API Test Console loaded"
    });

    if (state.holdId) {
        fetchHoldDetail(state.holdId, { silent: true });
    }
    if (state.selectedShowId != null) {
        fetchAvailability(state.selectedShowId, { silent: true });
    }
})();
