@echo off
setlocal
cd /d "%~dp0"

call gradlew.bat build
set EXIT_CODE=%ERRORLEVEL%

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Build failed with exit code %EXIT_CODE%.
  exit /b %EXIT_CODE%
)

echo.
echo Build successful.
exit /b 0
