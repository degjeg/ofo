package o.f.o.com.shareofo.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import o.f.o.com.shareofo.net.bean.ShareRequestRequest;
import o.f.o.com.shareofo.net.common.Pack;
import o.f.o.com.shareofo.net.common.PacketHandler;
import o.f.o.com.shareofo.net.common.TcpServer;
import o.f.o.com.shareofo.net.handlers.ShareDataRequestHandler;

/**
 * Created by Administrator on 2017/5/5.
 */

public class ShareOfoServer implements PacketHandler {
    private static final ShareOfoServer shareOfoServer = new ShareOfoServer();

    final TcpServer tcpServer;


    private ShareDataRequestHandler shareDataRequestHandler;

    private ShareOfoServer() {
        tcpServer = new TcpServer();
        tcpServer.setPacketHandler(this);
    }

    public static ShareOfoServer get() {
        return shareOfoServer;
    }

    public void startServer() {
        tcpServer.startServer();
    }

    @Override
    public void handlePack(Pack pack, SocketChannel key) {
        switch (pack.getCmd()) {
            case Cmds.SHARE_DATA_REQ:
                handleShareDataRequest(pack, key);
                break;
        }
    }

    private void handleShareDataRequest(Pack pack, SocketChannel key) {
        try {
            ShareRequestRequest request = ShareRequestRequest.ADAPTER.decode(pack.getPackData());
            shareDataRequestHandler.onShardDataRequest(request, key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reject(SocketChannel key) {

    }

    public void agree(SocketChannel key) {

    }

    public void setShareDataRequestHandler(ShareDataRequestHandler shareDataRequestHandler) {
        this.shareDataRequestHandler = shareDataRequestHandler;
    }


}
