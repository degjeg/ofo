package o.f.o.com.shareofo.net.handlers;

import java.nio.channels.SocketChannel;

import o.f.o.com.shareofo.net.bean.ShareRequestRequest;

/**
 * Created by Administrator on 2017/5/5.
 */

public interface ShareDataRequestHandler {
    void onShardDataRequest(ShareRequestRequest request, SocketChannel key);
}
