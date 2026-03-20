@echo off
setlocal
cd /d "%~dp0"

call gradlew.bat compileJava
set EXIT_CODE=%ERRORLEVEL%

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Fast build failed with exit code %EXIT_CODE%.
  exit /b %EXIT_CODE%
)

echo.
echo Fast build successful.
exit /b 0
