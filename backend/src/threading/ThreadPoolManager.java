package threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
    private static ExecutorService threadPool;
    private static final int THREAD_POOL_SIZE = 50;

    public static synchronized ExecutorService getThreadPool() {
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            System.out.println("✅ Thread pool initialized with " + THREAD_POOL_SIZE + " threads");
        }
        return threadPool;
    }

    public static void shutdown() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            System.out.println("🛑 Thread pool shut down");
        }
    }
}