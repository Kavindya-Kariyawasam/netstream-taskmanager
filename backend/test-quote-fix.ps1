# Quick test for GET_QUOTE fix
Write-Host "=== Testing Quote API Fix ===" -ForegroundColor Cyan
Write-Host ""

try {
    $client = New-Object System.Net.Sockets.TcpClient("localhost", 8082)
    $stream = $client.GetStream()
    $writer = New-Object System.IO.StreamWriter($stream)
    $reader = New-Object System.IO.StreamReader($stream)
    
    Write-Host "Sending GET_QUOTE request..." -ForegroundColor Yellow
    $writer.WriteLine('{"action":"GET_QUOTE"}')
    $writer.WriteLine()
    $writer.Flush()
    
    $response = $reader.ReadLine()
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Green
    Write-Host $response
    Write-Host ""
    
    # Check if successful
    if ($response -like '*"status":"success"*') {
        Write-Host "✓ GET_QUOTE is now working!" -ForegroundColor Green
        Write-Host "✓ Using ZenQuotes API (better SSL support)" -ForegroundColor Green
    } else {
        Write-Host "✗ Still having issues" -ForegroundColor Red
    }
    
    $client.Close()
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure the URL Service is running:" -ForegroundColor Yellow
    Write-Host "  java -cp `"bin;lib/*`" Main" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
