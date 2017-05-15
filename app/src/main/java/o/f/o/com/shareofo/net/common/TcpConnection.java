package o.f.o.com.shareofo.net.common;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/12.
 */

public class TcpConnection implements Runnable {
    public static final int WAIT_TIMEOUT = 800000;


    public static final String TAG_ = "tcp-";
    public final String TAG; // = "tcp-";

    public static int ID = 1;
    public static int REQ_CODE = 1;
    int id;
    SocketChannel channel;


    public static int bufferSize = 1024;

    ByteBuffer recvCache = ByteBuffer.allocate(100 * 1024);
    PacketHandler packetHandler;
    PacketParser packetParser;


    ConnectionListener connectionListener;
    HandlerThread checkTimeoutThread; //  = new HandlerThread("chk_timeout");
    Handler handler;

    // final Queue<DefRetPackHandler> pendingPackets = new LinkedBlockingDeque<>();
    final List<DefRetPackHandler> pendingPackets = Collections.synchronizedList(new ArrayList<DefRetPackHandler>());


    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public TcpConnection(SocketChannel channel, PacketHandler packetHandler, PacketParser packetParser) {
        this.channel = channel;
        setPacketHandler(packetHandler);
        setPacketParser(packetParser);
        this.id = ++ID;

        TAG = String.format("tcp-con[%d]", id);

        checkTimeoutThread = new HandlerThread("chk_timeout");
        checkTimeoutThread.start();
        handler = new Handler(checkTimeoutThread.getLooper());
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public void setPacketParser(PacketParser packetParser) {
        this.packetParser = packetParser;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        if (connectionListener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (TcpConnection.this.connectionListener != null) {
                        TcpConnection.this.connectionListener.onConnected(TcpConnection.this);
                    }
                }
            });
        }
    }

    public void sendPack(int cmd, byte[] data, RetPacketHandler handler) {
        Packet reqPack = new Packet();

        reqPack.setCmd((short) cmd);
        reqPack.setReqCode((short) ++REQ_CODE);
        reqPack.setPackData(data);

        sendPack(reqPack, handler);
        // channel.k.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void sendPack(Packet reqPack, RetPacketHandler handler) {
        synchronized (pendingPackets) {
            pendingPackets.add(new DefRetPackHandler(handler, reqPack));
        }
        this.handler.postDelayed(this, WAIT_TIMEOUT);

        new Thread() {
            @Override
            public void run() {
                try {

                    handleWrite(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void sendPack(int cmd, String data, RetPacketHandler handler) {
        try {
            sendPack(cmd, data == null ? null : data.getBytes("utf-8"), handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            channel.close();
            // L.get().e(TAG, "close", new Exception());

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (TcpConnection.this.connectionListener != null) {
                        TcpConnection.this.connectionListener.onClosed(TcpConnection.this);
                        TcpConnection.this.connectionListener = null;
                    }

                    synchronized (pendingPackets) {
                        while (pendingPackets.size() > 0) {
                            DefRetPackHandler h = pendingPackets.remove(0);
                            if (h != null
                                    && h.handler != null
                                    && (h.handler instanceof RetPacketHandler)) {
                                h.handler.onError(RetPacketHandler.ERR_CLOSED, null);
                            }
                        }
                        pendingPackets.clear();
                    }
                }
            });
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * 从一个SocketChannel读取信息的处理
     *
     * @param key
     * @throws IOException
     */
    void handleRead(SelectionKey key) throws IOException {

        // 获得与客户端通信的信道
        SocketChannel clientChannel = (SocketChannel) key.channel();

        if (clientChannel == null) return;

        // 得到并清空缓冲区
        ByteBuffer buffer = recvCache; // (ByteBuffer) key.attachment();

        if (buffer == null) return;


        // buffer.clear();

        // 读取信息获得读取的字节数
        long bytesRead = clientChannel.read(buffer);


        L.get().e(TAG, "received:" + bytesRead + " bytes");
        if (bytesRead == -1) {
            // 没有读取到内容的情况
            // close();
            throw new ClosedChannelException();
        }


        do {
            Packet packet = null;
            try {
                packet = packetParser.parsePacket(buffer);
            } catch (Exception e) {
                L.get().e(TAG, "parsed failed", e);
                break;
            }

            if (packet != null) {
                L.get().e(TAG, "after parse:" + packet.getPackLen() + "+" + buffer.position() + "/" + bytesRead);
                handlePack(packet);
            } else {
                break;
            }
        } while (true);

        // 设置为下一次读取或是写入做准备
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void handlePack(Packet packet) {
        L.get().e(TAG, "recv a pack:" + packet.toString1());

        synchronized (pendingPackets) {
            for (int i = 0; i < pendingPackets.size(); i++) {
                if (pendingPackets.get(i).hasSent
                        && pendingPackets.get(i).reqPack.reqCode == packet.reqCode) {
                    if (pendingPackets.get(i).handler != null && (pendingPackets.get(i).handler instanceof RetPacketHandler)) {
                        ((RetPacketHandler) pendingPackets.get(i).handler).onGetReturnPacket(packet);
                    }

                    pendingPackets.remove(i--);
                }
            }
        }

        if (packetHandler != null) packetHandler.handlePack(packet, this);
    }

    /**
     * 向一个SocketChannel写入信息的处理
     *
     * @param key
     * @throws IOException
     */
    void handleWrite(SelectionKey key) throws IOException {
        // L.get().e(TAG, "handleWrite key" + key);

        DefRetPackHandler pack = null;
        synchronized (pendingPackets) {
            for (int i = 0; i < pendingPackets.size(); i++) {
                if (!pendingPackets.get(i).hasSent) {
                    pack = pendingPackets.get(i);
                    break;
                }
            }
        }
        if (pack != null) {
            // if ((pack.reqPack.reqCode %2)==0) {
            //     pack.reqPack.getPackData()[0] = (byte) (1+pack.reqPack.getPackData()[0]);
            // }
            channel.write(ByteBuffer.wrap(pack.reqPack.toByteArray()));
            // channel.write(ByteBuffer.wrap(((new Random().nextDouble()%20)+"").getBytes()));
            pack.hasSent = true;
            // L.get().e(TAG, "sent a pack:" + pack.reqPack.toString1());

            if (pack.handler == null || !(pack.handler instanceof RetPacketHandler)) {
                synchronized (pendingPackets) {
                    pendingPackets.remove(pack);
                }
            } else {
                pack.handler.onPackSent(pack.reqPack, this);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("[%d]", id);
    }

    @Override
    public void run() {
        synchronized (pendingPackets) {
            for (int i = 0; i < pendingPackets.size(); i++) {
                DefRetPackHandler pack = pendingPackets.get(i);
                if (pack.isTimeout()) {
                    if (pack.handler != null)
                        pack.handler.onError(-1, null);
                    pendingPackets.remove(i--);
                }
            }
        }
    }

    private static class DefRetPackHandler {
        SendPacketHandler handler;
        Packet reqPack;
        boolean hasSent = false;
        long addTime;

        public DefRetPackHandler(SendPacketHandler handler, Packet reqPack) {
            this.handler = handler;
            this.reqPack = reqPack;
            addTime = SystemClock.elapsedRealtime();
        }

        public boolean isTimeout() {
            return (SystemClock.elapsedRealtime() - addTime) > WAIT_TIMEOUT;
        }
    }
}
