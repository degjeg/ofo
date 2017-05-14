package o.f.o.com.shareofo.net.common;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/12.
 */

public class TcpConnection {
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
    int headerLen;

    Packet packet = new Packet();
    ConnectionListener connectionListener;

    // final Queue<DefRetPackHandler> pendingPackets = new LinkedBlockingDeque<>();
    final ArrayList<DefRetPackHandler> pendingPackets = new ArrayList<>();


    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public TcpConnection(SocketChannel channel, PacketHandler packetHandler, PacketParser packetParser) {
        this.channel = channel;
        setPacketHandler(packetHandler);
        setPacketParser(packetParser);
        this.id = ++ID;

        TAG = String.format("tcp-con[%d]", id);
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public void setPacketParser(PacketParser packetParser) {
        this.packetParser = packetParser;
        headerLen = packetParser.getHeaderLen();
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

    public void sendPack(Packet reqPack, RetPacketHandler handler) {
        synchronized (pendingPackets) {
            pendingPackets.add(new DefRetPackHandler(handler, reqPack));
        }

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

                    while (pendingPackets.size() > 0) {
                        DefRetPackHandler h = pendingPackets.remove(0);
                        if (h != null && h.handler != null) {
                            h.handler.onGetReturnPacketError(RetPacketHandler.ERR_CLOSED, null);
                        }
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

        MyByteBuffer myByteBuffer = new MyByteBuffer(buffer);
        // buffer.clear();

        // 读取信息获得读取的字节数
        long bytesRead = clientChannel.read(buffer);


        L.get().e(TAG, "received:" + bytesRead + " bytes");
        if (bytesRead == -1) {
            // 没有读取到内容的情况
            // close();
            throw new ClosedChannelException();
        }

        myByteBuffer.forRead();
        while (buffer.remaining() >= headerLen) {
            byte[] rawHeaderData = new byte[headerLen];
            buffer.get(rawHeaderData); // 读出包头

            packetParser.parseHeader(rawHeaderData, packet);
            if (packet.getPackContentLen() > 100 * 1024) {
                buffer.clear();
                return;
            }

            if (buffer.remaining() < packet.getPackContentLen()) {
                buffer.position(buffer.position() - headerLen); // 把已经读出的包头放回去
                myByteBuffer.rewind();
                return;
            }

            if (packet.getPackContentLen() > 0) { // 读出包体内容
                byte[] packData = new byte[packet.getPackContentLen()];
                buffer.get(packData);
                packet.setPackData(packData);
            }

            handlePack(packet);

            packet = new Packet();
        }
        myByteBuffer.rewind();

        // 设置为下一次读取或是写入做准备
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void handlePack(Packet packet) {
        L.get().e(TAG, "recv a pack:" + packet.toString1());
        for (int i = 0; i < pendingPackets.size(); i++) {
            if (pendingPackets.get(i).hasSent
                    && pendingPackets.get(i).reqPack.reqCode == packet.reqCode) {
                if (pendingPackets.get(i).handler != null) {
                    pendingPackets.get(i).handler.onGetReturnPacket(packet);
                }

                pendingPackets.remove(i--);
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
            channel.write(ByteBuffer.wrap(pack.reqPack.toByteArray()));
            pack.hasSent = true;
            L.get().e(TAG, "sent a pack:" + pack.reqPack.toString1());
        }
    }

    @Override
    public String toString() {
        return String.format("[%d]", id);
    }

    private static class DefRetPackHandler {
        RetPacketHandler handler;
        Packet reqPack;
        boolean hasSent = false;

        public DefRetPackHandler(RetPacketHandler handler, Packet reqPack) {
            this.handler = handler;
            this.reqPack = reqPack;
        }

        public void onGetReturnPacket(Packet pack) {
            handler.onGetReturnPacket(pack);
        }
    }
}
