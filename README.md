# Valid8 — Rutgers Parking Dashboard

A live campus parking dashboard for Rutgers University New Brunswick.
Built with **Java 17 + Spring Boot** backend and **HTML + Tailwind CSS + Vanilla JS** frontend.

---

## Features

- **Dashboard** — Real-time lot cards with occupancy bars and color-coded availability
- **Violations** — All parking records that fail the `legal()` check (overstay, unregistered, wrong lot)
- **Unregistered** — Plates not found in `registered_cars.csv`
- **Live Map** — Leaflet.js map of Rutgers NB with color-coded lot markers, auto-updating every 5 seconds
- **Reload Data** — Hot-reload CSVs without restarting the app

---

## Project Structure

```
valid8/
├── pom.xml
├── registered_cars.csv
├── simulation_data.csv
└── src/main/
    ├── java/com/valid8/
    │   ├── Valid8Application.java
    │   ├── controller/
    │   │   ├── ApiController.java       ← REST JSON endpoints
    │   │   └── PageController.java      ← Page routing
    │   ├── model/
    │   │   ├── AutoGate.java            ← Lot configs + business logic (ported from Python)
    │   │   ├── ParkingRecord.java       ← legal() rule engine
    │   │   ├── Registration.java        ← Registered vehicle
    │   │   ├── LotConfig.java           ← Lot name/capacity/coordinates
    │   │   ├── LotSummary.java          ← /api/summary response
    │   │   ├── ViolationItem.java       ← /api/violations response
    │   │   └── UnregisteredItem.java    ← /api/unregistered response
    │   ├── service/
    │   │   ├── Valid8Service.java       ← Orchestrates data + business logic
    │   │   └── CsvDataLoader.java       ← Reads CSV files
    │   └── util/
    │       └── TimeUtils.java           ← Flexible datetime parser
    └── resources/
        ├── application.properties
        ├── templates/                   ← Thymeleaf HTML pages
        │   ├── dashboard.html
        │   ├── violations.html
        │   ├── unregistered.html
        │   └── map.html
        └── static/
            ├── css/app.css
            └── js/
                ├── common.js
                ├── dashboard.js
                ├── violations.js
                ├── unregistered.js
                └── map.js
```

---

## Running on Windows with VS Code

### Prerequisites

1. **Java 17+ JDK**
   - Download from https://adoptium.net/ (Temurin 17 LTS recommended)
   - During install, check "Add to PATH" and "Set JAVA_HOME"
   - Verify: open Command Prompt → `java -version`

2. **Apache Maven**
   - Download from https://maven.apache.org/download.cgi
   - Extract to `C:\Maven` (or similar)
   - Add `C:\Maven\bin` to your system PATH
   - Verify: `mvn -version`

3. **VS Code**
   - Download from https://code.visualstudio.com/
   - Install the **Extension Pack for Java** (Microsoft)
     - Includes: Language Support for Java, Maven for Java, Debugger for Java

### Steps

1. **Open the project**
   ```
   File → Open Folder → select the `valid8` folder
   ```

2. **Place CSV files**
   - Put `registered_cars.csv` and `simulation_data.csv` in the **project root** (same folder as `pom.xml`)
   - Sample files are already included — edit them as needed

3. **Run the app**

   **Option A — VS Code terminal:**
   ```bash
   mvn spring-boot:run
   ```

   **Option B — VS Code Maven sidebar:**
   - Click the Maven icon in the left sidebar
   - Expand `valid8` → `Plugins` → `spring-boot` → double-click `spring-boot:run`

4. **Open in browser**
   ```
   http://localhost:8080
   ```

---

## CSV File Format

### registered_cars.csv

| Column | Description | Example |
|--------|-------------|---------|
| `plate` | License plate (case-insensitive) | `ABC123` |
| `lot` | Exact lot name | `Busch Lot 51` |
| `max_hours` | Max allowed parking hours | `4` |

### simulation_data.csv

| Column | Description | Example |
|--------|-------------|---------|
| `plate` | License plate | `ABC123` |
| `lot` | Exact lot name | `Busch Lot 51` |
| `entry_time` | Entry datetime | `2024-03-01 08:00:00` |
| `exit_time` | Exit datetime (blank = still in lot) | `2024-03-01 11:30:00` |

**Supported datetime formats:**
- `yyyy-MM-dd HH:mm:ss` ← preferred
- `yyyy-MM-dd HH:mm`
- `yyyy-MM-dd'T'HH:mm:ss`
- `MM/dd/yyyy HH:mm:ss`

---

## Lot Names and Capacities

| Lot Name | Capacity |
|----------|----------|
| Busch Lot 51 | 90 |
| Livingston Yellow | 85 |
| College Ave Deck | 100 |
| Cook Lot 98 | 60 |
| Stadium West | 75 |

> Lot names in your CSV must match exactly (case-insensitive).

---

## Using the "Reload Data" Button

Click **↺ Reload Data** on any page to:
1. Re-read both CSV files from disk
2. Recompute all summaries, violations, and unregistered lists
3. Refresh the current page display

No app restart needed. Useful when you update the CSV files during a demo.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/summary` | Lot summaries (capacity, inLot, available, etc.) |
| GET | `/api/violations` | All violation records |
| GET | `/api/unregistered` | All unregistered plates |
| POST | `/api/reload` | Reload CSV files |

---

## Troubleshooting

### Port already in use
```
Web server failed to start. Port 8080 was already in use.
```
Change the port in `src/main/resources/application.properties`:
```properties
server.port=9090
```
Then open `http://localhost:9090`

---

### CSV file not found
```
[CsvDataLoader] File not found: registered_cars.csv
```
Make sure the CSV files are in the **project root directory** (same folder as `pom.xml`), not inside `src/`.

Alternatively, set absolute paths in `application.properties`:
```properties
valid8.csv.registrations=C:/Users/you/data/registered_cars.csv
valid8.csv.simulation=C:/Users/you/data/simulation_data.csv
```

---

### Java version mismatch
```
Source option 17 is no longer supported. Use 21 or later.
```
You may have JDK 21+ installed. Update `pom.xml`:
```xml
<properties>
    <java.version>21</java.version>
</properties>
```
Or install JDK 17 from https://adoptium.net/

---

### Maven not found
```
'mvn' is not recognized as an internal or external command
```
- Ensure Maven's `bin` directory is on your system PATH
- Restart VS Code after changing PATH
- Alternatively use the VS Code Maven extension sidebar

---

## Business Logic Notes

The `legal()` method in `ParkingRecord.java` faithfully mirrors the Python `ParkingRecord.legal()` logic:

1. **Unregistered** — Plate not found in `registered_cars.csv` → violation
2. **Wrong Lot** — Plate registered, but for a different lot → violation
3. **Overstay** — Parking duration exceeds `max_hours` for the plate → violation

All other records are considered compliant.
