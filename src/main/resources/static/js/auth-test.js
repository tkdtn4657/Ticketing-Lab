const state = {
    userId: null,
    email: "",
    role: "",
    accessToken: "",
    refreshToken: ""
};

const dom = {
    signupForm: document.getElementById("signup-form"),
    loginForm: document.getElementById("login-form"),
    meForm: document.getElementById("me-form"),
    refreshForm: document.getElementById("refresh-form"),
    logoutForm: document.getElementById("logout-form"),
    clearLogButton: document.getElementById("clear-log"),
    signupEmail: document.getElementById("signup-email"),
    signupPassword: document.getElementById("signup-password"),
    loginEmail: document.getElementById("login-email"),
    loginPassword: document.getElementById("login-password"),
    meAccessToken: document.getElementById("me-access-token"),
    refreshToken: document.getElementById("refresh-token"),
    logoutAccessToken: document.getElementById("logout-access-token"),
    logoutRefreshToken: document.getElementById("logout-refresh-token"),
    authStatus: document.getElementById("auth-status"),
    stateUserId: document.getElementById("state-user-id"),
    stateEmail: document.getElementById("state-email"),
    stateRole: document.getElementById("state-role"),
    stateAccessToken: document.getElementById("state-access-token"),
    stateRefreshToken: document.getElementById("state-refresh-token"),
    lastAction: document.getElementById("last-action"),
    lastStatus: document.getElementById("last-status"),
    resultView: document.getElementById("result-view")
};

function nowText() {
    return new Date().toLocaleTimeString("ko-KR", { hour12: false });
}

function setState(patch) {
    Object.assign(state, patch);
    syncView();
}

function clearTokens() {
    setState({
        userId: null,
        role: "",
        accessToken: "",
        refreshToken: ""
    });
}

function syncView() {
    const authenticated = Boolean(state.accessToken);
    dom.authStatus.textContent = authenticated ? "Authenticated" : "Not Authenticated";
    dom.authStatus.className = `status ${authenticated ? "ready" : "idle"}`;

    dom.stateUserId.textContent = state.userId ?? "-";
    dom.stateEmail.textContent = state.email || "-";
    dom.stateRole.textContent = state.role || "-";
    dom.stateAccessToken.value = state.accessToken || "";
    dom.stateRefreshToken.value = state.refreshToken || "";

    dom.meAccessToken.value = state.accessToken || "";
    dom.refreshToken.value = state.refreshToken || "";
    dom.logoutAccessToken.value = state.accessToken || "";
    dom.logoutRefreshToken.value = state.refreshToken || "";

    if (state.email) {
        dom.loginEmail.value = state.email;
    }
}

function renderLog(title, statusText, payload) {
    dom.lastAction.textContent = title;
    dom.lastStatus.textContent = statusText;
    dom.resultView.textContent = JSON.stringify(payload, null, 2);
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

function extractAccessToken(headers, body) {
    if (body && typeof body === "object" && body.accessToken) {
        return body.accessToken;
    }

    const authorization = headers.authorization;
    if (authorization && authorization.startsWith("Bearer ")) {
        return authorization.slice("Bearer ".length);
    }

    return "";
}

async function callApi({ title, endpoint, method, body, accessToken }) {
    const requestHeaders = { Accept: "application/json" };
    if (body !== undefined) {
        requestHeaders["Content-Type"] = "application/json";
    }
    if (accessToken) {
        requestHeaders.Authorization = `Bearer ${accessToken}`;
    }

    renderLog(title, `Request started at ${nowText()}`, {
        request: {
            method,
            endpoint,
            headers: requestHeaders,
            body
        }
    });

    try {
        const response = await fetch(endpoint, {
            method,
            headers: requestHeaders,
            credentials: "same-origin",
            body: body !== undefined ? JSON.stringify(body) : undefined
        });

        const responseHeaders = collectHeaders(response);
        const responseBody = await parseBody(response);

        renderLog(title, `${response.status} ${response.statusText}`, {
            request: {
                method,
                endpoint,
                headers: requestHeaders,
                body
            },
            response: {
                status: response.status,
                statusText: response.statusText,
                headers: responseHeaders,
                body: responseBody
            },
            note: "Set-Cookie is not readable from browser JavaScript."
        });

        return {
            ok: response.ok,
            status: response.status,
            headers: responseHeaders,
            body: responseBody
        };
    } catch (error) {
        renderLog(title, "Network error", { error: error.message });
        throw error;
    }
}

dom.signupForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = dom.signupEmail.value.trim();
    const password = dom.signupPassword.value;
    const result = await callApi({
        title: "AUTH-001 Signup",
        endpoint: "/api/auth/signup",
        method: "POST",
        body: { email, password }
    });

    if (result.ok) {
        setState({
            userId: result.body?.userId ?? state.userId,
            email,
            role: state.role || "USER"
        });
        dom.loginEmail.value = email;
        dom.loginPassword.value = password;
    }
});

dom.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = dom.loginEmail.value.trim();
    const password = dom.loginPassword.value;
    const result = await callApi({
        title: "AUTH-002 Login",
        endpoint: "/api/auth/login",
        method: "POST",
        body: { email, password }
    });

    if (result.ok) {
        setState({
            email,
            accessToken: extractAccessToken(result.headers, result.body),
            refreshToken: result.body?.refreshToken || ""
        });
    }
});

dom.meForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const accessToken = dom.meAccessToken.value.trim();
    const result = await callApi({
        title: "AUTH-005 Me",
        endpoint: "/api/auth/me",
        method: "GET",
        accessToken
    });

    if (result.ok && result.body) {
        setState({
            userId: result.body.userId ?? state.userId,
            email: result.body.email ?? state.email,
            role: result.body.role ?? state.role
        });
    }
});

dom.refreshForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const refreshToken = dom.refreshToken.value.trim();
    const result = await callApi({
        title: "AUTH-003 Refresh",
        endpoint: "/api/auth/refresh",
        method: "POST",
        body: { refreshToken }
    });

    if (result.ok) {
        setState({
            accessToken: extractAccessToken(result.headers, result.body),
            refreshToken: result.body?.refreshToken || refreshToken
        });
    }
});

dom.logoutForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const accessToken = dom.logoutAccessToken.value.trim();
    const refreshToken = dom.logoutRefreshToken.value.trim();
    const result = await callApi({
        title: "AUTH-004 Logout",
        endpoint: "/api/auth/logout",
        method: "POST",
        accessToken,
        body: { refreshToken }
    });

    if (result.ok) {
        clearTokens();
    }
});

dom.clearLogButton.addEventListener("click", () => {
    renderLog("Ready", "No request yet", {
        ready: true,
        message: "Auth Flow Console loaded"
    });
});

syncView();
renderLog("Ready", "No request yet", {
    ready: true,
    message: "Auth Flow Console loaded"
});