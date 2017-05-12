package o.f.o.com.shareofo.net;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import o.f.o.com.shareofo.net.common.ConnectionListener;
import o.f.o.com.shareofo.net.common.Packet;
import o.f.o.com.shareofo.net.common.PacketHandler;
import o.f.o.com.shareofo.net.common.RetPacketHandler;
import o.f.o.com.shareofo.net.common.TcpClient;
import o.f.o.com.shareofo.net.common.TcpConnection;
import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/5.
 */

public class ShareOfoClient implements PacketHandler, ConnectionListener {

    private static final ShareOfoClient shareOfoClient = new ShareOfoClient();

    final TcpClient tcpClient;


    private ShareOfoClient() {
        tcpClient = new TcpClient();
        tcpClient.setPacketHandler(this);
        tcpClient.setPacketParser(new PacketParser());
    }

    public static ShareOfoClient get() {
        return shareOfoClient;
    }

    public void requestShareData(String host, int port) {
        if (tcpClient.isConnected()) {
            sendTest();
        } else {
            tcpClient.connectServer(host, port);
        }

    }

    @Override
    public void handlePack(Packet pack, TcpConnection connection) {
        L.get().e(TcpConnection.TAG, "handlePack:" + pack.toString1());
    }

    @Override
    public void onConnected(SocketChannel socketChannel) {
        sendTest();
    }

    private void sendTest() {
        for (int i = 0; i < 1; i++) {
            tcpClient.sendData(1, ("Hello" + (i + 1)).getBytes(), new RetPacketHandler() {
                @Override
                public void onGetReturnPacket(Packet pack) {
                    L.get().e(TcpConnection.TAG, "onGetReturnPacket:" + pack.toString1());
                }

                @Override
                public void onGetReturnPacketError(int code, Exception e) {
                    L.get().e(TcpConnection.TAG, "onGetReturnPacketError:" + code, e);

                }
            });
        }
    }

    @Override
    public void onClosed(SocketChannel socketChannel) {

    }
}
