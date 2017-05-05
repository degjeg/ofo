package o.f.o.com.shareofo.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import o.f.o.com.shareofo.net.bean.ShareRequestRequest;
import o.f.o.com.shareofo.net.common.ConnectionListener;
import o.f.o.com.shareofo.net.common.Pack;
import o.f.o.com.shareofo.net.common.PacketHandler;
import o.f.o.com.shareofo.net.common.TcpClient;
import o.f.o.com.shareofo.net.common.TcpServer;
import o.f.o.com.shareofo.net.handlers.ShareDataRequestHandler;

/**
 * Created by Administrator on 2017/5/5.
 */

public class ShareOfoClient implements PacketHandler, ConnectionListener {

    private static final ShareOfoClient shareOfoClient = new ShareOfoClient();

    final TcpClient tcpClient;
    final Queue<byte[]> pendingPack = new LinkedBlockingDeque<>();


    private ShareOfoClient() {
        tcpClient = new TcpClient();
        tcpClient.setPacketHandler(this);
    }

    public static ShareOfoClient get() {
        return shareOfoClient;
    }

    public void requestShareData(String host, int port) {
        tcpClient.connectServer(host, port);
        S
    }

    public void sendData(byte[] data) {
        if (data != null) {
            pendingPack.offer(data);
        }

        if (tcpClient.isConnected()) {
            while (!pendingPack.isEmpty() && tcpClient.sendData(pendingPack.poll())) ;
        }
    }

    @Override
    public void handlePack(Pack pack, SocketChannel key) {

    }

    @Override
    public void onConnected(SocketChannel socketChannel) {

    }

    @Override
    public void onClosed(SocketChannel socketChannel) {

    }
}
