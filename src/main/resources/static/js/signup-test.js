const form = document.getElementById("signup-form");
const result = document.getElementById("result");

function render(payload) {
    result.textContent = JSON.stringify(payload, null, 2);
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    render({ loading: true, message: "요청 중..." });

    try {
        const response = await fetch("/api/auth/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email, password })
        });

        const bodyText = await response.text();
        let body;

        try {
            body = bodyText ? JSON.parse(bodyText) : null;
        } catch {
            body = bodyText;
        }

        render({
            ok: response.ok,
            status: response.status,
            statusText: response.statusText,
            body
        });
    } catch (error) {
        render({
            ok: false,
            error: error.message
        });
    }
});
