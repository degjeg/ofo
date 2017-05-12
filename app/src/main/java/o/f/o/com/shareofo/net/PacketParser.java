package o.f.o.com.shareofo.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import o.f.o.com.shareofo.net.common.Packet;

/**
 * Created by Administrator on 2017/5/12.
 */

public class PacketParser implements o.f.o.com.shareofo.net.common.PacketParser {
    @Override
    public int getHeaderLen() {
        return 8;
    }

    @Override
    public int parsePacket(Packet packet) {

        return 0;
    }

    @Override
    public int parseHeader(byte[] rawHeaderData, Packet packet) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawHeaderData));
        try {

            packet.setPackLen(dis.readInt());
            packet.setCmd(dis.readShort());
            packet.setReqCode(dis.readShort());
        } catch (IOException e) {
            // e.printStackTrace();
            // should happen
        }
        return 0;
    }
}
