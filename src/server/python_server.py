#!/usr/bin/env python3
"""
Sense HAT TCP Server (Assessment-style)

Protocol:
- Client connects and sends ONE byte:
    b'1' -> Temperature
    b'2' -> Humidity
- Server collects 10 readings (default delay 5 seconds)
- Server sends back a single line of space-separated floats + newline, then closes.

Example response:
21.53 21.49 21.44 21.57 21.52 21.45 21.40 21.41 21.43 21.39\n
"""

from __future__ import annotations

import socket
import time
from typing import Callable, List

# Prefer emulator when running on a PC; fall back to hardware on Raspberry Pi
try:
    from sense_emu import SenseHat  # type: ignore
except ImportError:
    from sense_hat import SenseHat  # type: ignore


HOST = "0.0.0.0"
PORT = 2003

SAMPLES = 10
DELAY_SECONDS = 5.0


def take_samples(read_fn: Callable[[], float], samples: int, delay_s: float) -> List[float]:
    readings: List[float] = []
    for _ in range(samples):
        value = round(float(read_fn()), 2)
        readings.append(value)
        time.sleep(delay_s)
    return readings


def handle_client(conn: socket.socket, addr: tuple[str, int], sense: SenseHat) -> None:
    print(f"[+] Client connected: {addr}")

    try:
        option = conn.recv(1)
        if not option:
            print("[!] No option received (client closed connection).")
            return

        if option == b"1":
            print("[*] Client requested: TEMPERATURE")
            # Hardware: sense.get_temperature() exists
            # Emulator: often works too
            readings = take_samples(sense.get_temperature, SAMPLES, DELAY_SECONDS)

        elif option == b"2":
            print("[*] Client requested: HUMIDITY")
            readings = take_samples(sense.get_humidity, SAMPLES, DELAY_SECONDS)

        else:
            print(f"[!] Invalid option received: {option!r}")
            conn.sendall(b"ERROR Invalid option\n")
            return

        # Send space-separated readings + newline so Java can use readLine()
        payload = (" ".join(map(str, readings)) + "\n").encode("utf-8")
        conn.sendall(payload)

        print("[+] Sent readings:", readings)

    except Exception as e:
        print("[!] Error while handling client:", e)

    finally:
        conn.close()
        print(f"[-] Client disconnected: {addr}")


def start_server() -> None:
    sense = SenseHat()
    print(f"[*] Starting server on {HOST}:{PORT}")
    print("[*] Waiting for client...")

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((HOST, PORT))
        server.listen(5)

        while True:
            conn, addr = server.accept()
            handle_client(conn, addr, sense)


if __name__ == "__main__":
    start_server()
