@echo off
chcp 65001 >nul
title MAPS Connect - Backend (dev)
cd /d "%~dp0"

echo.
echo ============================================
echo   MAPS Connect - Lanzar backend (desarrollo)
echo ============================================
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

rem ---- Ruta del Maven (si no esta en PATH) ----
set "MVN=C:\Users\danie\Maven\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

echo [1/2] Iniciando Spring Boot... esto tarda unos segundos.
echo       No cierres esta ventana mientras la app este en uso.
echo.

echo Pulsa Ctrl+C en esta ventana para DETENER la app cuando termines.
echo.
echo Esperando: http://localhost:8080/api/health
echo.
echo Abre la app en tu navegador:  http://localhost:8080/api/pages/inicio.html
echo.

rem ---- Levanta el backend con el perfil dev y las credenciales ----
"%MVN%" spring-boot:run "-Dspring-boot.run.profiles=dev"

echo.
echo ============================================
echo   La app se detuvo (puede que siga abierta en el navegador).
echo ============================================
pause
