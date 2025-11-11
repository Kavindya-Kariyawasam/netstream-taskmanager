package threading;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExceptionHandler {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void handle(Exception e) {
        String timestamp = LocalDateTime.now().format(formatter);
        String exceptionType = e.getClass().getSimpleName();
        
        System.err.println("\n" + "=".repeat(60));
        System.err.println("[!] EXCEPTION CAUGHT");
        System.err.println("=".repeat(60));
        System.err.println("Time:      " + timestamp);
        System.err.println("Type:      " + exceptionType);
        System.err.println("Message:   " + e.getMessage());
        
        // Specific handling for different exception types
        if (e instanceof SocketTimeoutException) {
            System.err.println("Reason:    Connection timeout - client didn't respond in time");
        } else if (e instanceof SocketException) {
            System.err.println("Reason:    Socket error - connection may have been reset");
        } else if (e instanceof UnknownHostException) {
            System.err.println("Reason:    Cannot resolve host address");
        } else if (e instanceof IOException) {
            System.err.println("Reason:    I/O operation failed");
        }
        
        System.err.println("\nStack Trace:");
        e.printStackTrace();
        System.err.println("=".repeat(60) + "\n");
    }

    public static void handle(Exception e, String context) {
        System.err.println("Context:   " + context);
        handle(e);
    }
}