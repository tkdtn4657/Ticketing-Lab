const API_BASE = window.location.port === "9090"
    ? ""
    : `${window.location.protocol}//${window.location.hostname}:9090`;

const state = {
    filterStatus: "",
    events: [],
    selectedEventId: null,
    selectedEvent: null,
    selectedShows: []
};

const dom = {
    listForm: document.getElementById("list-form"),
    detailForm: document.getElementById("detail-form"),
    statusFilter: document.getElementById("status-filter"),
    listEndpointPreview: document.getElementById("list-endpoint-preview"),
    detailEndpointPreview: document.getElementById("detail-endpoint-preview"),
    eventIdInput: document.getElementById("event-id-input"),
    loadEventsButton: document.getElementById("load-events-button"),
    loadEventDetailButton: document.getElementById("load-event-detail-button"),
    clearStateButton: document.getElementById("clear-state-button"),
    clearLogButton: document.getElementById("clear-log-button"),
    eventListView: document.getElementById("event-list-view"),
    eventDetailView: document.getElementById("event-detail-view"),
    listSummaryBadge: document.getElementById("list-summary-badge"),
    detailSummaryBadge: document.getElementById("detail-summary-badge"),
    consoleStatus: document.getElementById("console-status"),
    stateFilter: document.getElementById("state-filter"),
    stateListCount: document.getElementById("state-list-count"),
    stateSelectedId: document.getElementById("state-selected-id"),
    stateSelectedTitle: document.getElementById("state-selected-title"),
    stateEventsSnapshot: document.getElementById("state-events-snapshot"),
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

function statusClass(status) {
    return String(status || "").toLowerCase().replace(/[^a-z0-9]+/g, "-");
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
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

async function callApi({ title, endpoint, method = "GET" }) {
    const headers = { Accept: "application/json" };

    renderLog(title, `Request started at ${nowText()}`, {
        request: {
            method,
            endpoint,
            headers
        }
    });

    try {
        const response = await fetch(endpoint, {
            method,
            headers,
            credentials: API_BASE ? "omit" : "same-origin"
        });

        const bodyText = await response.text();
        const body = parseBody(bodyText);
        const responseHeaders = {};
        response.headers.forEach((value, key) => {
            responseHeaders[key] = value;
        });

        renderLog(title, `${response.status} ${response.statusText}`, {
            request: {
                method,
                endpoint,
                headers
            },
            response: {
                status: response.status,
                statusText: response.statusText,
                headers: responseHeaders,
                body
            }
        });

        return {
            ok: response.ok,
            status: response.status,
            body
        };
    } catch (error) {
        renderLog(title, "Network error", { error: error.message });
        throw error;
    }
}

function setLoading(button, loading) {
    button.disabled = loading;
}

function buildListEndpoint(status) {
    return status
        ? `${API_BASE}/api/events?status=${encodeURIComponent(status)}`
        : `${API_BASE}/api/events`;
}

function buildDetailEndpoint(eventId) {
    return eventId
        ? `${API_BASE}/api/events/${eventId}`
        : `${API_BASE}/api/events/{eventId}`;
}

function syncEndpointPreview() {
    const status = dom.statusFilter.value.trim();
    dom.listEndpointPreview.value = buildListEndpoint(status);

    const eventId = dom.eventIdInput.value.trim();
    dom.detailEndpointPreview.value = buildDetailEndpoint(eventId);
}

function syncSummary() {
    const selectedTitle = state.selectedEvent ? state.selectedEvent.title : "-";
    const listLoaded = state.events.length > 0;
    const detailLoaded = Boolean(state.selectedEvent);

    dom.stateFilter.textContent = state.filterStatus || "ALL";
    dom.stateListCount.textContent = String(state.events.length);
    dom.stateSelectedId.textContent = state.selectedEventId != null ? String(state.selectedEventId) : "-";
    dom.stateSelectedTitle.textContent = selectedTitle;
    dom.stateEventsSnapshot.value = JSON.stringify(state.events, null, 2);

    dom.listSummaryBadge.textContent = listLoaded ? `${state.events.length} Event${state.events.length > 1 ? "s" : ""} Loaded` : "No Events Loaded";
    dom.listSummaryBadge.className = `status ${listLoaded ? "ready" : "idle"}`;

    dom.detailSummaryBadge.textContent = detailLoaded ? `Event #${state.selectedEventId}` : "No Event Selected";
    dom.detailSummaryBadge.className = `status ${detailLoaded ? "ready" : "idle"}`;

    const consoleReady = detailLoaded || listLoaded;
    dom.consoleStatus.textContent = consoleReady ? "Loaded" : "Ready";
    dom.consoleStatus.className = `status ${consoleReady ? "ready" : "idle"}`;
}

function renderEmptyList() {
    dom.eventListView.innerHTML = `
        <article class="empty-panel">
            <strong>아직 조회된 이벤트가 없습니다.</strong>
            <p class="placeholder-copy">목록 조회를 실행하면 여기에서 이벤트 카드를 확인할 수 있습니다. 현재 DB에 데이터가 없으면 빈 배열이 표시되는 것이 정상입니다.</p>
        </article>
    `;
}

function renderEventList() {
    if (!state.events.length) {
        renderEmptyList();
        return;
    }

    dom.eventListView.innerHTML = state.events.map((event) => {
        const selected = state.selectedEventId === event.eventId ? " selected" : "";
        const status = escapeHtml(event.status);
        return `
            <article class="event-card${selected}">
                <div class="event-card-head">
                    <div>
                        <h3>${escapeHtml(event.title)}</h3>
                        <p class="muted">eventId ${escapeHtml(event.eventId)}</p>
                    </div>
                    <span class="status-token ${statusClass(event.status)}">${status}</span>
                </div>
                <p>${escapeHtml(event.description || "설명이 없습니다.")}</p>
                <div class="meta-row">
                    <span class="token">Created ${escapeHtml(formatDateTime(event.createdAt))}</span>
                    <button type="button" class="button button-inline" data-event-id="${escapeHtml(event.eventId)}">상세 조회</button>
                </div>
            </article>
        `;
    }).join("");
}

function renderEmptyDetail() {
    dom.eventDetailView.innerHTML = `
        <article class="empty-panel">
            <strong>선택된 이벤트가 없습니다.</strong>
            <p class="placeholder-copy">목록 카드의 상세 조회 버튼을 누르거나 eventId를 직접 입력하면 상세 응답과 회차 목록이 여기에 표시됩니다.</p>
        </article>
    `;
}

function renderEventDetail() {
    if (!state.selectedEvent) {
        renderEmptyDetail();
        return;
    }

    const event = state.selectedEvent;
    const shows = state.selectedShows;
    const showsMarkup = shows.length
        ? `<div class="show-list">${shows.map((show) => `
            <article class="show-card">
                <div class="show-head">
                    <div>
                        <h3>Show #${escapeHtml(show.showId)}</h3>
                        <p class="muted">${escapeHtml(formatDateTime(show.startAt))}</p>
                    </div>
                    <span class="status-token ${statusClass(show.status)}">${escapeHtml(show.status)}</span>
                </div>
                <div class="meta-row">
                    <span class="token">venueId ${escapeHtml(show.venueId)}</span>
                    <span class="token">startAt ${escapeHtml(formatDateTime(show.startAt))}</span>
                    <a class="button button-inline button-link" href="/shows-test.html?showId=${escapeHtml(show.showId)}">SHW-001 열기</a>
                </div>
            </article>
        `).join("")}</div>`
        : `
            <article class="empty-panel">
                <strong>등록된 회차가 없습니다.</strong>
                <p class="placeholder-copy">상세 응답은 정상이며, 현재 <code>shows</code> 배열이 비어 있습니다.</p>
            </article>
        `;

    dom.eventDetailView.innerHTML = `
        <article class="detail-summary">
            <div class="detail-head">
                <div>
                    <h3>${escapeHtml(event.title)}</h3>
                    <p class="muted">eventId ${escapeHtml(event.eventId)}</p>
                </div>
                <span class="status-token ${statusClass(event.status)}">${escapeHtml(event.status)}</span>
            </div>
            <p class="detail-copy">${escapeHtml(event.description || "설명이 없습니다.")}</p>
            <div class="detail-meta">
                <span class="token">Created ${escapeHtml(formatDateTime(event.createdAt))}</span>
                <span class="token">Shows ${escapeHtml(shows.length)}</span>
            </div>
        </article>
        ${showsMarkup}
    `;
}

function syncView() {
    syncEndpointPreview();
    syncSummary();
    renderEventList();
    renderEventDetail();
}

function resetState() {
    state.filterStatus = "";
    state.events = [];
    state.selectedEventId = null;
    state.selectedEvent = null;
    state.selectedShows = [];

    dom.statusFilter.value = "";
    dom.eventIdInput.value = "";

    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Events API Test Console loaded"
    });

    syncView();
}

async function fetchEventDetail(eventId) {
    state.selectedEventId = eventId;
    dom.eventIdInput.value = String(eventId);
    syncEndpointPreview();

    setLoading(dom.loadEventDetailButton, true);
    try {
        const result = await callApi({
            title: "EVT-002 Event Detail",
            endpoint: `${API_BASE}/api/events/${eventId}`
        });

        if (result.ok && result.body) {
            state.selectedEvent = result.body.event || null;
            state.selectedShows = Array.isArray(result.body.shows) ? result.body.shows : [];
        } else {
            state.selectedEvent = null;
            state.selectedShows = [];
        }

        syncView();
    } finally {
        setLoading(dom.loadEventDetailButton, false);
    }
}

async function fetchEventList() {
    const status = dom.statusFilter.value.trim();

    setLoading(dom.loadEventsButton, true);
    try {
        const result = await callApi({
            title: "EVT-001 Event List",
            endpoint: buildListEndpoint(status)
        });

        state.filterStatus = status;
        state.events = result.ok && result.body && Array.isArray(result.body.events) ? result.body.events : [];

        if (state.selectedEventId && !state.events.some((event) => event.eventId === state.selectedEventId)) {
            state.selectedEventId = null;
            state.selectedEvent = null;
            state.selectedShows = [];
        }

        syncView();
    } finally {
        setLoading(dom.loadEventsButton, false);
    }
}

dom.statusFilter.addEventListener("change", syncEndpointPreview);
dom.eventIdInput.addEventListener("input", syncEndpointPreview);

dom.listForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await fetchEventList();
});

dom.detailForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const eventId = Number(dom.eventIdInput.value);
    if (!Number.isInteger(eventId) || eventId < 1) {
        renderLog("EVT-002 Event Detail", "Invalid input", {
            error: "eventId must be a positive integer"
        });
        return;
    }

    await fetchEventDetail(eventId);
});

dom.eventListView.addEventListener("click", async (event) => {
    const trigger = event.target.closest("[data-event-id]");
    if (!trigger) {
        return;
    }

    const eventId = Number(trigger.dataset.eventId);
    if (!Number.isInteger(eventId)) {
        return;
    }

    await fetchEventDetail(eventId);
});

dom.clearStateButton.addEventListener("click", () => {
    resetState();
});

dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Events API Test Console loaded"
    });
});

resetState();