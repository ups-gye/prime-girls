@echo off
setlocal
echo Borrando compilaciones anteriores...
if exist code\model\target rmdir /s /q code\model\target
if exist code\server-hub\target rmdir /s /q code\server-hub\target
if exist code\client\target rmdir /s /q code\client\target
if exist code\server-monitor\target rmdir /s /q code\server-monitor\target
echo Carpetas target borradas.
echo.

echo [1/4] model...
cd code\model
call mvn install -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR model && pause && exit /b 1 )
cd ..\..

echo [2/4] server-hub...
cd code\server-hub
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR server-hub && pause && exit /b 1 )
cd ..\..

echo [3/4] client...
cd code\client
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR client && pause && exit /b 1 )
cd ..\..

echo [4/4] server-monitor...
cd code\server-monitor
call mvn package -q
if %ERRORLEVEL% NEQ 0 ( echo ERROR server-monitor && pause && exit /b 1 )
cd ..\..

echo.
echo BUILD COMPLETO
echo JARs listos en code\*\target\
echo.
pause
