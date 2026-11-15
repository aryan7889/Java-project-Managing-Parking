#!/usr/bin/env bash
set -e

echo "=================================================="
echo "  Building Smart Parking Management System...     "
echo "=================================================="

mkdir -p bin data

javac -d bin src/com/smartparking/*.java \
             src/com/smartparking/model/*.java \
             src/com/smartparking/exception/*.java \
             src/com/smartparking/service/*.java \
             src/com/smartparking/util/*.java \
             src/com/smartparking/web/*.java \
             src/com/smartparking/test/*.java

echo "[OK] Compilation successful!"
echo ""
echo "Launching Application..."
java -cp bin com.smartparking.ParkingManager
