@echo off
echo Compiling Hostel System...
javac --module-path "C:\javafx-sdk-24\lib" --add-modules javafx.controls,javafx.fxml -d bin src\hostel\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
mkdir bin\hostel 2>nul
copy src\hostel\style.css bin\hostel\style.css >nul

echo Starting Hostel System...
java --module-path "C:\javafx-sdk-24\lib" --add-modules javafx.controls,javafx.fxml -cp bin hostel.HostelApp
pause
