#!/bin/bash
# ═══════════════════════════════════════════════════════════
#  Batalla Naval - Script de compilación (Linux/Mac)
#  Requisitos: Java 11+, Maven 3.6+
# ═══════════════════════════════════════════════════════════

set -e
cd code

echo "▶ Compilando model..."
cd model && mvn install -q && cd ..

echo "▶ Compilando server-hub..."
cd server-hub && mvn package -q && cd ..

echo "▶ Compilando client..."
cd client && mvn package -q && cd ..

echo "▶ Compilando server-monitor..."
cd server-monitor && mvn package -q && cd ..

echo ""
echo "✅ Build exitoso. JARs en:"
echo "   code/server-hub/target/ServerHub.jar"
echo "   code/client/target/Client.jar"
echo "   code/server-monitor/target/Monitor.jar"
echo ""
echo "Para ejecutar:"
echo "   java -jar code/server-hub/target/ServerHub.jar"
echo "   java -jar code/client/target/Client.jar"
echo "   java -jar code/server-monitor/target/Monitor.jar"
