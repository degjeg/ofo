package o.f.o.com.shareofo.net;

import android.support.annotation.IntDef;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.sql.Date;

import o.f.o.com.shareofo.db.Db;
import o.f.o.com.shareofo.db.dao.BicycleDao;
import o.f.o.com.shareofo.db.model.BicycleData;
import o.f.o.com.shareofo.net.bean.Cmds;
import o.f.o.com.shareofo.net.bean.DataPack;
import o.f.o.com.shareofo.net.bean.ErrorCodes;
import o.f.o.com.shareofo.net.bean.OfoDate;
import o.f.o.com.shareofo.net.common.ConnectionListener;
import o.f.o.com.shareofo.net.common.Packet;
import o.f.o.com.shareofo.net.common.PacketHandler;
import o.f.o.com.shareofo.net.common.RetPacketHandler;
import o.f.o.com.shareofo.net.common.TcpConnection;
import o.f.o.com.shareofo.net.common.TcpServer;
import o.f.o.com.shareofo.net.handlers.ShareDataRequestHandler;
import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/5.
 */

public class ShareOfoServer implements PacketHandler, ConnectionListener {
    public static final int PORT = 9999;


    private static final ShareOfoServer shareOfoServer = new ShareOfoServer();

    final TcpServer tcpServer;


    public static final int STEP_IDLE = 0,
            STEP_EXPORTING_DATA = 2,
            STEP_SENDING_DATA = 4,
            STEP_RECVING_DATA = 5;

    @IntDef({STEP_IDLE, STEP_EXPORTING_DATA, STEP_SENDING_DATA, STEP_RECVING_DATA})
    public @interface Step {
    }

    @Step
    int currentStep = STEP_IDLE;

    private ShareServerListener shareServerListener;
    private ShareDataRequestHandler shareDataRequestHandler;

    DataExportor exportor;

    private ShareOfoServer() {
        tcpServer = new TcpServer();
        tcpServer.setPacketHandler(this);
        tcpServer.setPacketParser(new PacketParser());
        tcpServer.setConnectionListener(this);
    }

    public static ShareOfoServer get() {
        return shareOfoServer;
    }

    public void startServer() {
        tcpServer.startServer(PORT);
    }

    @Override
    public void handlePack(Packet pack, TcpConnection connection) {
        switch (pack.getCmd()) {
            case Cmds.SHARE_DATA_REQ:
                handleShareDataRequest(pack, connection);
                break;
            case Cmds.PUSH_DATA:
                handlePushData(pack, connection);
                break;
            case Cmds.QUERY_EXPORT_FINISH:
                try {
                    exportor = new DataExportor();
                    connection.sendPack((Packet)pack.clone() , null);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                break;

            case Cmds.PULL_DATA:
                handlePullData(pack, connection);
                break;
        }
    }

    private void handlePullData(Packet pack, TcpConnection connection) {
        Packet packet = exportor.sendOne();
        packet.setCmd(pack.getCmd());
        packet.setReqCode(pack.getReqCode());
        shareServerListener.onSendingData(exportor.getCurrent(), exportor.getTotal());

        RetPacketHandler retPacketHandler = new RetPacketHandler() {
            @Override
            public void onGetReturnPacket(TcpConnection connection, Packet pack) {

            }

            @Override
            public void onPackSent(Packet pack, TcpConnection connection) {

            }

            @Override
            public void onError(int code, Exception e) {

            }
        };

        connection.sendPack(packet, retPacketHandler);
        //  new DataExportor().start();
        if (exportor.isFinished()) {
            shareServerListener.onTaskFinish(0);
        } else {
            // Packet packet = exportor.sendOne();
            // connection.sendPack(packet, this);
        }

    }

    private void handlePushData(Packet pack, TcpConnection connection) {
        try {
            DataPack dataPack = DataPack.ADAPTER.decode(pack.getPackData());
            BicycleDao dao = Db.get().getBicycleDao();

            for (OfoDate date : dataPack.data) {
                BicycleData ofoData = new BicycleData(date.code, date.password);
                // BicycleDao dao = Db.get().getBicycleDao();
                ofoData.setTime(new Date(date.time));
                ofoData.setDeleted(date.deleted != null && date.deleted != 0);
                ofoData.setDesc(date.desc);

                if (dao.queryBuilder()
                        .where(BicycleDao.Properties.Code.eq(date.code))
                        .where(BicycleDao.Properties.Password.eq(date.password))
                        .build().list().size() == 0) {
                    dao.insert(ofoData);
                }
            }

            shareServerListener.onReceivingData(dataPack.current, dataPack.total);
            Packet packet1 = new Packet();
            packet1.setRetCode(ErrorCodes.SUCCESS);
            packet1.setReqCode(pack.getReqCode());

            connection.sendPack(packet1, new RetPacketHandler() {
                @Override
                public void onGetReturnPacket(TcpConnection connection, Packet pack) {

                }

                @Override
                public void onPackSent(Packet pack, TcpConnection connection) {

                }

                @Override
                public void onError(int code, Exception e) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleShareDataRequest(Packet pack, TcpConnection connection) {
        // L.get().e(connection.TAG, "handlePack:" + pack.toString1());

        if (currentStep != STEP_IDLE) {
            Packet retPack = new Packet();
            retPack.setCmd(pack.getCmd());
            retPack.setReqCode(pack.getReqCode());
            retPack.setRetCode(ErrorCodes.REJECTED_BUSY);

            connection.sendPack(retPack, null);
        }
        shareDataRequestHandler.onShardDataRequest(null, pack, connection);

        // try {
        //     ShareRequestRequest request = ShareRequestRequest.ADAPTER.decode(pack.getPackData());
        //     shareDataRequestHandler.onShardDataRequest(request, connection);
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    public void reject(Packet pack, TcpConnection connection) {
        Packet retPack = new Packet();
        retPack.setCmd(pack.getCmd());
        retPack.setReqCode(pack.getReqCode());
        retPack.setRetCode(ErrorCodes.REJECTED);

        connection.sendPack(retPack, null);
    }

    public void agree(Packet pack, TcpConnection connection) {
        Packet retPack = new Packet();
        retPack.setCmd(pack.getCmd());
        retPack.setReqCode(pack.getReqCode());
        retPack.setRetCode(ErrorCodes.SUCCESS);

        connection.sendPack(retPack, null);

        shareServerListener.onExportingData(0, 0);
    }

    public void setShareServerListener(ShareServerListener shareServerListener) {
        this.shareServerListener = shareServerListener;
    }

    public void setShareDataRequestHandler(ShareDataRequestHandler shareDataRequestHandler) {
        this.shareDataRequestHandler = shareDataRequestHandler;
    }

    @Override
    public void onConnected(TcpConnection connection) {
        L.get().e(connection.TAG, "connected on server");
    }

    @Override
    public void onClosed(TcpConnection connection) {
        L.get().e(connection.TAG, "closed  on server");
    }
}
