# URL Integration Service Test Script
# Tests all features of the URL service

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "URL Integration Service - Test Script" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$host_addr = "localhost"
$port = 8082

function Send-URLRequest {
    param (
        [string]$request,
        [string]$testName
    )
    
    Write-Host "[$testName] Sending request..." -ForegroundColor Yellow
    Write-Host "Request: $request" -ForegroundColor Gray
    
    try {
        $client = New-Object System.Net.Sockets.TcpClient($host_addr, $port)
        $stream = $client.GetStream()
        $writer = New-Object System.IO.StreamWriter($stream)
        $reader = New-Object System.IO.StreamReader($stream)
        
        # Send request
        $writer.WriteLine($request)
        $writer.WriteLine()
        $writer.Flush()
        
        # Read response
        $response = $reader.ReadLine()
        
        Write-Host "Response: " -ForegroundColor Green -NoNewline
        Write-Host $response -ForegroundColor White
        Write-Host ""
        
        $client.Close()
    }
    catch {
        Write-Host "Error: $_" -ForegroundColor Red
        Write-Host "Make sure the URL Service is running on port $port" -ForegroundColor Yellow
        Write-Host ""
    }
}

# Test 1: Get Motivational Quote
Write-Host "========== TEST 1: GET_QUOTE ==========" -ForegroundColor Cyan
$request1 = '{"action":"GET_QUOTE"}'
Send-URLRequest -request $request1 -testName "GET_QUOTE"

Start-Sleep -Seconds 1

# Test 2: Get Gravatar Avatar
Write-Host "========== TEST 2: GET_AVATAR ==========" -ForegroundColor Cyan
$request2 = '{"action":"GET_AVATAR","data":{"email":"test@example.com"}}'
Send-URLRequest -request $request2 -testName "GET_AVATAR"

Start-Sleep -Seconds 1

# Test 3: Validate URL
Write-Host "========== TEST 3: VALIDATE_URL ==========" -ForegroundColor Cyan
$request3 = '{"action":"VALIDATE_URL","data":{"url":"https://www.google.com"}}'
Send-URLRequest -request $request3 -testName "VALIDATE_URL"

Start-Sleep -Seconds 1

# Test 4: Parse URL
Write-Host "========== TEST 4: PARSE_URL ==========" -ForegroundColor Cyan
$request4 = '{"action":"PARSE_URL","data":{"url":"https://example.com:8080/path?param1=value1&param2=value2#section"}}'
Send-URLRequest -request $request4 -testName "PARSE_URL"

Start-Sleep -Seconds 1

# Test 5: Upload File
Write-Host "========== TEST 5: UPLOAD_FILE ==========" -ForegroundColor Cyan
$request5 = '{"action":"UPLOAD_FILE","data":{"fileData":"This is a test file content","fileName":"test_upload.txt"}}'
Send-URLRequest -request $request5 -testName "UPLOAD_FILE"

Start-Sleep -Seconds 1

# Test 6: Get Weather
Write-Host "========== TEST 6: GET_WEATHER ==========" -ForegroundColor Cyan
$request6 = '{"action":"GET_WEATHER","data":{"city":"London"}}'
Send-URLRequest -request $request6 -testName "GET_WEATHER"

Start-Sleep -Seconds 1

# Test 7: Fetch from Generic API
Write-Host "========== TEST 7: FETCH_API ==========" -ForegroundColor Cyan
$request7 = '{"action":"FETCH_API","data":{"url":"https://jsonplaceholder.typicode.com/todos/1","method":"GET"}}'
Send-URLRequest -request $request7 -testName "FETCH_API"

Start-Sleep -Seconds 1

# Test 8: Download File
Write-Host "========== TEST 8: DOWNLOAD_FILE ==========" -ForegroundColor Cyan
$request8 = '{"action":"DOWNLOAD_FILE","data":{"url":"https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf","fileName":"test_download.pdf"}}'
Send-URLRequest -request $request8 -testName "DOWNLOAD_FILE"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "All tests completed!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
