# NetStream TaskManager - Frontend

React + TypeScript frontend for the NetStream TaskManager application, demonstrating real-time task management through multiple network protocols.

## 🎨 Application Layout

```
┌─────────────────────────────────────────────────────────┐
│  Header: NetStream TaskManager                   (UDP)  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────┐  ┌──────────────────────┐  │
│  │   Task List (TCP)       │  │  Profile Card (URL)  │  │
│  │   - Create Task         │  │  - Avatar (Gravatar) │  │
│  │   - View Tasks          │  │  - Quote API         │  │
│  │   - Update/Delete       │  │                      │  │
│  │   - Filter by status    │  └──────────────────────┘  │
│  │                         │                            │
│  │   ┌──────┐ ┌──────┐     │  ┌──────────────────────┐  │
│  │   │Task 1│ │Task 2│     │  │  File Upload (NIO)   │  │
│  │   └──────┘ └──────┘     │  │  - Drag & Drop       │  │
│  │                         │  │  - Progress bar      │  │
│  └─────────────────────────┘  └──────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Single Page Application**

## 🚀 Quick Start

### Prerequisites

- Node.js 18+ and npm 9+
- Backend servers running (TCP on 8080, UDP on 9090, NIO on 8081, URL on 8082)

### Installation

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The application will be available at `http://localhost:5173`

## 📦 Tech Stack

- **Framework**: React 18.2+
- **Language**: TypeScript 5+
- **Build Tool**: Vite 5+
- **Styling**: Tailwind CSS 4+
- **HTTP Client**: Axios
- **Icons**: Lucide React

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/              # React components
│   │   ├── TaskList.tsx         # Main task list with CRUD (TCP)
│   │   ├── TaskForm.tsx         # Create/Edit task form (TCP)
│   │   ├── TaskCard.tsx         # Individual task card (TCP)
│   │   ├── NotificationBell.tsx # Real-time notifications (UDP)
│   │   ├── FileUpload.tsx       # File upload widget (NIO)
│   │   └── ProfileCard.tsx      # User profile & quotes (URL)
│   │
│   ├── services/                # API service layer
│   │   ├── tcpService.ts        # TCP server communication (Port 8080)
│   │   ├── udpService.ts        # UDP notifications listener (Port 9090)
│   │   ├── nioService.ts        # File operations (Port 8081)
│   │   └── urlService.ts        # External APIs (Port 8082)
│   │
│   ├── types/                   # TypeScript type definitions
│   │   └── index.ts             # Task, ApiResponse interfaces
│   │
│   ├── App.tsx                  # Main application component
│   ├── main.tsx                 # Application entry point
│   └── index.css                # Global styles (Tailwind)
│
├── public/                      # Static assets
├── index.html                   # HTML entry point
├── package.json                 # Dependencies
├── tsconfig.json                # TypeScript config (root)
├── tsconfig.app.json            # App-specific TS config
├── tsconfig.node.json           # Build tools TS config
├── vite.config.ts               # Vite configuration
├── postcss.config.js            # PostCSS config
└── eslint.config.js             # ESLint configuration
```

## 🔧 Configuration Files

### `vite.config.ts`

```typescript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
```

### `index.css`

Simple Tailwind CSS import:

```css
@import "tailwindcss";
```

## 🎯 Component Overview

### Task Management (TCP Service)

#### `TaskList.tsx`

- Fetches and displays all tasks
- Filter by status (all, pending, in-progress, completed)
- Refresh functionality
- Create new task button
- Handles loading and error states

#### `TaskForm.tsx`

- Modal form for creating/editing tasks
- Fields: title, assignee, deadline, priority, status
- Form validation
- Submit to TCP server

#### `TaskCard.tsx`

- Displays individual task details
- Status and priority badges with color coding
- Edit and delete buttons
- Hover effects

### Real-Time Notifications (UDP Service)

#### `NotificationBell.tsx`

- Displays notification icon in header
- Shows unread notification count
- Dropdown list of recent notifications
- Listens to UDP broadcasts from backend

Note: The notification component includes an inbox UI (bell) where users can delete or dismiss notifications; the frontend listens to UDP broadcasts and updates the UI in real time.

### File Operations (NIO Service)

#### `FileUpload.tsx`

- Drag-and-drop file upload
- File size validation (max 50MB)
- Upload progress indicator
- Connects to NIO server on port 8081

Note: The frontend file upload widget supports browser-based multipart uploads (drag & drop) and works with the backend NIO endpoint which returns a file ID on success.

### External Integrations (URL Service)

#### `ProfileCard.tsx`

- Fetches and displays user avatar from Gravatar
- Shows motivational quote from external API
- Refresh quote functionality
- Connects to URL service on port 8082

## 🌐 API Communication

### TCP Service (Port 8080)

All requests use JSON format:

```typescript
// Create Task
{
  "action": "CREATE_TASK",
  "data": {
    "title": "Build frontend",
    "assignee": "John Doe",
    "deadline": "2025-11-01",
    "priority": "high"
  }
}

// Get All Tasks
{
  "action": "GET_TASKS"
}

// Update Task
{
  "action": "UPDATE_TASK",
  "data": {
    "taskId": "task_12345",
    "status": "completed"
  }
}

// Delete Task
{
  "action": "DELETE_TASK",
  "data": {
    "taskId": "task_12345"
  }
}
```

Response format:

```typescript
{
  "status": "success" | "error",
  "data": { /* response data */ },
  "message": "Optional message"
}
```

### UDP Service (Port 9090)

Receives notifications in format:

```
NOTIFICATION_TYPE|TASK_ID|MESSAGE|TIMESTAMP
```

Example:

```
TASK_CREATED|task_123|New task assigned to John|1729350000000
```

### NIO Service (Port 8081)

File upload/download using multipart form data.

### URL Service (Port 8082)

- `GET /api/quote` - Fetch motivational quote
- `GET /api/avatar/{email}` - Get Gravatar URL

## 🎨 Styling

### Tailwind CSS

Using Tailwind CSS v4 with minimal configuration. All styling is done through utility classes directly in components.

### Status Colors

```typescript
pending: 'bg-yellow-100 text-yellow-800'
in-progress: 'bg-blue-100 text-blue-800'
completed: 'bg-green-100 text-green-800'
```

### Priority Colors

```typescript
low: "bg-gray-100 text-gray-800";
medium: "bg-orange-100 text-orange-800";
high: "bg-red-100 text-red-800";
```

## 🛠️ Available Scripts

```bash
# Development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run ESLint
npm run lint
```

## 🧪 Testing the Frontend

1. **Start Backend Servers**

   ```bash
   cd backend
   java -cp "bin:lib/*" Main
   ```

2. **Start Frontend**

   ```bash
   cd frontend
   npm run dev
   ```

3. **Open Browser**
   - Navigate to `http://localhost:5173`
   - Create a task using the form
   - View, edit, and delete tasks
   - Check real-time notifications

## 🔄 Integration with Backend

### TCP Server Integration

The `tcpService.ts` communicates with the TCP server for all CRUD operations:

```typescript
import { tcpService } from "@/services/tcpService";

// Create task
const response = await tcpService.createTask({
  title: "New Task",
  assignee: "John Doe",
  priority: "high",
});

// Get all tasks
const tasks = await tcpService.getTasks();

// Update task
await tcpService.updateTask("task_123", { status: "completed" });

// Delete task
await tcpService.deleteTask("task_123");
```

### Error Handling

All API calls include try-catch blocks and display user-friendly error messages:

```typescript
try {
  const response = await tcpService.createTask(taskData);
  if (response.status === "success") {
    alert("Task created successfully!");
  } else {
    alert("Error: " + response.message);
  }
} catch (error) {
  alert("Network error: " + error.message);
}
```

## 🐛 Troubleshooting

### Port Already in Use

If port 5173 is busy:

```bash
# Kill process using port 5173
lsof -ti:5173 | xargs kill -9

# Or specify a different port
npm run dev -- --port 3000
```

### Backend Connection Refused

Ensure all backend servers are running:

```bash
# Check if servers are running
netstat -an | grep LISTEN | grep -E '8080|9090|8081|8082'
```

### CORS Issues

If you encounter CORS errors, the backend servers need to include CORS headers. This is handled in the backend configuration.

### Build Errors

```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install

# Clear Vite cache
rm -rf node_modules/.vite
```

### Import Aliases

Use `@/` for cleaner imports:

```typescript
// Good
import { Task } from "@/types";
import { tcpService } from "@/services/tcpService";
import TaskCard from "@/components/TaskCard";

// Avoid
import { Task } from "../../../types";
```

### Component Structure

```typescript
// 1. Imports
import { useState } from "react";
import { Task } from "@/types";

// 2. Types/Interfaces
interface ComponentProps {
  task: Task;
}

// 3. Component
export default function Component({ task }: ComponentProps) {
  // 4. State
  const [loading, setLoading] = useState(false);

  // 5. Effects
  useEffect(() => {
    // ...
  }, []);

  // 6. Handlers
  const handleClick = () => {
    // ...
  };

  // 7. Render
  return <div>{/* JSX */}</div>;
}
```

### Environment Variables

Create `.env` file for different environments:

```bash
# .env.development
VITE_TCP_API_URL=http://localhost:8080
VITE_UDP_API_URL=http://localhost:9090
VITE_NIO_API_URL=http://localhost:8081
VITE_URL_API_URL=http://localhost:8082
```

## 📚 Dependencies

### Production Dependencies

```json
{
  "axios": "^1.12.2", // HTTP client
  "lucide-react": "^0.548.0", // Icon library
  "react": "^19.1.1", // React framework
  "react-dom": "^19.1.1" // React DOM
}
```

### Development Dependencies

```json
{
  "@vitejs/plugin-react": "^5.0.4", // Vite React plugin
  "@types/react": "^19.1.16", // React types
  "@types/react-dom": "^19.1.9", // React DOM types
  "autoprefixer": "^10.4.21", // PostCSS plugin
  "eslint": "^9.36.0", // Linting
  "postcss": "^8.5.6", // CSS processing
  "tailwindcss": "^4.1.16", // Styling framework
  "typescript": "~5.9.3", // TypeScript
  "vite": "^7.1.7" // Build tool
}
```

## 🎓 Learning Resources

- [React Documentation](https://react.dev)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Tailwind CSS Docs](https://tailwindcss.com/docs)
- [Vite Guide](https://vitejs.dev/guide/)
- [Axios Documentation](https://axios-http.com/docs/intro)
