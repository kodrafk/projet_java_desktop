@echo off
echo ============================================================
echo  NutriLife - Installation Face Recognition (Python)
echo ============================================================
echo.

REM Detect Python
set PYTHON_EXE=
for %%p in (python python3 py) do (
    %%p --version >nul 2>&1
    if not errorlevel 1 (
        set PYTHON_EXE=%%p
        goto :found_python
    )
)

REM Try common install paths
for %%p in (
    "C:\Python312\python.exe"
    "C:\Python311\python.exe"
    "C:\Python310\python.exe"
    "%USERPROFILE%\AppData\Local\Programs\Python\Python312\python.exe"
    "%USERPROFILE%\AppData\Local\Programs\Python\Python311\python.exe"
    "%USERPROFILE%\AppData\Local\Programs\Python\Python310\python.exe"
) do (
    if exist %%p (
        set PYTHON_EXE=%%p
        goto :found_python
    )
)

echo ERROR: Python not found!
echo.
echo Please install Python 3.8 or newer from: https://python.org/downloads
echo Make sure to check "Add Python to PATH" during installation.
echo.
pause
exit /b 1

:found_python
echo Python found: %PYTHON_EXE%
%PYTHON_EXE% --version
echo.

echo Step 1/4: Upgrading pip...
%PYTHON_EXE% -m pip install --upgrade pip

echo.
echo Step 2/4: Installing cmake (required for dlib)...
%PYTHON_EXE% -m pip install cmake

echo.
echo Step 3/4: Installing dlib (face detection engine)...
echo NOTE: This may take several minutes to compile...
%PYTHON_EXE% -m pip install dlib

echo.
echo Step 4/4: Installing face_recognition, pillow, numpy...
%PYTHON_EXE% -m pip install face_recognition pillow numpy

echo.
echo ============================================================
echo  Testing installation...
echo ============================================================
%PYTHON_EXE% -c "import face_recognition; import PIL; import numpy; print('All packages OK!')"
if errorlevel 1 (
    echo.
    echo WARNING: Some packages may not have installed correctly.
    echo Try running this script again, or install manually:
    echo   pip install face_recognition pillow numpy
) else (
    echo.
    echo SUCCESS! Face Recognition is ready.
    echo You can now use Face ID in NutriLife.
)

echo.
pause
