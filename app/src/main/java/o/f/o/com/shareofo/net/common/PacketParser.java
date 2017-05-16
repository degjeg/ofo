package o.f.o.com.shareofo.net.common;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/5/12.
 */

public interface PacketParser {
    // int getHeaderLen();

    Packet parsePacket(ByteBuf buffer);

    // int parseHeader(byte[] rawHeaderData, Packet packet);
}
