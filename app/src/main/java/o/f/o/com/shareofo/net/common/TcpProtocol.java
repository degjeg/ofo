package o.f.o.com.shareofo.net.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by Administrator on 2017/5/5.
 */

public class TcpProtocol {
    protected int bufferSize = 10 * 1024;

    PacketHandler packetHandler;

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    /**
     * 接收一个SocketChannel的处理
     *
     * @param key
     * @throws IOException
     */

    void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
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

        // 得到并清空缓冲区
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        MyByteBuffer myByteBuffer = new MyByteBuffer(buffer);
        // buffer.clear();

        // 读取信息获得读取的字节数
        long bytesRead = clientChannel.read(buffer);


        if (bytesRead == -1) {
            // 没有读取到内容的情况
            clientChannel.close();
        } else if (bytesRead < Pack.HEADER_LEN) {
            // myByteBuffer.forWrite();
        } else {
            // 将缓冲区准备为数据传出状态
            myByteBuffer.forRead();

            byte[] rawHeaderData = new byte[Pack.HEADER_LEN];
            buffer.get(rawHeaderData); // 读出包头

            Pack pack = new Pack(rawHeaderData);

            if (buffer.remaining() < pack.getPackContentLen()) {
                myByteBuffer.forWrite();
                return;
            }

            if (pack.getPackContentLen() > 0) {
                byte[] packData = new byte[pack.getPackContentLen()];
                buffer.get(packData);
                pack.setPackData(packData);
            }


            packetHandler.handlePack(pack, clientChannel);
            myByteBuffer.rewind();

            // 设置为下一次读取或是写入做准备
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    /**
     * 向一个SocketChannel写入信息的处理
     *
     * @param key
     * @throws IOException
     */
    void handleWrite(SelectionKey key) throws IOException {
    }
}
