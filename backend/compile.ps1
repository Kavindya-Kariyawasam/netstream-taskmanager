<#
  backend/compile.ps1

  Simple helper to compile the backend Java sources using the local gson JAR.
  Usage: Run this script from any PowerShell prompt. It will produce compiled
  classes under backend/bin.
#>

# Resolve script directory and run from backend folder
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Push-Location $scriptDir

Write-Host "Preparing file list of Java sources..."
# Create an argfile with quoted forward-slash paths to handle spaces
# Write the argfile without a UTF-8 BOM to avoid javac parsing issues (use ASCII)
Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { '"' + ($_.FullName -replace '\\','/') + '"' } | Out-File -FilePath .\files.txt -Encoding ascii

Write-Host "Compiling Java sources into .\bin (using backend/lib/gson-2.10.1.jar)"
# Use cmd /c so javac @argfile semantics and quoting behave consistently on Windows
cmd /c "javac -d .\bin -cp .\lib\gson-2.10.1.jar @.\files.txt"

if ($LASTEXITCODE -eq 0) {
    Write-Host "COMPILATION SUCCEEDED"
} else {
    Write-Host "COMPILATION FAILED with exit code $LASTEXITCODE"
    Exit $LASTEXITCODE
}

Pop-Location
