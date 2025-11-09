import NotificationBell from "./components/NotificationBell";
import TaskList from "./components/TaskList";
import ProfileCard from "./components/ProfileCard";
import FileUpload from "./components/FileUpload";

function App() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 flex flex-col">
      {/* Header */}
      <header className="bg-white/80 backdrop-blur-sm shadow-sm border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
                NetStream TaskManager
              </h1>
              <p className="text-sm text-slate-600 mt-1">
                Real-time collaborative task management
              </p>
            </div>
            <NotificationBell />
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Task List - Takes 2 columns */}
          <div className="lg:col-span-2">
            <TaskList />
          </div>

          {/* Sidebar - Takes 1 column */}
          <div className="space-y-6">
            <ProfileCard />
            <FileUpload />
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white/80 backdrop-blur-sm border-t border-slate-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="text-center text-sm text-slate-500">
            <p className="font-medium text-slate-700 mb-2">
              NetStream TaskManager
            </p>
            <div className="flex justify-center gap-4 flex-wrap">
              <span className="inline-flex items-center">
                <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                TCP:8080
              </span>
              <span className="inline-flex items-center">
                <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                UDP:9090
              </span>
              <span className="inline-flex items-center">
                <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                NIO:8081
              </span>
              <span className="inline-flex items-center">
                <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                URL:8082
              </span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
