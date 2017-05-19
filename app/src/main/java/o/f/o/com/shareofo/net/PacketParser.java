package o.f.o.com.shareofo.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import o.f.o.com.shareofo.net.common.ByteBuf;
import o.f.o.com.shareofo.net.common.MyByteBuffer;
import o.f.o.com.shareofo.net.common.Packet;
import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/12.
 */

public class PacketParser implements o.f.o.com.shareofo.net.common.PacketParser {
    Packet packet = new Packet();

    @Override
    public Packet parsePacket(ByteBuf buffer) {

        int HEADER_LEN = packet.HEADER_LEN;

        // 1.不足包头的长度
        if (buffer.length() < HEADER_LEN) {
            return null;
        }

        byte[] rawHeaderData = new byte[HEADER_LEN];
        buffer.mark();

        buffer.get(rawHeaderData); // 读出包头

        Packet packet = this.packet;

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawHeaderData));
        try {

            packet.setPackLen(dis.readInt());
            packet.setCmd(dis.readShort());
            packet.setReqCode(dis.readShort());
            packet.setRetCode(dis.readShort());
            packet.setCheckBit(dis.readInt());
        } catch (IOException e) {
            // e.printStackTrace();
            // should happen
        }

        // 返回小于0的数字代表异常情况,包太长,不符合规范
        if (packet.getPackContentLen() > 100 * 1024) {
            buffer.clear();
            throw new RuntimeException(String.format("Invalid pack length:%d", packet.getPackContentLen()));
        }

        // 包还没有接收完
        if (buffer.length() < packet.getPackContentLen()) {
            buffer.reset();
            return null; //  2;
        }

        if (packet.getPackContentLen() > 0) { // 读出包体内容
            byte[] packData = new byte[packet.getPackContentLen()];
            buffer.get(packData);

            if (Arrays.hashCode(packData) != packet.getCheckBit()) {
                buffer.clear();
                // L.get().e("tcp-", "content received error:" + Arrays.toString(packData));
                throw new RuntimeException("check failed");
            } else {
                // L.get().e("tcp-", "content received right:" + Arrays.toString(packData));
            }
            packet.setPackData(packData);
        }

        buffer.rewind();

        this.packet = new Packet();
        return packet;
    }
}
