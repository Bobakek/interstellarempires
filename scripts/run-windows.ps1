<#
Run-local helper for Windows 11 (PowerShell).

What it does:
- Builds ServerCore and ClGLayer using the Gradle wrapper
- Starts the test server (ServerCore) in a background process
- Waits for the server to listen on port 51234
- Launches the GUI client fat-jar (ClGLayer)
- When the client exits, stops the server

Usage:
  Open PowerShell as a normal user and run from the repository root:
    .\scripts\run-windows.ps1

Notes:
- This script assumes `java` and `gradlew.bat` are in PATH (or run from repo root where gradlew.bat exists).
- On some systems PowerShell's Start-Process cannot redirect stdout to a file; the server will open in a new console window.
#>

Set-StrictMode -Version Latest

function Write-Line($s) { Write-Host $s }

try {
  $repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
  Set-Location $repoRoot

  Write-Line "Building ServerCore and ClGLayer..."
  & .\gradlew.bat :ServerCore:classes :ClGLayer:build
  if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed (exit $LASTEXITCODE). Fix build errors and retry."
  }

  # Start server process
  Write-Line "Starting server..."
  $serverArgs = @('-cp', 'ServerCore\build\classes\java\main', 'io.lonelyrobot.empires.server.ServerMain')
  $serverProc = Start-Process -FilePath 'java' -ArgumentList $serverArgs -PassThru
  Write-Line "Server started (PID=$($serverProc.Id)). Waiting for port 51234..."

  # Wait for server port to become available
  $maxWait = 20
  $waited = 0
  $ready = $false
  while ($waited -lt $maxWait) {
    if (Test-NetConnection -ComputerName 'localhost' -Port 51234 -InformationLevel Quiet) { $ready = $true; break }
    Start-Sleep -Seconds 1
    $waited++
  }
  if (-not $ready) {
    Write-Line "Server did not start listening on port 51234 after $maxWait seconds. Check server console for errors."
    Write-Line "You can view server processes with: Get-Process -Id $($serverProc.Id)"
    throw "Server not ready"
  }

  # Find the latest client jar
  $libDir = Join-Path $repoRoot 'ClGLayer\build\libs'
  $clientJar = Get-ChildItem -Path $libDir -Filter *.jar | Sort-Object LastWriteTime -Descending | Select-Object -First 1
  if (-not $clientJar) {
    throw "Client jar not found in $libDir"
  }

  Write-Line "Launching GUI client: $($clientJar.Name)"
  $clientProc = Start-Process -FilePath 'java' -ArgumentList ('-jar', $clientJar.FullName) -PassThru

  Write-Line "Client started (PID=$($clientProc.Id)). Waiting for client to exit..."
  Wait-Process -Id $clientProc.Id

  Write-Line "Client exited. Stopping server (PID=$($serverProc.Id))..."
  try { Stop-Process -Id $serverProc.Id -Force } catch { Write-Line "Failed to stop server process: $_" }

  Write-Line "Done. If you want to see server output, run the server manually or open its console window."

} catch {
  Write-Error "Error: $_"
  if ($serverProc -and -not $serverProc.HasExited) {
    Write-Line "Attempting to stop server (PID=$($serverProc.Id))..."
    try { Stop-Process -Id $serverProc.Id -Force } catch { }
  }
  exit 1
}
