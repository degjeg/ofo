package o.f.o.com.shareofo.net.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/5.
 */
public class TcpServer {

    // 超时时间，单位毫秒
    private static final int timeout = 3000;


    // private static final TcpServer TCP_SERVER = new TcpServer();
    // ShareDataRequestHandler shareDataRequestHandler;
    // List<Integer> autorizedConnections = new ArrayList<>();
    PacketHandler packetHandler;
    PacketParser packetParser;
    ConnectionListener connectionListener;

    // 网络通信
    ServerSocketChannel serverSocketChannel;
    HashMap<SocketChannel, TcpConnection> connections = new HashMap<>();

    public TcpServer() {
    }

    // public static TcpServer get() {
    //     return TCP_SERVER;
    // }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public void setPacketParser(PacketParser packetParser) {
        this.packetParser = packetParser;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    /**
     * 接收一个SocketChannel的处理
     *
     * @param key
     * @throws IOException
     */

    void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(TcpConnection.bufferSize));

        TcpConnection connection = new TcpConnection(clientChannel,
                packetHandler, packetParser
        );
        L.get().e(connection.TAG, "new connection:" + connection);
        connections.put(clientChannel, connection);

        connection.setConnectionListener(connectionListener);
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
                        TcpConnection connection = null;
                        while (keyIter.hasNext()) {
                            SelectionKey key = keyIter.next();


                            try {
                                if (key.isAcceptable()) {
                                    // 有客户端连接请求时
                                    handleAccept(key);
                                    continue;
                                }

                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                connection = connections.get(socketChannel);
                                if (connection == null) {
                                    continue;
                                }
                                if (!key.isValid()) {
                                    connection.close();
                                    connections.remove(socketChannel);
                                } else {
                                    if (key.isReadable()) {
                                        // 从客户端读取数据
                                        connection.handleRead(key);
                                    }

                                    if (key.isWritable()) {
                                        // 客户端可写时
                                        connection.handleWrite(key);
                                    }
                                }
                            } catch (Exception ex) {
                                L.get().e(TcpConnection.TAG_, "", ex);
                                // 出现IO异常（如客户端断开连接）时移除处理过的键
                                if (connection != null) {
                                    connection.close();
                                    connections.remove(key.channel());
                                }
                            } finally {
                                keyIter.remove();
                            }
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

            Iterator<TcpConnection> it = connections.values().iterator();

            while (it.hasNext()) {
                TcpConnection connection = it.next();
                connection.close();
            }

            connections.clear();
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
