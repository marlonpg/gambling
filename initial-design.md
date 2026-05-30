# Race to the Moon - Game Design (v2)

## Vision

Race to the Moon is a 24/7 livestream gambling game where players place bets on how far a rocket can travel before exploding.

The game is designed around three principles:

- Extremely easy to understand
- Exciting to watch
- Runs forever

Players do not manually cash out.

Instead, they define a target altitude before launch.

Example:

```text
!bet 100 at 100km
```

If the rocket reaches 100km, the player automatically cashes out.

If the rocket explodes before reaching 100km, the player loses.

---

# Core Gameplay

## Betting Phase

Players join using chat commands:

```text
!bet 100 at 20km
!bet 500 at 100km
!bet 1000 at 5000km
```

Duration:

- 30 seconds

Displayed on stream:

```text
NEW LAUNCH

Alice -> 20km
Bob   -> 100km
Mike  -> 5000km
```

---

# Why Automatic Cashout

Traditional crash games require a manual cashout.

This creates:

- Stream delay issues
- Mobile disadvantages
- Complaints about timing

Race to the Moon removes this problem entirely.

Players choose their risk before launch.

The round then plays itself.

---

# Visual Direction

## Important Decision

The game will NOT run as a terminal application.

The game will run in a browser.

Backend:

- Spring Boot
- WebSocket

Frontend:

- HTML
- Canvas
- Vanilla JavaScript

The visual style will intentionally mimic a retro terminal.

This gives us:

- Better animations
- Easier streaming
- Easier future expansion
- Retro aesthetic

---

# Rendering Model

The backend owns the game state.

The frontend is only a renderer.

Backend broadcasts updates every 100ms.

Example:

```json
{
  "altitude": 142,
  "speed": 12,
  "phase": "FLYING"
}
```

Frontend receives updates and redraws the screen.

---

# Rocket Design

No emoji rocket.

The rocket is rendered using ASCII art.

Example:

```text
      /\
     /  \
    /____\
    |    |
   /|____|\
      ||
      ||
```

---

# Animation Strategy

Instead of moving the rocket upward...

The rocket remains mostly fixed near the bottom of the screen.

The world moves downward.

This creates the illusion of flight.

Advantages:

- Easier implementation
- Smoother animation
- Better spectator experience

---

# Atmosphere Layers

As altitude increases, the environment changes.

## Launch Pad

```text
====================================
```

## Clouds

```text
~~~~~~      ~~~~~
```

## Upper Atmosphere

```text
      ~~~~
```

## Space

```text
*      *       *
```

## Deep Space

```text
*    ☄    *    🪐
```

The frontend scrolls these layers downward.

---

# Engine Animation

The flame alternates every frame.

Frame A

```text
      ||
      /\
```

Frame B

```text
      ||
      \/
```

Frame C

```text
      ||
     /\/\
```

This creates a living rocket effect.

---

# Mission Control UI

Example screen:

```text
+--------------------------------------------------+
|                RACE TO THE MOON                  |
+--------------------------------------------------+

ALTITUDE: 142km
SPEED: 12km/s

TOP TARGETS

MIKE      100000km
JOHN       10000km
ALICE       5000km

                    /\
                   /  \
                  /____\
                  |    |
                 /|____|\
                    ||
                    ||
                    /\

===================================================

RECENT EVENTS

Alice cashed out at 100km
Bob cashed out at 500km
```

---

# Player Markers

Players become visible on the altitude scale.

Example:

```text
100000km | Mike
 10000km | John
  5000km | Alice
   500km | Bob
   100km |
         |
         Rocket
```

As the rocket climbs:

```text
SUCCESS: Bob reached 500km
```

Bob disappears from the list.

Now everyone watches Alice.

---

# Explosion Sequence

When the hidden explosion altitude is reached:

Frame 1

```text
ENGINE FAILURE
```

Frame 2

```text
 \ | /
--- * ---
 / | \
```

Frame 3

```text
BOOM
```

Frame 4

```text
Debris detected...
```

Result:

```text
EXPLODED AT 742km
```

---

# Rare Events

## Moon Mission

Special round every few hours.

```text
MOON MISSION
```

Features:

- Larger payouts
- Special visuals
- Increased participation

---

## Golden Rocket

Very rare.

Different rocket model.

Special effects.

Entire chat knows a legendary round has started.

---

# Technical Architecture

## Backend

- Java
- Spring Boot
- Scheduler (100ms tick)
- PostgreSQL
- WebSocket

Responsibilities:

- Accept bets
- Generate crash altitude
- Calculate payouts
- Broadcast game state

---

## Frontend

- HTML
- Canvas
- Vanilla JavaScript

Responsibilities:

- Draw rocket
- Animate flames
- Animate atmosphere
- Display players
- Display leaderboards

The frontend never decides outcomes.

Only the backend does.

---

# Future Enhancements

- Sound effects
- Different rocket skins
- Monthly seasons
- Team launches
- Streamer-controlled events
- Betting statistics
- Replay of biggest explosions

---

# Final Product Vision

The final experience should feel like:

- A retro NASA mission control system
- A livestream event
- A gambling game
- A community spectacle

The rocket launches every round.

The audience watches the same rocket.

The higher it flies, the more tension builds.

Everyone is waiting to see:

"Will it reach my altitude, or will it explode first?"
