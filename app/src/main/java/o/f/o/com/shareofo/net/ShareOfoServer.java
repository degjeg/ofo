package o.f.o.com.shareofo.net;

import java.nio.channels.SocketChannel;

import o.f.o.com.shareofo.net.common.ConnectionListener;
import o.f.o.com.shareofo.net.common.Packet;
import o.f.o.com.shareofo.net.common.PacketHandler;
import o.f.o.com.shareofo.net.common.TcpConnection;
import o.f.o.com.shareofo.net.common.TcpServer;
import o.f.o.com.shareofo.net.handlers.ShareDataRequestHandler;
import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/5.
 */

public class ShareOfoServer implements PacketHandler, ConnectionListener {
    public static final int PORT = 9999;


    private static final ShareOfoServer shareOfoServer = new ShareOfoServer();

    final TcpServer tcpServer;


    private ShareDataRequestHandler shareDataRequestHandler;

    private ShareOfoServer() {
        tcpServer = new TcpServer();
        tcpServer.setPacketHandler(this);
        tcpServer.setPacketParser(new PacketParser());
        tcpServer.setConnectionListener(this);
    }

    public static ShareOfoServer get() {
        return shareOfoServer;
    }

    public void startServer() {
        tcpServer.startServer(PORT);
    }

    @Override
    public void handlePack(Packet pack, TcpConnection connection) {
        switch (pack.getCmd()) {
            case Cmds.SHARE_DATA_REQ:
                handleShareDataRequest(pack, connection);
                break;
        }
    }

    private void handleShareDataRequest(Packet pack, TcpConnection connection) {
        L.get().e(connection.TAG, "handlePack:" + pack.toString1());

        Packet retPack = new Packet();
        retPack.setCmd(pack.getCmd());
        retPack.setReqCode(pack.getReqCode());
        retPack.setPackData("World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:World:".getBytes());
        connection.sendPack(retPack, null);
        // try {
        //     ShareRequestRequest request = ShareRequestRequest.ADAPTER.decode(pack.getPackData());
        //     shareDataRequestHandler.onShardDataRequest(request, connection);
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    public void reject(SocketChannel key) {

    }

    public void agree(SocketChannel key) {

    }

    public void setShareDataRequestHandler(ShareDataRequestHandler shareDataRequestHandler) {
        this.shareDataRequestHandler = shareDataRequestHandler;
    }


    @Override
    public void onConnected(TcpConnection connection) {
        L.get().e(connection.TAG, "connected on server");
    }

    @Override
    public void onClosed(TcpConnection connection) {
        L.get().e(connection.TAG, "closed  on server");
    }
}
