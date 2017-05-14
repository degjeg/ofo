package o.f.o.com.shareofo.net.common;

/**
 * Created by Administrator on 2017/5/5.
 */

public interface ConnectionListener {
    void onConnected(TcpConnection connection);

    void onClosed(TcpConnection connection);
}
