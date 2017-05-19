package o.f.o.com.shareofo.net;

import android.support.annotation.IntDef;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Date;

import o.f.o.com.shareofo.db.Db;
import o.f.o.com.shareofo.db.dao.BicycleDao;
import o.f.o.com.shareofo.db.model.BicycleData;
import o.f.o.com.shareofo.net.bean.Cmds;
import o.f.o.com.shareofo.net.bean.DataPack;
import o.f.o.com.shareofo.net.bean.ErrorCodes;
import o.f.o.com.shareofo.net.bean.OfoDate;
import o.f.o.com.shareofo.net.bean.PullData;
import o.f.o.com.shareofo.net.common.ConnectionListener;
import o.f.o.com.shareofo.net.common.Packet;
import o.f.o.com.shareofo.net.common.PacketHandler;
import o.f.o.com.shareofo.net.common.RetPacketHandler;
import o.f.o.com.shareofo.net.common.TcpClient;
import o.f.o.com.shareofo.net.common.TcpConnection;
import o.f.o.com.shareofo.utils.L;

/**
 * Created by Administrator on 2017/5/5.
 */

public class ShareOfoClient implements PacketHandler, ConnectionListener {

    private static final ShareOfoClient shareOfoClient = new ShareOfoClient();

    TcpClient tcpClient;
    private final Object lock = new Object();

    ShareClientListener clientListener;

    public static final int STEP_IDLE = 0,
            STEP_WAINTING_ANSWER = 1,
            STEP_EXPORTING_DATA = 2,
            STEP_WAITING_SERVER_EXPORT_DATA = 3,
            STEP_SENDING_DATA = 4,
            STEP_RECVING_DATA = 5;

    @IntDef({STEP_IDLE,
            STEP_WAINTING_ANSWER,
            STEP_EXPORTING_DATA,
            STEP_WAITING_SERVER_EXPORT_DATA,
            STEP_SENDING_DATA,
            STEP_RECVING_DATA})
    public @interface Step {
    }

    @ShareOfoClient.Step
    int currentStep = STEP_IDLE;

    DataExportor exportor; // = new DataExportor();

    private ShareOfoClient() {

    }

    private void init() {
        synchronized (lock) {
            if (tcpClient == null) {
                tcpClient = new TcpClient();

                tcpClient.setPacketHandler(this);
                tcpClient.setPacketParser(new PacketParser());
                tcpClient.setConnectionListener(this);
            }
        }
    }

    public static ShareOfoClient get() {
        return shareOfoClient;
    }

    public void setClientListener(ShareClientListener clientListener) {
        this.clientListener = clientListener;
    }

    public void requestShareData(String host, int port) {
        init();

        if (currentStep != STEP_IDLE) { // 拒绝后,回到空闲状态)
            return;
        }
        currentStep = STEP_WAINTING_ANSWER;
        clientListener.onWaitingForAnswer();
        tcpClient.connectServer(host, port);

        // if (tcpClient.isConnected()) {
        //     sendTest();
        // } else {
        //
        // }
    }

    public void close() {
        synchronized (lock) {
            if (tcpClient != null) {
                tcpClient.close();
                tcpClient = null;
            }
            currentStep = STEP_IDLE;
        }
    }

    @Override
    public void handlePack(Packet pack, TcpConnection connection) {
        // L.get().e(connection.TAG, "handlePack:" + pack.toString1());

    }

    @Override
    public void onConnected(final TcpConnection socketChannel) {
        // sendTest();
        // 发送分享数据请求
        tcpClient.sendData(Cmds.SHARE_DATA_REQ, null, new RetPacketHandler() {
            @Override
            public void onGetReturnPacket(TcpConnection connection, Packet pack) {
                if (pack.getRetCode() == ErrorCodes.SUCCESS) {
                    currentStep = STEP_EXPORTING_DATA;
                    clientListener.onAccepted();
                    waitServerReady(connection);

                } else {
                    currentStep = STEP_IDLE; // 拒绝后,回到空闲状态
                    clientListener.onRejected();
                    clientListener.onTaskFinish(-2);
                }
            }

            @Override
            public void onPackSent(Packet pack, TcpConnection connection) {

            }

            @Override
            public void onError(int code, Exception e) {
                clientListener.onTaskFinish(-1);
            }
        });
    }

    private void waitServerReady(TcpConnection connection) {
        clientListener.onWaitingServerExportingData(0, 0);

        connection.sendPack(Cmds.QUERY_EXPORT_FINISH, (byte[]) null, new RetPacketHandler() {
            @Override
            public void onGetReturnPacket(TcpConnection connection, Packet pack) {
                sendDataToServer(connection);
            }

            @Override
            public void onPackSent(Packet pack, TcpConnection connection) {

            }

            @Override
            public void onError(int code, Exception e) {
                clientListener.onTaskFinish(code);
            }
        });
    }

    private void sendDataToServer(final TcpConnection connection) {
        exportor = new DataExportor();
        Packet packet = exportor.sendOne();

        clientListener.onSendingData(exportor.getCurrent(), exportor.getTotal());
        RetPacketHandler retPacketHandler = new RetPacketHandler() {
            @Override
            public void onGetReturnPacket(TcpConnection connection, Packet pack) {
                if (exportor.isFinished()) {
                    clientListener.onReceivingData(0, 0);
                    pullData(connection);
                } else {
                    Packet packet = exportor.sendOne();
                    clientListener.onSendingData(exportor.getCurrent(), exportor.getTotal());
                    connection.sendPack(packet, this);
                }
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
    }

    private void pullData(TcpConnection connection) {

        RetPacketHandler retPacketHandler = new RetPacketHandler() {
            @Override
            public void onGetReturnPacket(TcpConnection connection, Packet pack) {

                try {
                    DataPack dataPack = DataPack.ADAPTER.decode(pack.getPackData());
                    BicycleDao dao = Db.get().getBicycleDao();

                    clientListener.onReceivingData(dataPack.current, dataPack.total);

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

                    if (dataPack.current.equals(dataPack.total)) {
                        clientListener.onTaskFinish(0);
                    } else {
                        pullData(connection);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPackSent(Packet pack, TcpConnection connection) {

            }

            @Override
            public void onError(int code, Exception e) {
                clientListener.onTaskFinish(code);
            }
        };

        PullData pullData = new PullData.Builder()
                .build();
        connection.sendPack(Cmds.PULL_DATA, pullData.encode(), retPacketHandler);

    }

    @Override
    public void onClosed(TcpConnection connection) {
        L.get().e(connection.TAG, "closed  on client");
    }

    private void sendTest() {
        new Thread() {
            @Override
            public void run() {

                for (int i = 0; i < 20000; i++) {
                    tcpClient.sendData(1, ("HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello" + (i + 1)).getBytes(),
                            null
                            /*new RetPacketHandler() {
                        @Override
                        public void onGetReturnPacket(Packet pack) {
                            L.get().e(tcpClient.getConnection().TAG, "onGetReturnPacket:" + pack.toString1());
                        }

                        @Override
                        public void onPackSent(Packet pack, TcpConnection connection) {

                        }

                        @Override
                        public void onError(int code, Exception e) {
                            L.get().e(tcpClient.getConnection().TAG, "onGetReturnPacketError:" + code, e);
                        }
                    }*/);
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
