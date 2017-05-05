package o.f.o.com.shareofo.net.common;

import java.nio.channels.SocketChannel;

/**
 * Created by Administrator on 2017/5/5.
 */
public interface PacketHandler {
    void handlePack(Pack pack, SocketChannel key);
}
