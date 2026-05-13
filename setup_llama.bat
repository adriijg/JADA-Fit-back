@echo off
title JADA Fit - IA Local
cls

echo ============================================
echo   JADA Fit - IA Local
echo ============================================
echo.
echo Este script descarga e inicia un modelo de IA
echo local para el asistente de JADA Fit.
echo.

set "SCRIPT_DIR=%~dp0"
set "LLAMA_DIR=%SCRIPT_DIR%llama"
set "SERVER_EXE=%LLAMA_DIR%\llama-server-cpu.exe"
set "MODEL_DIR=%LLAMA_DIR%\models"

REM ---------- llama-server ----------
if not exist "%SERVER_EXE%" (
    echo.
    echo [Paso 1/3] Descargando instalacion de ~30MB...
    if not exist "%LLAMA_DIR%" mkdir "%LLAMA_DIR%"

    echo    Descargando...

    powershell -Command "$ProgressPreference='SilentlyContinue'; $zip='%LLAMA_DIR%\llama.zip'; Write-Host '   Descargando...'; Invoke-WebRequest -Uri 'https://github.com/ggml-org/llama.cpp/releases/download/b4628/llama-b4628-bin-win-avx2-x64.zip' -OutFile $zip; Expand-Archive -Path $zip -DestinationPath '%LLAMA_DIR%' -Force; Remove-Item $zip"

    for /r "%LLAMA_DIR%" %%f in (llama-server.exe) do (
        if not exist "%SERVER_EXE%" copy "%%f" "%SERVER_EXE%" >nul
    )

    if exist "%SERVER_EXE%" (
        echo    [OK] llama-server descargado
    ) else (
        echo    [ERROR] No se pudo descargar llama-server
        echo    Descarga manual desde:
        echo    https://github.com/ggml-org/llama.cpp/releases
        pause
        exit /b 1
    )
) else (
    echo.
    echo [Paso 1/3] [OK] llama-server ya existe
)

REM ---------- Modelo ----------
if not exist "%MODEL_DIR%" mkdir "%MODEL_DIR%"

set "MODEL_FILE=%MODEL_DIR%\qwen2.5-1.5b-instruct-q4_k_m.gguf"

if not exist "%MODEL_FILE%" (
    echo.
    echo [Paso 2/3] Descargando modelo de ~1GB...
    echo    Modelo: Qwen2.5-1.5B-Instruct
    echo    Esto puede tardar varios minutos...

    powershell -Command "$ProgressPreference='SilentlyContinue'; $url='https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf'; Write-Host '   Descargando...'; Invoke-WebRequest -Uri $url -OutFile '%MODEL_FILE%'"

    if exist "%MODEL_FILE%" (
        echo    [OK] Modelo descargado
    ) else (
        echo    [ERROR] No se pudo descargar el modelo
        echo    Descarga manual desde:
        echo    https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF
        pause
        exit /b 1
    )
) else (
    echo.
    echo [Paso 2/3] [OK] Modelo ya existe
)

REM ---------- Iniciar servidor ----------
echo.
echo [Paso 3/3] Iniciando servidor en puerto 8090...
echo.
echo   Una vez iniciado, abre JADA Fit y usa
echo   el asistente IA con normalidad.
echo.
echo   Para detenerlo, cierra esta ventana.
echo.

"%SERVER_EXE%" -m "%MODEL_FILE%" -c 8192 -np 1 --port 8090 --jinja

echo.
echo El servidor se ha detenido.
pause
