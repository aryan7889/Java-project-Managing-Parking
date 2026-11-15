#!/usr/bin/env bash
set -e

echo "=================================================="
echo "  Compiling and Running All 167 Tests...         "
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
echo "=== Phase 1 Tests (26 Assertions) ==="
java -cp bin com.smartparking.test.Phase1Test
echo ""
echo "=== Phase 2 Tests (28 Assertions) ==="
java -cp bin com.smartparking.test.Phase2Test
echo ""
echo "=== Phase 3 Tests (28 Assertions) ==="
java -cp bin com.smartparking.test.Phase3Test
echo ""
echo "=== Phase 4 Tests (30 Assertions) ==="
java -cp bin com.smartparking.test.Phase4Test
echo ""
echo "=== Phase 5 Tests (28 Assertions) ==="
java -cp bin com.smartparking.test.Phase5Test
echo ""
echo "=== Phase 6 Tests (27 Assertions - All 14 Scenarios) ==="
java -cp bin com.smartparking.test.Phase6Test
echo ""
echo "=================================================="
echo "  ALL TEST SUITES COMPLETED SUCCESSFULLY!         "
echo "=================================================="
