import { Bell } from "lucide-react";
import TaskList from "./components/TaskList";

function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                NetStream TaskManager
              </h1>
              <p className="text-sm text-gray-600 mt-1">
                Real-time collaborative task management
              </p>
            </div>
            <button className="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-full transition-colors">
              <Bell className="w-6 h-6" />
              <span className="absolute top-1 right-1 block h-2 w-2 rounded-full bg-red-500 ring-2 ring-white"></span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <TaskList />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="text-center text-sm text-gray-500">
            <p>NetStream TaskManager - Network Programming Project</p>
            <p className="mt-1">
              TCP Server (Port 8080) | UDP Server (Port 9090) | NIO Server (Port
              8081)
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
