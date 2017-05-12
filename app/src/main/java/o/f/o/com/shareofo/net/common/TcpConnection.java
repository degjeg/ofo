package o.f.o.com.shareofo.net.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/12.
 */

public class TcpConnection {
    public static final String TAG = "tcp-";

    public static int ID = 1;
    public static int REQ_CODE = 1;
    int id;
    SocketChannel channel;


    public static int bufferSize = 10 * 1024;

    PacketHandler packetHandler;
    PacketParser packetParser;
    int headerLen;

    Packet packet = new Packet();

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
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public void setPacketParser(PacketParser packetParser) {
        this.packetParser = packetParser;
        headerLen = packetParser.getHeaderLen();
    }

    public void sendPack(int cmd, byte[] data, RetPacketHandler handler) {
        Packet reqPack = new Packet();

        reqPack.setCmd((short) cmd);
        reqPack.setReqCode((short) ++REQ_CODE);
        reqPack.setPackData(data);

        synchronized (pendingPackets) {
            pendingPackets.add(new DefRetPackHandler(handler, reqPack));
        }

        // channel.k.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

    }

    public void sendPack(int cmd, String data, RetPacketHandler handler) {
        try {
            sendPack(cmd, data == null ? null : data.getBytes("utf-8"), handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void close() {

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
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        if (buffer == null) return;

        MyByteBuffer myByteBuffer = new MyByteBuffer(buffer);
        // buffer.clear();

        // 读取信息获得读取的字节数
        long bytesRead = clientChannel.read(buffer);


        if (bytesRead == -1) {
            // 没有读取到内容的情况
            clientChannel.close();
        }

        myByteBuffer.forRead();
        while (buffer.remaining() >= headerLen) {
            byte[] rawHeaderData = new byte[headerLen];
            buffer.get(rawHeaderData); // 读出包头

            packetParser.parseHeader(rawHeaderData, packet);

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

        if (packetHandler != null) packetHandler.handlePack(packet, this);

        for (int i = 0; i < pendingPackets.size(); i++) {
            if (pendingPackets.get(i).reqPack.reqCode == packet.reqCode) {
                pendingPackets.get(i).handler.onGetReturnPacket(packet);
                pendingPackets.remove(i--);
            }
        }
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
