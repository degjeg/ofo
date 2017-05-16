package o.f.o.com.shareofo.net.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/5/5.
 */
public class TcpClient {
    // 超时时间，单位毫秒
    private static final int timeout = 3000;


    // private static final TcpServer TCP_SERVER = new TcpServer();
    // ShareDataRequestHandler shareDataRequestHandler;
    // List<Integer> autorizedConnections = new ArrayList<>();
    PacketHandler packetHandler;
    PacketParser packetParser;

    // 网络通信
    TcpConnection connection; //  = new TcpConnection();
    boolean isConnected = false;

    ConnectionListener connectionListener;
    final Object lock = new Object();

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

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    /**
     * 在指定端口监听
     *
     * @param port
     */
    public void connectServer(final String host, final int port) {
        synchronized (lock) {
            if (isConnected) {
                return;
            }
            isConnected = true;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    SocketChannel clientSocketChannel = SocketChannel.open(new InetSocketAddress(host, port));
                    clientSocketChannel.configureBlocking(false);


                    connection = new TcpConnection(clientSocketChannel, packetHandler, packetParser);


                    TcpConnectionManager connectionManager = new TcpConnectionManager("cli");
                    connectionManager.registerConnection(connection);
                    connection.setConnectionListener(connectionListener);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // close();
                }
            }
        }.start();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void close() {
        try {
            synchronized (lock) {
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isConnected = false;
            connection = null;
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

    public TcpConnection getConnection() {
        return connection;
    }
}
