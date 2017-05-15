package o.f.o.com.shareofo.net.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by Administrator on 2017/5/5.
 */

public class Packet {
    public static final int HEADER_LEN = 12;
    public static final int MAX_PACK_LEN = 1024 * 1024;

    // header,一共6字节
    int packLen; // 4B, 包长,包括包头和包体
    short cmd;   // 2B
    short reqCode;   // 2B


    // content
    byte[] packData;
    int checkBit; // hash 检验

    public Packet() {
    }


    public int getPackLen() {
        return packLen;
    }

    public int getPackContentLen() {
        return packLen - HEADER_LEN;
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

    public int getCheckBit() {
        return checkBit;
    }

    public void setCheckBit(int checkBit) {
        this.checkBit = checkBit;
    }

    public byte[] getPackData() {
        return packData;
    }

    public void setPackData(byte[] packData) {
        this.packData = packData;
        packLen = HEADER_LEN; //  + ((packData == null) ? 0 : packData.length);
        if (packData != null) {
            packLen += packData.length;
            checkBit = Arrays.hashCode(packData);
        }
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dis = new DataOutputStream(bos);

        try {
            dis.writeInt(packLen);
            dis.writeShort(cmd);
            dis.writeShort(reqCode);
            dis.writeInt(checkBit);

            if (packData != null) {
                dis.write(packData);
            }
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
                .append(",len:")
                .append(getPackContentLen())
                .append(",")
                // .append(packData == null ? "" : new String(packData))
                .toString();
    }
}
