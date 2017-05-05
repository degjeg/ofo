package o.f.o.com.shareofo.net.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/5/5.
 */

public class Pack {
    public static final int HEADER_LEN = 6;
    public static final int MAX_PACK_LEN = 1024 * 1024;

    // header,一共6字节
    int packLen; // 4B
    short cmd;   // 2B

    // content
    byte[] packData;

    public Pack(byte[] rawHeaderData) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawHeaderData));
        try {
            packLen = dis.readInt();
            packLen = dis.readShort();
        } catch (IOException e) {
            // e.printStackTrace();
            // should happen
        }
    }

    public int getPackLen() {
        return packLen;
    }

    public int getPackContentLen() {
        return packLen - 6;
    }

    public void setPackLen(int packLen) {
        this.packLen = packLen;
    }

    public short getCmd() {
        return cmd;
    }

    public void setCmd(short cmd) {
        this.cmd = cmd;
    }

    public byte[] getPackData() {
        return packData;
    }

    public void setPackData(byte[] packData) {
        this.packData = packData;
    }
}
