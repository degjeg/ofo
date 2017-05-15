package o.f.o.com.shareofo.net.common;

/**
 * Created by Administrator on 2017/5/5.
 */
public interface SendPacketHandler {
    void onPackSent(Packet pack, TcpConnection connection);
    void onError(int code, Exception e);
}
