package o.f.o.com.shareofo.net.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.ClientInfoStatus;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/5/5.
 */
public class TcpClient {
    public static final int DEFAULT_PORT = 9999;
    // 超时时间，单位毫秒
    private static final int timeout = 3000;


    // private static final TcpServer TCP_SERVER = new TcpServer();
    // ShareDataRequestHandler shareDataRequestHandler;
    // List<Integer> autorizedConnections = new ArrayList<>();
    PacketHandler packetHandler;
    PacketParser packetParser;

    // 网络通信
    SocketChannel clientSocketChannel;
    TcpConnection connection; //  = new TcpConnection();
    boolean isConnected = false;

    public TcpClient() {
    }

    // public static TcpServer get() {
    //     return TCP_SERVER;
    // }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        // protocol.setPacketHandler(packetHandler);
    }

    public void setPacketParser(PacketParser packetParser) {
        this.packetParser = packetParser;
    }

    /**
     * 在指定端口监听
     *
     * @param port
     */
    public void connectServer(final String host, final int port) {
        if (isConnected) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    clientSocketChannel = SocketChannel.open(new InetSocketAddress(host, port));
                    clientSocketChannel.configureBlocking(false);

                    isConnected = true;
                    // 创建选择器
                    Selector selector = Selector.open();

                    clientSocketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    connection = new TcpConnection(clientSocketChannel, packetHandler, packetParser);
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
                                // if (key.isAcceptable()) {
                                //     // 有客户端连接请求时
                                //     protocol.handleAccept(key);
                                // }

                                if (key.isReadable()) {
                                    // 从客户端读取数据
                                    connection.handleRead(key);
                                }

                                if (key.isValid() && key.isWritable()) {
                                    // 客户端可写时
                                    connection.handleWrite(key);
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
                    close();
                }
            }
        }.start();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void close() {
        try {
            if (clientSocketChannel != null)
                clientSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isConnected = false;
            clientSocketChannel = null;
        }
    }

    public boolean sendData(int cmd, byte[] peek, RetPacketHandler handler) {
        connection.sendPack(cmd, peek, handler);
        // try {
        //     clientSocketChannel.write(ByteBuffer.wrap(peek));
        //     return true;
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
        return false;
    }
}
