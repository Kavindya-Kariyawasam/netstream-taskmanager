# 🚀 Frontend + Backend Demo - Quick Start

## ✅ Issues Fixed

1. **JavaScript Error Fixed**: Changed `error.getMessage()` to `error.message`
2. **Backend Services**: All 3 services must be running

---

## 📋 Step-by-Step Demo Instructions

### Step 1: Start Backend (Terminal 1)

```powershell
cd c:\Users\Thilinika\Desktop\Me\Network\netstream-taskmanager\backend
java -cp "bin;lib/*" Main
```

**Expected Output:**

```
[INFO] Starting NetStream TaskManager...
==================================================
[URL Service] Started on port 8082
[INFO] TCP Server started on port 8080
[INFO] HTTP Gateway started on port 3000
==================================================
[INFO] All servers ready!
```

✅ **Leave this terminal running!**

### Step 2: Start Frontend (Terminal 2)

```powershell
cd c:\Users\Thilinika\Desktop\Me\Network\netstream-taskmanager\frontend
npm run dev
```

**Expected Output:**

```
VITE v5.x.x  ready in xxx ms
➜  Local:   http://localhost:5173/
```

✅ **Leave this terminal running!**

### Step 3: Open Browser

Open: **http://localhost:5173**

You should see a beautiful demo page with 8 features!

---

## 🎯 Testing All 8 Features

### 1. Motivational Quote ✨

- Click **"Get Quote"**
- Shows quote from ZenQuotes API
- Displays author and source

### 2. Gravatar Avatar 👤

- Email: `demo@example.com`
- Click **"Generate Avatar URL"**
- Shows avatar image + copyable URL

### 3. URL Validator ✅

- URL: `https://www.google.com`
- Click **"Validate URL"**
- Shows valid, accessible, protocol, host, status

### 4. URL Parser 🔗

- URL: `https://example.com:8080/path?param1=value1#section`
- Click **"Parse URL"**
- Shows protocol, host, port, path, query params

### 5. Weather Info ☁️

- City: `London`
- Click **"Get Weather"**
- Shows temperature, humidity, wind speed

### 6. File Download ⬇️

- Uses sample PDF URL
- Click **"Download File"**
- Shows file name, size, type, path

### 7. File Upload ⬆️

- Click **"Upload Sample File"**
- Uploads text content
- Shows confirmation

### 8. API Fetcher 🌐

- URL: `https://jsonplaceholder.typicode.com/todos/1`
- Click **"Fetch API"**
- Shows JSON response from any REST API

---

## 🐛 Troubleshooting

### Connection Refused Error

**Symptom:** `ERR_CONNECTION_REFUSED` in browser console

**Solution:**

1. Check backend is running in Terminal 1
2. Make sure you see "HTTP Gateway started on port 3000"
3. Verify with: `netstat -ano | findstr :3000`

### Frontend Won't Start

**Symptom:** Port 5173 already in use

**Solution:**

```powershell
Get-Process -Id (Get-NetTCPConnection -LocalPort 5173).OwningProcess | Stop-Process -Force
```

### Services Not Responding

**Solution:** Restart backend

1. Stop backend (Ctrl+C in Terminal 1)
2. Restart: `java -cp "bin;lib/*" Main`

---

## 🎥 For Your Demo Presentation

### Architecture Explanation

```
Browser (React Frontend)
    ↓ HTTP POST
HTTP Gateway (Port 3000)
    ↓ TCP/JSON
URL Service (Port 8082)
    ↓ HTTPS
External APIs (Internet)
```

### What to Say

**Introduction:**

> "I'm demonstrating Member 3 - the URL/URI External Integration Service. This service uses Java's URL, URLConnection, and HttpURLConnection classes to integrate with external REST APIs."

**Show Features:**

> "Let me demonstrate the 8 features..."
>
> 1. Click Get Quote - "This calls the ZenQuotes API using HttpURLConnection"
> 2. Click Generate Avatar - "This creates a Gravatar URL using MD5 hashing"
> 3. Click Validate URL - "This checks URL format and HTTP accessibility"

**Technical Points:**

> "The backend uses:
>
> - `URL` class for parsing URLs
> - `URLConnection` for HTTP communication
> - Exception handling for `MalformedURLException` and `IOException`
> - JSON protocol for request/response"

**Frontend Integration:**

> "The React frontend communicates through an HTTP Gateway that forwards JSON requests to the URL service using TCP sockets."

---

## ✅ Pre-Demo Checklist

- [ ] Backend compiled (`compile.ps1`)
- [ ] Backend running (Terminal 1)
- [ ] Frontend running (Terminal 2)
- [ ] Browser open to http://localhost:5173
- [ ] Test at least 3 features before demo
- [ ] Have code files open in VS Code
- [ ] Know which features to demonstrate

---

## 🎯 Recommended Demo Flow (5 minutes)

1. **Show both terminals running** (30 sec)
2. **Open browser, show UI** (30 sec)
3. **Demo 3 features:**
   - Get Quote (1 min)
   - Generate Avatar (1 min)
   - Validate URL or Weather (1 min)
4. **Show code in VS Code** (1.5 min)
   - `URLIntegrationService.java` main service
   - Point out URL/URLConnection usage
   - Show exception handling
5. **Q&A** (remaining time)

---

## 🚀 Quick Commands Summary

```powershell
# Terminal 1: Backend
cd backend
java -cp "bin;lib/*" Main

# Terminal 2: Frontend
cd frontend
npm run dev

# Browser
http://localhost:5173
```

**That's it! You're ready to demo! 🎉**
