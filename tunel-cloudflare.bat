@echo off
chcp 65001 >nul
title MAPS Connect - Tunel Cloudflare (acceso desde internet)
cd /d "%~dp0"

echo.
echo ==========================================================
echo   MAPS Connect - Exponer la app a INTERNET (Cloudflare)
echo ==========================================================
echo.
echo 1) PRIMERO lanza el backend: doble clic en "lanzar.bat"
echo    y espera a que diga que esta listo en el puerto 8080.
echo.
echo 2) Luego ejecuta este script desde aqui abajo.
echo.
echo ==========================================================
echo.

set "PORT=8080"
powershell -NoProfile -Command "try { $r = Invoke-WebRequest 'http://127.0.0.1:8080/api/health' -UseBasicParsing -TimeoutSec 5; if ($r.StatusCode -ne 200) { exit 1 } } catch { exit 1 }"
if errorlevel 1 (
    echo El backend no responde. Ejecuta lanzar.bat y revisa el error antes de abrir el tunel.
    pause
    exit /b 1
)
rem cloudflared.exe debe estar en la raiz del proyecto (junto a este script)
set "CF=%~dp0cloudflared.exe"
if not exist "%CF%" (
    echo NO se encontro cloudflared.exe. Debe estar en la raiz del
    echo proyecto (junto a lanzar.bat). Descargalo de la web de Cloudflare
    echo o mueve el ejecutable que ya tienes.
    pause
    exit /b 1
)

echo Arrancando tunel... la URL publica https aparecera cuando inicie.
echo.
echo La URL se ve asi (ejemplo):  https://xxxx-try.cloudflare.com
echo Comparte esa URL; apunta a tu localhost:8080 y escribira
echo en tu base de datos local mientras la maquina este encendida.
echo.
echo Pulsa Ctrl+C para cerrar el tunel.
echo.
echo ==========================================================
echo.

rem ---- Tunel temporal (quick tunnel), sin cuenta: publica una URL https ---- 
"%CF%" tunnel --protocol http2 --url http://127.0.0.1:%PORT%

echo.
echo Tunel cerrado.
pause
