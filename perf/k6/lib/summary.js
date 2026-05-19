const REPORT_DIR = __ENV.K6_REPORT_DIR || "/scripts/results";

export function reportSummary(data, scenarioName) {
  const timestamp = new Date().toISOString();
  const fileTimestamp = timestamp.replace(/[:.]/g, "-");
  const baseName = `${scenarioName}-${fileTimestamp}`;

  return {
    stdout: renderConsoleSummary(data, scenarioName),
    [`${REPORT_DIR}/${baseName}.json`]: JSON.stringify(data, null, 2),
    [`${REPORT_DIR}/${baseName}.html`]: renderHtmlReport(data, scenarioName, timestamp),
  };
}

function renderConsoleSummary(data, scenarioName) {
  const lines = [
    "",
    `k6 report: ${scenarioName}`,
    `status: ${thresholdsPassed(data) ? "PASS" : "FAIL"}`,
    `http_reqs: ${formatNumber(metricValue(data, "http_reqs", "count"))}`,
    `http_req_failed: ${formatPercent(metricValue(data, "http_req_failed", "rate"))}`,
    `http_req_duration p95: ${formatMs(metricValue(data, "http_req_duration", "p(95)") )}`,
    `http_req_duration p99: ${formatMs(metricValue(data, "http_req_duration", "p(99)") )}`,
    "",
    `HTML/JSON report written to ${REPORT_DIR}`,
    "",
  ];

  return `${lines.join("\n")}\n`;
}

function renderHtmlReport(data, scenarioName, timestamp) {
  const thresholds = collectThresholds(data);
  const customMetrics = collectCustomMetrics(data);
  const status = thresholds.every((threshold) => threshold.ok);

  return `<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>k6 Report - ${escapeHtml(scenarioName)}</title>
  <style>
    :root {
      color-scheme: light;
      --bg: #f7f8fa;
      --panel: #ffffff;
      --line: #d9dee7;
      --text: #202631;
      --muted: #657083;
      --pass: #167a3d;
      --fail: #b42318;
      --accent: #2457c5;
    }
    body {
      margin: 0;
      background: var(--bg);
      color: var(--text);
      font-family: Arial, sans-serif;
      line-height: 1.5;
    }
    main {
      max-width: 1120px;
      margin: 0 auto;
      padding: 32px 20px 48px;
    }
    header {
      margin-bottom: 20px;
    }
    h1 {
      margin: 0 0 8px;
      font-size: 28px;
    }
    h2 {
      margin: 0 0 12px;
      font-size: 18px;
    }
    .meta {
      color: var(--muted);
      font-size: 14px;
    }
    .status {
      display: inline-block;
      margin-top: 12px;
      padding: 6px 10px;
      border-radius: 6px;
      color: #fff;
      font-weight: 700;
      background: ${status ? "var(--pass)" : "var(--fail)"};
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
      gap: 12px;
      margin: 20px 0;
    }
    .card, section {
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 8px;
      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
    }
    .card {
      padding: 14px;
    }
    .label {
      color: var(--muted);
      font-size: 13px;
    }
    .value {
      margin-top: 4px;
      font-size: 24px;
      font-weight: 700;
    }
    section {
      padding: 16px;
      margin-top: 16px;
      overflow-x: auto;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 14px;
    }
    th, td {
      padding: 9px 8px;
      border-bottom: 1px solid var(--line);
      text-align: left;
      white-space: nowrap;
    }
    th {
      color: var(--muted);
      font-weight: 700;
    }
    .pass {
      color: var(--pass);
      font-weight: 700;
    }
    .fail {
      color: var(--fail);
      font-weight: 700;
    }
  </style>
</head>
<body>
<main>
  <header>
    <h1>k6 테스트 리포트</h1>
    <div class="meta">시나리오: ${escapeHtml(scenarioName)} · 생성 시각: ${escapeHtml(timestamp)}</div>
    <div class="status">${status ? "PASS" : "FAIL"}</div>
  </header>

  <div class="grid">
    ${metricCard("총 HTTP 요청", formatNumber(metricValue(data, "http_reqs", "count")))}
    ${metricCard("요청 처리량", `${formatNumber(metricValue(data, "http_reqs", "rate"))} req/s`)}
    ${metricCard("실패율", formatPercent(metricValue(data, "http_req_failed", "rate")))}
    ${metricCard("checks 통과율", formatPercent(metricValue(data, "checks", "rate")))}
    ${metricCard("응답 p95", formatMs(metricValue(data, "http_req_duration", "p(95)")))}
    ${metricCard("응답 p99", formatMs(metricValue(data, "http_req_duration", "p(99)")))}
    ${metricCard("최대 응답", formatMs(metricValue(data, "http_req_duration", "max")))}
    ${metricCard("반복 수", formatNumber(metricValue(data, "iterations", "count")))}
  </div>

  <section>
    <h2>Threshold 결과</h2>
    <table>
      <thead>
        <tr>
          <th>상태</th>
          <th>메트릭</th>
          <th>조건</th>
        </tr>
      </thead>
      <tbody>
        ${thresholds.map((threshold) => `
          <tr>
            <td class="${threshold.ok ? "pass" : "fail"}">${threshold.ok ? "PASS" : "FAIL"}</td>
            <td>${escapeHtml(threshold.metric)}</td>
            <td>${escapeHtml(threshold.expression)}</td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  </section>

  <section>
    <h2>주요 지연시간</h2>
    <table>
      <thead>
        <tr>
          <th>메트릭</th>
          <th>avg</th>
          <th>min</th>
          <th>med</th>
          <th>p90</th>
          <th>p95</th>
          <th>p99</th>
          <th>max</th>
        </tr>
      </thead>
      <tbody>
        ${durationRow(data, "http_req_duration")}
        ${durationRow(data, "iteration_duration")}
      </tbody>
    </table>
  </section>

  <section>
    <h2>커스텀 카운터</h2>
    <table>
      <thead>
        <tr>
          <th>메트릭</th>
          <th>count</th>
          <th>rate</th>
        </tr>
      </thead>
      <tbody>
        ${customMetrics.map((metric) => `
          <tr>
            <td>${escapeHtml(metric.name)}</td>
            <td>${formatNumber(metric.count)}</td>
            <td>${formatNumber(metric.rate)}</td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  </section>

  <section>
    <h2>실행 환경</h2>
    <table>
      <tbody>
        ${environmentRows().map(([key, value]) => `
          <tr>
            <th>${escapeHtml(key)}</th>
            <td>${escapeHtml(value)}</td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  </section>
</main>
</body>
</html>`;
}

function metricCard(label, value) {
  return `
    <div class="card">
      <div class="label">${escapeHtml(label)}</div>
      <div class="value">${escapeHtml(value)}</div>
    </div>
  `;
}

function durationRow(data, metricName) {
  return `
    <tr>
      <td>${escapeHtml(metricName)}</td>
      <td>${formatMs(metricValue(data, metricName, "avg"))}</td>
      <td>${formatMs(metricValue(data, metricName, "min"))}</td>
      <td>${formatMs(metricValue(data, metricName, "med"))}</td>
      <td>${formatMs(metricValue(data, metricName, "p(90)"))}</td>
      <td>${formatMs(metricValue(data, metricName, "p(95)"))}</td>
      <td>${formatMs(metricValue(data, metricName, "p(99)"))}</td>
      <td>${formatMs(metricValue(data, metricName, "max"))}</td>
    </tr>
  `;
}

function collectThresholds(data) {
  const thresholds = [];

  for (const [metricName, metric] of Object.entries(data.metrics || {})) {
    for (const [expression, result] of Object.entries(metric.thresholds || {})) {
      thresholds.push({
        metric: metricName,
        expression,
        ok: result.ok,
      });
    }
  }

  return thresholds;
}

function collectCustomMetrics(data) {
  const ignored = new Set([
    "checks",
    "data_received",
    "data_sent",
    "dropped_iterations",
    "http_req_blocked",
    "http_req_connecting",
    "http_req_duration",
    "http_req_failed",
    "http_req_receiving",
    "http_req_sending",
    "http_req_tls_handshaking",
    "http_req_waiting",
    "http_reqs",
    "iteration_duration",
    "iterations",
    "vus",
    "vus_max",
  ]);

  return Object.entries(data.metrics || {})
    .filter(([name, metric]) => !ignored.has(name) && metric.values && "count" in metric.values)
    .map(([name, metric]) => ({
      name,
      count: metric.values.count,
      rate: metric.values.rate,
    }))
    .sort((left, right) => left.name.localeCompare(right.name));
}

function thresholdsPassed(data) {
  return collectThresholds(data).every((threshold) => threshold.ok);
}

function metricValue(data, metricName, valueName) {
  const value = data.metrics?.[metricName]?.values?.[valueName];
  return typeof value === "number" && Number.isFinite(value) ? value : null;
}

function environmentRows() {
  const keys = [
    "BASE_URL",
    "SHOW_ID",
    "VUS",
    "DURATION",
    "SEAT_COUNT",
    "RACE_USERS",
    "PAYMENT_REQUESTS",
    "MAX_DURATION",
    "VERIFY_BODY",
    "K6_PROMETHEUS_RW_SERVER_URL",
  ];

  return keys
    .filter((key) => __ENV[key])
    .map((key) => [key, __ENV[key]]);
}

function formatNumber(value) {
  if (value === null || value === undefined) {
    return "-";
  }

  const number = Number(value);
  if (!Number.isFinite(number)) {
    return "-";
  }

  const fixed = Math.abs(number) >= 100
    ? number.toFixed(0)
    : number.toFixed(2);
  const parts = trimTrailingZeros(fixed).split(".");
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");

  return parts.join(".");
}

function trimTrailingZeros(value) {
  return value.replace(/\.0+$/, "").replace(/(\.\d*?)0+$/, "$1");
}

function formatMs(value) {
  if (value === null || value === undefined) {
    return "-";
  }

  return `${formatNumber(value)} ms`;
}

function formatPercent(value) {
  if (value === null || value === undefined) {
    return "-";
  }

  return `${formatNumber(value * 100)}%`;
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
