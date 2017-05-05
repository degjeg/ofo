package o.f.o.com.shareofo.net.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/5/5.
 */
public class TcpServer {
    public static final int DEFAULT_PORT = 9999;
    // 超时时间，单位毫秒
    private static final int timeout = 3000;


    // private static final TcpServer TCP_SERVER = new TcpServer();
    // ShareDataRequestHandler shareDataRequestHandler;
    // List<Integer> autorizedConnections = new ArrayList<>();
    PacketHandler packetHandler;

    // 网络通信
    ServerSocketChannel serverSocketChannel;
    TcpProtocol protocol = new TcpProtocol();

    public TcpServer() {
    }

    // public static TcpServer get() {
    //     return TCP_SERVER;
    // }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        protocol.setPacketHandler(packetHandler);
    }

    public void startServer() {
        startServer(DEFAULT_PORT);
    }

    /**
     * 在指定端口监听
     *
     * @param port
     */
    public void startServer(final int port) {
        if (serverSocketChannel != null) return;
        // 短时间调用2次暂时不管

        new Thread() {
            @Override
            public void run() {
                try {
                    serverSocketChannel = ServerSocketChannel.open();
                    serverSocketChannel.socket().bind(new InetSocketAddress(port));
                    serverSocketChannel.configureBlocking(false);
                    // 创建选择器
                    Selector selector = Selector.open();

                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                    // 反复循环,等待IO
                    while (true) {
                        // 等待某信道就绪(或超时)
                        if (selector.select(timeout) == 0) {
                            System.out.print("独自等待.");
                            continue;
                        }

                        // 取得迭代器.selectedKeys()中包含了每个准备好某一I/O操作的信道的SelectionKey
                        Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

                        while (keyIter.hasNext()) {
                            SelectionKey key = keyIter.next();

                            try {
                                if (key.isAcceptable()) {
                                    // 有客户端连接请求时
                                    protocol.handleAccept(key);
                                }

                                if (key.isReadable()) {
                                    // 从客户端读取数据
                                    protocol.handleRead(key);
                                }

                                if (key.isValid() && key.isWritable()) {
                                    // 客户端可写时
                                    protocol.handleWrite(key);
                                }
                            } catch (IOException ex) {
                                // 出现IO异常（如客户端断开连接）时移除处理过的键
                                keyIter.remove();
                                continue;
                            }

                            // 移除处理过的键
                            keyIter.remove();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    stopServer();
                }
            }
        }.start();
    }

    public void stopServer() {
        try {
            if (serverSocketChannel != null)
                serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocketChannel = null;
        }

    }

    // public void reject(int connectionId) {

    // }

    // public void agree(int connectionId) {
    //      autorizedConnections.add(connectionId);
    //  }
}
