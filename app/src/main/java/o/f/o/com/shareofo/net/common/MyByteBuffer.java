package o.f.o.com.shareofo.net.common;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/5/5.
 */
public class MyByteBuffer {
    ByteBuffer byteBuffer;

    int pos, limit;

    public MyByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public void forRead() {
        pos = byteBuffer.position();
        limit = byteBuffer.limit();
        byteBuffer.flip();
    }

    /**
     * 直接转为写,已经读掉的将会重新被读出(保留已经读出的数据)
     */
    public void forWrite() {
        byteBuffer.position(pos);
        byteBuffer.limit(limit);
    }

    /**
     * 转为写, 将未读完的数据放到缓冲区开始处(去掉已经读出的数据)
     */
    public void rewind() {
        int remaining = byteBuffer.remaining();
        if (remaining == 0) {
            byteBuffer.clear();
            return;
        }

        byte[] d = new byte[remaining];
        byteBuffer.get(d);
        byteBuffer.clear();
        byteBuffer.put(d);
    }
}
