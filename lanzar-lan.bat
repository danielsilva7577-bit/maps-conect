@echo off
chcp 65001 >nul
title MAPS Connect - Lanzar para red LAN
cd /d "%~dp0"

echo.
echo ==========================================================
echo   MAPS Connect - Lanzar para que la prueben en la red
echo   (misma red WiFi/LAN, p. ej. companeros en la escuela)
echo ==========================================================
echo.

rem ---- Credenciales MySQL local ----
rem Lee desde variables de entorno, con fallback interactivo si no están definidas.
if "%DB_USER%"=="" (
    set /p DB_USER="Usuario MySQL (default: root): "
)
if "%DB_USER%"=="" set "DB_USER=root"
if "%DB_PASSWORD%"=="" (
    set /p DB_PASSWORD="Contraseña MySQL: "
)
set "DB_URL=jdbc:mysql://localhost:3306/maps_conect"

set "MVN=C:\Users\danie\Maven\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

rem ---- Obtener la IP local de esta maquina (primera IPv4 no loopback) ----
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254*' } | Select-Object -First 1).IPAddress"') do set "IP=%%i"
if "%IP%"=="" set "IP=TU_IP_LOCAL"

echo [Opcional] Permite el puerto 8080 en el Firewall de Windows
echo            (se necesita ejecutar como ADMINISTRADOR).
echo.
set /p ABRIR="Dar permiso en firewall? (s/n): "
if /i "%ABRIR%"=="s" (
    echo.
    echo Solicitando permiso de administrador...
    powershell -Command "Start-Process cmd -ArgumentList '/c netsh advfirewall firewall add rule name=\"MAPS Connect 8080\" dir=in action=allow protocol=TCP localport=8080' -Verb RunAs"
    echo. La regla se agrego si aceptaste el aviso de permisos.
    echo.
)

echo.
echo ==========================================================
echo   Para que otra persona la pruebe en la MISMA RED:
echo.
echo   En su navegador:  http://%IP%:8080/api/pages/inicio.html
echo.
echo   (IP es tu direccion en la red; tu maquina y el MySQL
echo    deben quedarse encendidos y esta ventana abierta)
echo ==========================================================
echo.
echo Pulsa Ctrl+C en esta ventana para DETENER la app cuando termines.
echo.
echo Arrancando Spring Boot...
echo.

"%MVN%" spring-boot:run "-Dspring-boot.run.profiles=dev"

echo.
echo La app se detuvo.
pause
