package o.f.o.com.shareofo.net.common;

/**
 * Created by Administrator on 2017/5/12.
 */

public interface PacketParser {
    int getHeaderLen();

    int parsePacket(Packet packet);

    int parseHeader(byte[] rawHeaderData, Packet packet);
}
