const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");

const phaseEl = document.getElementById("phase");
const altitudeEl = document.getElementById("altitude");
const speedEl = document.getElementById("speed");
const countdownEl = document.getElementById("bettingCountdown");
const eventsEl = document.getElementById("events");
const betForm = document.getElementById("betForm");
const playerInput = document.getElementById("player");
const commandInput = document.getElementById("command");
const betMessageEl = document.getElementById("betMessage");
const adminMessageEl = document.getElementById("adminMessage");
const roundHistoryEl = document.getElementById("roundHistory");
const adminButtons = document.querySelectorAll(".admin-btn");

const rocketAscii = [
    "      /\\",
    "     /  \\",
    "    /____\\",
    "    |    |",
    "   /|____|\\",
    "      ||",
    "      ||"
];

const flameFrames = [
    ["      ||", "      /\\"],
    ["      ||", "      \\/"],
    ["      ||", "     /\\/\\"]
];

let state = {
    phase: "BETTING",
    altitudeKm: 0,
    speedKmPerSec: 0,
    bettingSecondsRemaining: 30,
    crashAltitudeKm: null,
    moonMission: false,
    goldenRocket: false,
    flameFrame: 0,
    topTargets: [],
    activeMarkers: [],
    recentEvents: []
};

function connectSocket() {
    const socket = new SockJS("/ws-game");
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        stompClient.subscribe("/topic/state", (message) => {
            state = JSON.parse(message.body);
            renderPanels();
        });
    });
}

async function fetchInitialState() {
    try {
        const response = await fetch("/api/state");
        if (response.ok) {
            state = await response.json();
            renderPanels();
        }
    } catch (error) {
        console.error(error);
    }
}

function renderPanels() {
    phaseEl.textContent = state.phase;
    altitudeEl.textContent = `${state.altitudeKm} km`;
    speedEl.textContent = `${state.speedKmPerSec.toFixed(2)} km/s`;

    if (state.phase === "BETTING" && state.bettingSecondsRemaining !== null) {
        countdownEl.textContent = `${state.bettingSecondsRemaining}s remaining`;
    } else {
        countdownEl.textContent = "closed";
    }

    eventsEl.innerHTML = "";
    for (const eventText of state.recentEvents || []) {
        const li = document.createElement("li");
        li.textContent = eventText;
        eventsEl.appendChild(li);
    }
}

async function fetchRoundHistory() {
    try {
        const response = await fetch("/api/history/rounds?limit=8");
        if (!response.ok) {
            return;
        }
        const rounds = await response.json();
        roundHistoryEl.innerHTML = "";
        rounds.forEach((round) => {
            const li = document.createElement("li");
            const flags = [
                round.moonMission ? "MOON" : null,
                round.goldenRocket ? "GOLDEN" : null
            ].filter(Boolean).join("/");
            const suffix = flags ? ` [${flags}]` : "";
            li.textContent = `#${round.roundNumber} ${round.phase} crash:${round.crashAltitudeKm ?? "-"}km${suffix}`;
            roundHistoryEl.appendChild(li);
        });
    } catch (error) {
        console.error(error);
    }
}

async function runAdminAction(path) {
    adminMessageEl.style.color = "#bdf9ff";
    adminMessageEl.textContent = "Sending admin command...";
    try {
        const response = await fetch(path, {method: "POST"});
        const result = await response.json();
        if (response.ok && result.success) {
            adminMessageEl.style.color = "#99ff9f";
            adminMessageEl.textContent = result.message;
        } else {
            adminMessageEl.style.color = "#ff9da7";
            adminMessageEl.textContent = result.message || "Admin command failed";
        }
        await fetchRoundHistory();
    } catch (error) {
        adminMessageEl.style.color = "#ff9da7";
        adminMessageEl.textContent = "Network error";
    }
}

function drawBackground(scroll) {
    const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
    gradient.addColorStop(0, "#040b23");
    gradient.addColorStop(0.5, "#071429");
    gradient.addColorStop(1, "#0b1f33");
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    for (let i = 0; i < 80; i++) {
        const x = (i * 171) % canvas.width;
        const y = ((i * 103 + scroll * 0.25) % (canvas.height + 40)) - 20;
        ctx.fillStyle = i % 5 === 0 ? "#ffe8a3" : "#8bc8ff";
        ctx.fillRect(x, y, 2, 2);
    }

    drawCloudBand(scroll);
    drawGroundLine(scroll);
}

function drawCloudBand(scroll) {
    const baseY = canvas.height - 240 + (scroll % 220);
    ctx.fillStyle = "rgba(172, 220, 255, 0.18)";
    for (let i = 0; i < 8; i++) {
        const x = (i * 170 + 70) % (canvas.width + 130) - 65;
        const y = baseY - i * 60;
        ctx.fillRect(x, y, 120, 14);
    }
}

function drawGroundLine(scroll) {
    const y = canvas.height - 86 + (scroll % 160);
    ctx.strokeStyle = "#86ffd8";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(canvas.width, y);
    ctx.stroke();
}

function drawRocket() {
    ctx.fillStyle = "#d8fbff";
    ctx.font = "22px VT323";
    const x = canvas.width * 0.44;
    const y = canvas.height - 240;

    rocketAscii.forEach((line, index) => {
        ctx.fillText(line, x, y + index * 22);
    });

    const flame = flameFrames[state.flameFrame % flameFrames.length];
    flame.forEach((line, index) => {
        ctx.fillStyle = "#ff9d3d";
        ctx.fillText(line, x, y + rocketAscii.length * 22 + index * 22);
    });
}

function drawMissionHud() {
    ctx.font = "20px Share Tech Mono";
    ctx.fillStyle = "#74f6ff";
    ctx.fillText(`ALTITUDE: ${state.altitudeKm}km`, 22, 34);
    ctx.fillText(`SPEED: ${state.speedKmPerSec.toFixed(2)}km/s`, 22, 60);

    if (state.moonMission) {
        ctx.fillStyle = "#ffe27a";
        ctx.fillText("MOON MISSION", 22, 90);
    }
    if (state.goldenRocket) {
        ctx.fillStyle = "#ffd36e";
        ctx.fillText("GOLDEN ROCKET", 190, 90);
    }

    if (state.phase === "EXPLODED" && state.crashAltitudeKm !== null) {
        ctx.fillStyle = "#ff6a74";
        ctx.font = "28px VT323";
        ctx.fillText(`EXPLODED AT ${state.crashAltitudeKm}km`, 22, 130);
    }

    drawMarkers();
}

function drawMarkers() {
    const markers = state.activeMarkers || [];
    ctx.fillStyle = "#c8fff6";
    ctx.font = "18px Share Tech Mono";
    ctx.fillText("TARGETS", canvas.width - 240, 34);

    markers.slice(0, 12).forEach((m, i) => {
        ctx.fillText(`${m.targetKm}km | ${m.player}`, canvas.width - 240, 62 + i * 20);
    });
}

function loop() {
    const scroll = state.altitudeKm;
    drawBackground(scroll);
    drawRocket();
    drawMissionHud();
    requestAnimationFrame(loop);
}

betForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const payload = {
        player: playerInput.value.trim(),
        command: commandInput.value.trim()
    };

    betMessageEl.style.color = "#bdf9ff";
    betMessageEl.textContent = "Sending command...";

    try {
        const response = await fetch("/api/bets", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });

        const result = await response.json();
        if (response.ok && result.accepted) {
            betMessageEl.style.color = "#99ff9f";
            betMessageEl.textContent = `Accepted: ${result.player} -> ${result.targetKm}km`;
            commandInput.value = "";
        } else {
            betMessageEl.style.color = "#ff9da7";
            betMessageEl.textContent = result.message || "Bet rejected";
        }
    } catch (error) {
        betMessageEl.style.color = "#ff9da7";
        betMessageEl.textContent = "Network error";
    }
});

adminButtons.forEach((button) => {
    button.addEventListener("click", async () => {
        const path = button.dataset.adminAction;
        if (!path) {
            return;
        }
        await runAdminAction(path);
    });
});

fetchInitialState();
connectSocket();
fetchRoundHistory();
setInterval(fetchRoundHistory, 10000);
loop();
