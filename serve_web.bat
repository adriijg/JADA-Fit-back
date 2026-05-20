@echo off

echo ========================================
echo  JADA-FIT - Servidor Web
echo ========================================
echo.

for /f "tokens=2 delims=:" %%a in ('netsh interface ip show address "Ethernet" ^| findstr /R "Direcci.n IP|IP Address"') do set "ip=%%a"
set "ip=%ip: =%"

if "%ip%"=="" set "ip=localhost"

set "port=3000"
set "web_dir=..\jadafit-front\build\web"

echo Verificando backend en http://%ip%:8080...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://%ip%:8080/api/users/register' -Method OPTIONS -TimeoutSec 3 -UseBasicParsing; if ($r.StatusCode -eq 403 -or $r.StatusCode -eq 404 -or $r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %errorlevel% neq 0 (
    echo [ADVERTENCIA] No se detecta el backend en http://%ip%:8080
    echo  Abre otra terminal y ejecuta:
    echo    .\mvnw.cmd spring-boot:run -Dspring.profiles.active=local
    echo  Presiona Ctrl+C para salir o espera 5s para continuar...
    timeout /t 5 /nobreak >nul
)

echo Construyendo web con la IP detectada: %ip%
cd /d "%~dp0..\jadafit-front"
call flutter build web --release --dart-define=BASE_URL=http://%ip%:8080/api
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al construir la web
    pause
    exit /b 1
)
cd /d "%~dp0"

echo  IP:       %ip%
echo  Puerto:   %port%
echo  Carpeta:  %web_dir%
echo.
echo  Abre en el navegador: http://%ip%:%port%
echo.
echo  Presiona Ctrl+C para detener
echo ========================================
echo.

python -m http.server %port% --directory "%web_dir%"

pause
