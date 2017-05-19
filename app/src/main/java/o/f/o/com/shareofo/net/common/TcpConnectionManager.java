package o.f.o.com.shareofo.net.common;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;

import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/16.
 */

public class TcpConnectionManager {
    int timeout = 5000;

    Thread sendThread;
    Thread receiveThread;

    SelectionKey sendSelectionKey;
    SelectionKey receiveSelectionKey;

    // 创建选择器
    Selector sendSelector = Selector.open();
    Selector receiveSelector = Selector.open();

    final HashMap<SocketChannel, TcpConnection> connections = new HashMap<>();

    public TcpConnectionManager(String name) throws IOException {
        sendSelector = SelectorProvider.provider().openSelector();
        receiveSelector = SelectorProvider.provider().openSelector();

        sendThread = new Thread(new MSelector(sendSelector), name+"send");
        receiveThread = new Thread(new MSelector(receiveSelector),name+ "receive");

        sendThread.start();
        receiveThread.start();
    }

    public void registerConnection(TcpConnection connection) throws ClosedChannelException {
        // L.get().e("tcp-", "registerConnection111");
        SelectionKey sendSelectionKey = connection.channel.register(sendSelector, SelectionKey.OP_WRITE /*| SelectionKey.OP_WRITE*/);
        SelectionKey receiveSelectionKey = connection.channel.register(receiveSelector, SelectionKey.OP_READ /*| SelectionKey.OP_WRITE*/);

        connection.setReceiveSelectionKey(receiveSelectionKey);
        connection.setSendSelectionKey(sendSelectionKey);

        connections.put(connection.channel, connection);
        // L.get().e("tcp-", "registerConnection222");
    }


    private class MSelector implements Runnable {

        Selector selector;

        public MSelector(Selector sendSelector) {
            this.selector = sendSelector;
        }

        @Override
        public void run() {
            // 反复循环,等待IO
            while (true) {
                try {
                    // 等待某信道就绪(或超时)
                    if (selector.select(100) == 0) {
                        System.out.print("独自等待.");
                        // Thread.sleep(3000);
                        continue;
                    }

                    // 取得迭代器.selectedKeys()中包含了每个准备好某一I/O操作的信道的SelectionKey
                    Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

                    while (keyIter.hasNext()) {
                        SelectionKey key = keyIter.next();

                        TcpConnection connection = null;
                        try {
                            // if (key.isAcceptable()) {
                            //     // 有客户端连接请求时
                            //     protocol.handleAccept(key);
                            // }

                            SocketChannel channel = (SocketChannel) key.channel();
                            connection = connections.get(channel);

                            if (connection != null) {
                                if (key.isReadable()) {
                                    // 从客户端读取数据
                                    connection.handleRead(key);
                                }

                                if (key.isValid() && key.isWritable()) {
                                    // 客户端可写时
                                    connection.handleWrite(key);
                                }
                            } else {
                                channel.close();
                                L.get().e("tcp-", "force close :" + channel);
                            }
                        } catch (IOException ex) {
                            // 出现IO异常（如客户端断开连接）时移除处理过的键
                            if (connection != null) {
                                connection.close();
                                synchronized (connections) {
                                    connections.remove(connection.channel);
                                }
                            }
                        } catch (Exception ex) {
                            // 出现其它异常,保留连接
                            continue;
                        } finally {
                            // 移除处理过的键
                            keyIter.remove();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
