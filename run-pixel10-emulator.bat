@echo off
set "EMULATOR=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe"

"%EMULATOR%" -avd Pixel_10_Pro_XL -partition-size 2047 -no-snapshot -no-snapshot-save
