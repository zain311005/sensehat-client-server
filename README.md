# Sense HAT Client–Server System

## Project Documentation

A client–server IoT application developed using a **Raspberry Pi Sense HAT**, where a **Python-based socket server** collects environmental sensor data and a **Java GUI client** requests, processes, and visualizes the data in real time.

**Control Flow:**  
Sense → Transmit → Analyze → Visualize

---

## 1. Project Overview

This project demonstrates a distributed IoT system using a client–server architecture.

The **server**, running on a Raspberry Pi with a Sense HAT, listens for incoming TCP connections and collects sensor data based on client requests.  
The **client**, implemented as a Java GUI application, connects to the server, retrieves sensor readings, and performs basic statistical analysis.

When the system runs:

- The client selects **Temperature** or **Humidity**
- The server collects **10 sensor readings** at fixed intervals
- Readings are transmitted over a TCP socket
- The client computes **minimum, maximum, and average**
- The GUI updates its background color based on comfort thresholds

---

## 2. Features

### Server (Python – Raspberry Pi)
- TCP socket-based communication
- Sense HAT sensor integration
- Supports temperature and humidity data requests
- Sends readings as space-separated values
- Designed for single-client sequential handling

### Client (Java GUI)
- Graphical user interface using Java Swing
- Requests sensor data from server
- Displays all received readings
- Computes:
  - Minimum
  - Maximum
  - Average
- Dynamic background color based on comfort level

### Comfort Classification Logic

#### Temperature
- **Cold:** `< 15°C`
- **Comfortable:** `15–22°C`
- **Hot:** `> 22°C`

#### Humidity
- **Dry:** `< 55%`
- **Sticky:** `55–65%`
- **Oppressive:** `> 65%`

---

## 3. Tech Stack

- **Programming Languages:** Python 3, Java
- **Hardware Platform:** Raspberry Pi (with Sense HAT)
- **Libraries:**
  - `sense_hat` / `sense_emu` (Python)
  - Java Swing (GUI)
  - Java Sockets (Networking)
- **Communication Protocol:** TCP

---

## 4. Project Structure

```text
sensehat-client-server/
├─ src/
│  ├─ server/
│  │  └─ sensehat_server.py
│  └─ client/
│     └─ ClientGUI2.java
├─ docs/
│  └─ Assessment 3 Report.docx
├─ README.md
├─ .gitignore
└─ LICENSE
```

---

## 5. Installation & Setup

### Server (Raspberry Pi)

#### Requirements
- Raspberry Pi with Raspberry Pi OS
- Physical Sense HAT attached
- Python 3 installed

#### Steps
```bash
sudo apt update
sudo apt install -y sense-hat
python3 src/server/sensehat_server.py
```

---

### Client (PC / Laptop)

#### Requirements
- Java JDK 8 or higher
- Network access to Raspberry Pi

#### Steps
1. Open `ClientGUI2.java`
2. Update the Raspberry Pi IP address:
   ```java
   private static final String SERVER_IP = "YOUR_PI_IP_ADDRESS";
   ```
3. Compile:
   ```bash
   javac src/client/ClientGUI2.java
   ```
4. Run:
   ```bash
   java -cp src/client ClientGUI2
   ```

---

## 6. Usage Example

1. Start the Python server on the Raspberry Pi.
2. Launch the Java client application.
3. Click **Temperature** or **Humidity**.
4. The server collects 10 readings and sends them to the client.
5. The GUI displays:
   - All readings
   - Average value
   - Minimum and maximum
6. The background color updates according to comfort level.

---

## 7. Learning Outcomes

This project strengthened understanding of:

- Client–server architecture
- TCP socket communication
- Distributed IoT systems
- Sensor data acquisition
- Cross-language integration (Python + Java)
- GUI-based data visualization

---

## License

Educational / Academic Use

