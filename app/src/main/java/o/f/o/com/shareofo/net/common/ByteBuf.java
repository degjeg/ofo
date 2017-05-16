package o.f.o.com.shareofo.net.common;

/**
 * Created by Administrator on 2017/5/16.
 */

public class ByteBuf {
    final byte[] buf;

    int pos1 = 0;
    int pos2 = 0;

    int mark1 = -1;
    int mark2 = -1;

    public ByteBuf(int capacity) {
        this.buf = new byte[capacity];
    }

    public void mark() {
        mark1 = pos1;
        mark2 = pos2;
    }

    public void reset() {
        pos1 = mark1;
        pos2 = mark2;
        mark1 = -1;
        mark2 = -1;
    }

    public void rewind() {
        if (pos1 > 0) {
            int len = pos2 - pos1;
            System.arraycopy(buf, pos1, buf, 0, len);
            mark1 = -1;
            mark2 = -1;
            pos1 = 0;
            pos2 = len;
        }
    }

    public void put(byte[] d) {
        put(d, 0, d.length);
    }

    public void put(byte[] d, int p, int len) {
        System.arraycopy(d, p, buf, pos2, len);
        pos2 += len;
    }

    public void get(byte[] d) {
        get(d, 0, d.length);
    }

    public void get(byte[] d, int p, int len) {
        System.arraycopy(buf, pos1, d, p, len);
        pos1 += len;
    }

    public int length() {
        return pos2 - pos1;
    }

    public void clear() {
        pos1 = 0;
        pos2 = 0;
        mark1 = -1;
        mark2 = -1;
    }


}
