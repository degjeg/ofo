package o.f.o.com.shareofo.net.common;

/**
 * Created by Administrator on 2017/5/5.
 */
public interface RetPacketHandler {
    int ERR_TIMEOUT = -1;
    int ERR_CONNECT_FAIL = -2;
    int ERR_CLOSED = -3;

    void onGetReturnPacket(Packet pack);

    void onGetReturnPacketError(int code, Exception e);
}
