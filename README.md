# Race to the Moon

Initial implementation of the livestream crash-style game from `initial-design.md`.

## Tech Stack

- Java 21
- Spring Boot (Web, WebSocket, Scheduling)
- Spring Data JPA + PostgreSQL
- Vanilla JS + Canvas frontend

## Features Implemented

- 30-second betting phase
- Bet command parsing (`!bet 100 at 500km`)
- 100ms backend game tick
- Automatic cashout when target is reached
- Random crash altitude and explosion phase
- Rare round modifiers (Moon Mission, Golden Rocket)
- WebSocket broadcasts of live game state
- Retro mission-control UI with animated rocket and atmosphere
- Persisted rounds and bets in PostgreSQL
- Admin controls for launch/round/pause/rare events
- History endpoints and frontend recent-round panel

## Start PostgreSQL (Docker)

```bash
docker compose up -d
```

The default DB values are in `.env.example` and match `application.properties` fallbacks.

## Run

```bash
./mvnw spring-boot:run
```

Open:

- http://localhost:8080

## API

- `GET /api/state` - returns current game snapshot
- `POST /api/bets` - place bet
- `GET /api/history/rounds?limit=20` - list latest persisted rounds
- `GET /api/history/rounds/{roundId}/bets` - list persisted bets for a round
- `POST /api/admin/force-launch` - force launch from betting phase
- `POST /api/admin/new-round` - immediately start a new round
- `POST /api/admin/pause` - pause game ticks
- `POST /api/admin/resume` - resume game ticks
- `POST /api/admin/next-moon` - queue moon mission for next round
- `POST /api/admin/next-golden` - queue golden rocket for next round

Example request:

```json
{
  "player": "Alice",
  "command": "!bet 100 at 500km"
}
```

You can also send structured fields:

```json
{
  "player": "Alice",
  "amount": 100,
  "targetKm": 500
}
```
