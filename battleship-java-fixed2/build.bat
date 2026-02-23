@echo off
setlocal
echo.
echo BATALLA NAVAL - Build Script
echo ============================
echo.

cd code

echo [1/4] Compilando model...
cd model
call mvn install -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR en model && pause && exit /b 1 )
cd ..

echo [2/4] Compilando server-hub...
cd server-hub
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR en server-hub && pause && exit /b 1 )
cd ..

echo [3/4] Compilando client...
cd client
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR en client && pause && exit /b 1 )
cd ..

echo [4/4] Compilando server-monitor...
cd server-monitor
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR en server-monitor && pause && exit /b 1 )
cd ..

echo.
echo BUILD EXITOSO
echo =============
echo.
echo JARs generados:
echo   code\server-hub\target\ServerHub.jar
echo   code\client\target\Client.jar
echo   code\server-monitor\target\Monitor.jar
echo.
echo Para ejecutar (en este orden):
echo   1) java -jar code\server-hub\target\ServerHub.jar
echo   2) java -jar code\client\target\Client.jar
echo   3) java -jar code\server-monitor\target\Monitor.jar
echo.
pause
