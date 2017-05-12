package o.f.o.com.shareofo.net.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/5/5.
 */

public class Packet {
    public static final int HEADER_LEN = 8;
    public static final int MAX_PACK_LEN = 1024 * 1024;

    // header,一共6字节
    int packLen; // 4B, 包长,包括包头和包体
    short cmd;   // 2B
    short reqCode;   // 2B

    // content
    byte[] packData;

    public Packet() {
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

    public short getReqCode() {
        return reqCode;
    }

    public void setReqCode(short reqCode) {
        this.reqCode = reqCode;
    }

    public byte[] getPackData() {
        return packData;
    }

    public void setPackData(byte[] packData) {
        this.packData = packData;
        packLen = HEADER_LEN + ((packData == null) ? 0 : packData.length);
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dis = new DataOutputStream(bos);

        try {
            dis.writeInt(packLen);
            dis.writeShort(cmd);
            dis.writeShort(reqCode);
            if (packData != null) dis.write(packData);
        } catch (IOException e) {
            // e.printStackTrace();
            // should happen
        }

        return bos.toByteArray();
    }

    public String toString1() {
        return new StringBuilder()
                .append("[")
                .append(cmd)
                .append("]")
                .append(reqCode)
                .append(",")
                .append(packData == null ? "" : new String(packData))
                .toString();
    }
}
