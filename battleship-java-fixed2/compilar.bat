@echo off
setlocal
echo.
echo ============================================
echo  BATALLA NAVAL - Compilacion completa
echo ============================================
echo.
echo [1/4] Limpiando targets...
if exist code\model\target rmdir /s /q code\model\target 2>nul
if exist code\server-hub\target rmdir /s /q code\server-hub\target 2>nul
if exist code\client\target rmdir /s /q code\client\target 2>nul
if exist code\server-monitor\target rmdir /s /q code\server-monitor\target 2>nul
echo   OK

echo [2/4] Compilando model...
cd code\model
call mvn install -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR model && pause && exit /b 1 )
cd ..\..

echo [3/4] Compilando server-hub + client + monitor...
cd code\server-hub
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR server-hub && pause && exit /b 1 )
cd ..

cd client
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR client && pause && exit /b 1 )
cd ..

cd server-monitor
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR monitor && pause && exit /b 1 )
cd ..\..

echo.
echo BUILD EXITOSO
echo.
echo Ahora ejecuta en ventanas separadas:
echo   servidor.bat
echo   jugar.bat
echo   jugar.bat  (segunda ventana)
echo   monitor.bat
echo.
pause
