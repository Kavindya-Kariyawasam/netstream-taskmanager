package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

public class NIOFileServer {
    private final int port;
    private final ExecutorService workerpool;
    private final Gson gson = new Gson();

    public NIOFileServer(int port, int workThreads) {
        this.port = port;
        this.workerpool = Executors.newFixedThreadPool(workThreads);
    }

    public void start() throws IOException {
        System.out.println("Starting NIO file server on port " + port);
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("NIO server listening on port " + port);

        while (true) {
            int ready  = selector.select();
            if (ready == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isValid()) continue;

                if (key.isAcceptable()) {
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    if(clientChannel != null) {
                        clientChannel.configureBlocking(true);
                        workerpool.submit(()->handleClient(clientChannel));
                    }
                }
            }
        }
    }
    private void handleClient(SocketChannel channel) {
        try{

        }catch(Exception e) {

        }
    }
    public static void main(String[] args) throws IOException {
        int port = 8081;
        int workers = 10;
        NIOFileServer server = new NIOFileServer(port, workers);
        server.start();
    }
}
