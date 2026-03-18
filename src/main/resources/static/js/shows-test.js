const API_BASE = window.location.port === "9090"
    ? ""
    : `${window.location.protocol}//${window.location.hostname}:9090`;

const state = {
    selectedShowId: null,
    seats: [],
    sections: []
};

const dom = {
    availabilityForm: document.getElementById("availability-form"),
    showIdInput: document.getElementById("show-id-input"),
    availabilityEndpointPreview: document.getElementById("availability-endpoint-preview"),
    loadAvailabilityButton: document.getElementById("load-availability-button"),
    clearStateButton: document.getElementById("clear-state-button"),
    clearLogButton: document.getElementById("clear-log-button"),
    seatListView: document.getElementById("seat-list-view"),
    sectionListView: document.getElementById("section-list-view"),
    seatsSummaryBadge: document.getElementById("seats-summary-badge"),
    sectionsSummaryBadge: document.getElementById("sections-summary-badge"),
    consoleStatus: document.getElementById("console-status"),
    stateShowId: document.getElementById("state-show-id"),
    stateSeatCount: document.getElementById("state-seat-count"),
    stateAvailableSeats: document.getElementById("state-available-seats"),
    stateSectionCount: document.getElementById("state-section-count"),
    stateTotalRemaining: document.getElementById("state-total-remaining"),
    stateAvailabilitySnapshot: document.getElementById("state-availability-snapshot"),
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

async function fetchJson(endpoint) {
    const response = await fetch(endpoint, {
        method: "GET",
        headers: {
            Accept: "application/json"
        },
        credentials: API_BASE ? "omit" : "same-origin"
    });

    const bodyText = await response.text();
    const body = parseBody(bodyText);

    return {
        ok: response.ok,
        status: response.status,
        body
    };
}

async function discoverSampleShowId() {
    const eventsResult = await fetchJson(`${API_BASE}/api/events`);
    if (!eventsResult.ok || !eventsResult.body || !Array.isArray(eventsResult.body.events) || !eventsResult.body.events.length) {
        return null;
    }

    const sampleEvent = eventsResult.body.events.find((event) => String(event.title || "").includes("샘플"))
        || eventsResult.body.events.find((event) => event.status === "PUBLISHED")
        || eventsResult.body.events[0];

    if (!sampleEvent) {
        return null;
    }

    const detailResult = await fetchJson(`${API_BASE}/api/events/${sampleEvent.eventId}`);
    if (!detailResult.ok || !detailResult.body || !Array.isArray(detailResult.body.shows) || !detailResult.body.shows.length) {
        return null;
    }

    const scheduledShow = detailResult.body.shows.find((show) => show.status === "SCHEDULED");
    return (scheduledShow || detailResult.body.shows[0]).showId;
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
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

function buildAvailabilityEndpoint(showId) {
    return showId
        ? `${API_BASE}/api/shows/${showId}/availability`
        : `${API_BASE}/api/shows/{showId}/availability`;
}

function readShowIdFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const rawShowId = params.get("showId");
    const showId = Number(rawShowId);
    return Number.isInteger(showId) && showId > 0 ? showId : null;
}

function updateQuery(showId) {
    const url = new URL(window.location.href);
    if (showId) {
        url.searchParams.set("showId", String(showId));
    } else {
        url.searchParams.delete("showId");
    }
    window.history.replaceState({}, "", url);
}

function syncEndpointPreview() {
    const showId = dom.showIdInput.value.trim();
    dom.availabilityEndpointPreview.value = buildAvailabilityEndpoint(showId);
}

function syncSummary() {
    const availableSeatCount = state.seats.filter((seat) => seat.available).length;
    const totalRemaining = state.sections.reduce((sum, section) => sum + Number(section.remainingQty || 0), 0);
    const loaded = state.selectedShowId != null;

    dom.stateShowId.textContent = loaded ? String(state.selectedShowId) : "-";
    dom.stateSeatCount.textContent = String(state.seats.length);
    dom.stateAvailableSeats.textContent = String(availableSeatCount);
    dom.stateSectionCount.textContent = String(state.sections.length);
    dom.stateTotalRemaining.textContent = String(totalRemaining);
    dom.stateAvailabilitySnapshot.value = JSON.stringify({
        showId: state.selectedShowId,
        seats: state.seats,
        sections: state.sections
    }, null, 2);

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

function renderEmptySeatList() {
    dom.seatListView.innerHTML = `
        <article class="empty-panel">
            <strong>불러온 좌석 정보가 없습니다.</strong>
            <p class="placeholder-copy">회차 가용성을 조회하면 이 영역에 좌석별 가용 상태가 표시됩니다. 좌석 인벤토리가 없으면 빈 배열 응답이 정상입니다.</p>
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
            <p class="placeholder-copy">회차 가용성을 조회하면 구역별 잔여 수량이 표시됩니다. 구역 인벤토리가 없으면 빈 배열 응답이 정상입니다.</p>
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
    renderSeatList();
    renderSectionList();
}

function resetState({ preserveQuery = false } = {}) {
    state.selectedShowId = null;
    state.seats = [];
    state.sections = [];

    dom.showIdInput.value = "";
    if (!preserveQuery) {
        updateQuery(null);
    }

    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Shows Availability Test Console loaded"
    });

    syncView();
}

async function fetchAvailability(showId) {
    state.selectedShowId = showId;
    dom.showIdInput.value = String(showId);
    updateQuery(showId);
    syncEndpointPreview();

    setLoading(dom.loadAvailabilityButton, true);
    try {
        const result = await callApi({
            title: "SHW-001 Show Availability",
            endpoint: buildAvailabilityEndpoint(showId)
        });

        if (result.ok && result.body) {
            state.seats = Array.isArray(result.body.seats) ? result.body.seats : [];
            state.sections = Array.isArray(result.body.sections) ? result.body.sections : [];
        } else {
            state.seats = [];
            state.sections = [];
        }

        syncView();
    } finally {
        setLoading(dom.loadAvailabilityButton, false);
    }
}

dom.showIdInput.addEventListener("input", syncEndpointPreview);

dom.availabilityForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const showId = Number(dom.showIdInput.value);
    if (!Number.isInteger(showId) || showId < 1) {
        renderLog("SHW-001 Show Availability", "Invalid input", {
            error: "showId must be a positive integer"
        });
        return;
    }

    await fetchAvailability(showId);
});

dom.clearStateButton.addEventListener("click", () => {
    resetState();
});

dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Shows Availability Test Console loaded"
    });
});

resetState({ preserveQuery: true });

(async () => {
    const queryShowId = readShowIdFromQuery();
    if (queryShowId) {
        await fetchAvailability(queryShowId);
        return;
    }

    const discoveredShowId = await discoverSampleShowId();
    if (discoveredShowId) {
        await fetchAvailability(discoveredShowId);
        return;
    }

    renderLog("Ready", "No sample show found", {
        ready: true,
        message: "Shows Availability Test Console loaded",
        hint: "showId를 직접 입력해서 조회해 주세요."
    });
})();