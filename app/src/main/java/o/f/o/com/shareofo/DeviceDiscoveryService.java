package o.f.o.com.shareofo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import o.f.o.com.shareofo.bean.BroadCastPack;
import o.f.o.com.shareofo.bean.Cmds;
import o.f.o.com.shareofo.bean.Device;
import o.f.o.com.shareofo.bean.MyDeviceFactory;

/**
 * Created by Administrator on 2017/4/20.
 */

public class DeviceDiscoveryService extends Service {
    public static final String ACTION_START_DISCOVERY = BuildConfig.APPLICATION_ID + ".start_discovery";
    public static final String ACTION_STOP_DISCOVERY = BuildConfig.APPLICATION_ID + ".stop_discovery";

    // 通知出去
    public static final String ACTION_DEVICE_FOUND = BuildConfig.APPLICATION_ID + ".device_found";
    public static final String ACTION_STATUS_CHAGE = BuildConfig.APPLICATION_ID + ".status";

    public static final String EXTRA_BROADCAST_ADDR = "broadcat_addr";
    public static final String EXTRA_BROADCAST_PORT = "broadcat_port";

    /**
     * A类 首位0
     * B类 首位10
     * C类 首位110
     * D类 首位1110  (广播地址,11100000=224)
     */
    private String multiCastHost = "224.0.0.1";
    private int multiCastPort = 8003;
    private MulticastSocket multiCastServerSocket;
    private InetAddress multiCastReceiveAddress;

    // udp
    private int udpPort = 8004;
    private MulticastSocket udpServerSocket;


    private static final Vector<Device> devices = new Vector<>();

    public static final int CLOSED = 0;
    public static final int RUNNING = 1;
    public static final int CLOSING = 2002;

    private AtomicInteger status = new AtomicInteger(CLOSED);

    private HandlerThread multiCastServerThread, multiCastClientThread;
    private Handler multiCastServerHandler, clientHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        MyDeviceFactory.create(this);

        devices.clear();
        multiCastServerThread = new HandlerThread("server");
        multiCastClientThread = new HandlerThread("client");

        multiCastServerThread.start();
        multiCastClientThread.start();

        multiCastServerHandler = new Handler(multiCastServerThread.getLooper());
        clientHandler = new Handler(multiCastClientThread.getLooper());
    }

    private void log(CharSequence s) {
        // test
        // String time = new SimpleDateFormat("mm:ss S").format(new Date());
        // Intent inten = new Intent("l.o.g");
        // inten.putExtra("log", time + " " + s.toString());
        // sendBroadcast(inten);

        Log.d("xxxxxxxxxxxxx", s.toString());
        // SpannableStringBuilder b = new SpannableStringBuilder();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return super.onStartCommand(intent, flags, startId);
        String action = intent.getAction();
        switch (action) {
            case ACTION_START_DISCOVERY:
                if (intent.hasExtra(EXTRA_BROADCAST_ADDR)) {
                    String multiCastHost = intent.getStringExtra(EXTRA_BROADCAST_ADDR);
                    int multiCastPort = intent.getIntExtra(EXTRA_BROADCAST_PORT, 0);
                    if (!TextUtils.equals(this.multiCastHost, multiCastHost)
                            || this.multiCastPort != multiCastPort) {
                        stopDiscovery();
                        this.multiCastHost = multiCastHost;
                        this.multiCastPort = multiCastPort;
                    }
                }
                startDiscovery();
                break;

            case ACTION_STOP_DISCOVERY:
                stopDiscovery();
                break;
            // case ACTION_DEVICE_FOUND:
            //
            //     break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopDiscovery() {
        if (status.get() == CLOSED) {
            return;
        }
        setStatus(CLOSING);
        new Thread(stopServerRunnable).start();
        clientHandler.removeCallbacksAndMessages(null);
    }

    private void startDiscovery() {
        // status.set(true);
        multiCastServerHandler.post(serverRunnable);
        clientHandler.postDelayed(sendBroadcastRunnable, 300);
    }

    private void setStatus(int status) {
        this.status.set(status);
        log("status:" + status);
    }


    public static Vector<Device> getDevices() {
        return devices;
    }

    Runnable sendBroadcastRunnable = new Runnable() {
        @Override
        public void run() {

            clientHandler.removeCallbacks(this);

            // 发送的数据包，局网内的所有地址都可以收到该数据包
            try {
                // log("广播开始");
                sendBroadCastPack(multiCastHost, multiCastPort, Cmds.BROADCAST_STEP1);
                // log("广播已发出");
            } catch (Exception e) {
                e.printStackTrace();
                log(Log.getStackTraceString(e));
            } finally {
                if (status.get() == RUNNING)
                    clientHandler.postDelayed(this, 30000);

            }
        }
    };

    Runnable serverRunnable = new Runnable() {
        public void run() {
            try {
                if (status.get() == RUNNING) {
                    return;
                }
                while (status.get() == CLOSING) { // 等待
                    Thread.sleep(100);
                }

                setStatus(RUNNING);
                // if ((status.get() & 0x3000) == 0x3000) setStatus(WORKING);
                multiCastServerSocket = new MulticastSocket(multiCastPort);
                // multiCastServerSocket.setTimeToLive(1);
                // multiCastServerSocket.setSoTimeout(1000);
                multiCastReceiveAddress = InetAddress.getByName(multiCastHost);
                multiCastServerSocket.joinGroup(multiCastReceiveAddress);
                log("广播服务器开启成功");
                byte buf[] = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, 1024);

                while (status.get() == RUNNING) {
                    try {
                        multiCastServerSocket.receive(dp);
                        log("收到广播 " + dp.getAddress());

                        // byte[] tmp = new byte[dp.getLength()];
                        // System.arraycopy(buf, 0, tmp, 0, dp.getLength());
                        //
                        // if (!Arrays.equals(tmp, ID)) {
                        //     clientHandler.postDelayed(sendBroadcastRunnable, 1);
                        // }
                        // Toast.makeText(this, new String(buf, 0, dp.getLength()), Toast.LENGTH_LONG);
                        // log("InetAddress : " + dp.getAddress() + ":" + dp.getPort());
                        byte[] tmp = new byte[dp.getLength() - 1];
                        System.arraycopy(buf, 1, tmp, 0, tmp.length);
                        String jsonContent = new String(tmp, "utf-8");

                        byte cmd = buf[0];
                        if (cmd == Cmds.BROADCAST_STEP1) {
                            sendBroadCastPack(dp.getAddress().getHostAddress(), multiCastPort, Cmds.BROADCAST_STEP2);
                        }
                        if (cmd == Cmds.BROADCAST_STEP1
                                || cmd == Cmds.BROADCAST_STEP2) {
                            BroadCastPack pack = JSON.parseObject(jsonContent, BroadCastPack.class);
                            Device device = new Device(pack.getId(), dp.getAddress().getHostAddress());

                            if (!devices.contains(device)) {
                                device.setAlias(pack.getAlias());
                                device.setPhoneModel(pack.getPhoneModel());
                                devices.add(device);

                                // 通知列表变化
                                Intent intent = new Intent(ACTION_DEVICE_FOUND);
                                sendBroadcast(intent);

                                log("发现新设备 : " + device);
                            }
                        }
                    } catch (Exception e) {
                        if (status.get() == RUNNING) log(Log.getStackTraceString(e));
                    }
                }
            } catch (IOException e) {
                if (status.get() == RUNNING) {
                    log(Log.getStackTraceString(e));
                }
            } catch (InterruptedException e) {
                // log(Log.getStackTraceString(e));
            } finally {
                log("广播已退出");
                if (status.get() == RUNNING) stopServerRunnable.run();
            }
        }
    };

    private void sendBroadCastPack(String host, int port, byte cmd) {

        // 发送的数据包，局网内的所有地址都可以收到该数据包
        DatagramPacket dataPacket = null;
        MulticastSocket socket = null;
        try {
            // log("广播开始");
            socket = new MulticastSocket();
            socket.setTimeToLive(4);
            //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的

            Device device = MyDeviceFactory.myDevice();
            String json = JSON.toJSONString(device);
            byte[] jsonBytes = json.getBytes("utf-8");
            byte[] data = new byte[jsonBytes.length + 1];
            data[0] = cmd; // Cmds.BROADCAST_STEP2;
            System.arraycopy(jsonBytes, 0, data, 1, jsonBytes.length);
            // 224.0.0.1为广播地址
            InetAddress address = InetAddress.getByName(host);
            // 这个地方可以输出判断该地址是不是广播类型的地址
            // log("isMulticastAddress:" + address.isMulticastAddress());
            dataPacket = new DatagramPacket(data, data.length, address, port);
            socket.send(dataPacket);

            // socket.close();
            // log("广播已发出");
        } catch (Exception e) {
            e.printStackTrace();
            log(Log.getStackTraceString(e));
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

//    Runnable udpServerRunnable = new Runnable() {
//        public void run() {
//            try {
//                if (isUdpServerRunning()) {
//                    return;
//                }
//                while (status.get() == CLOSING) { // 等待
//                    Thread.sleep(100);
//                }
//
//                setUdpServerRunning();
//
//                udpServerSocket = new MulticastSocket(udpPort);
//                byte buf[] = new byte[1024];
//                DatagramPacket dp = new DatagramPacket(buf, 1024);
//                log("udp服务器开启成功");
//                while (isUdpServerRunning()) {
//                    try {
//                        udpServerSocket.receive(dp);
//                        // Toast.makeText(this, new String(buf, 0, dp.getLength()), Toast.LENGTH_LONG);
//                        // log("InetAddress : " + dp.getAddress() + ":" + dp.getPort());
//
//                    } catch (Exception e) {
//                        if (isUdpServerRunning()) log(Log.getStackTraceString(e));
//                    }
//                }
//            } catch (IOException e) {
//                if (isUdpServerRunning()) {
//                    log(Log.getStackTraceString(e));
//                }
//            } catch (InterruptedException e) {
//                // log(Log.getStackTraceString(e));
//            } finally {
//                log("udp已退出");
//                if (isUdpServerRunning()) stopServerRunnable.run();
//            }
//        }
//    };


    Runnable stopServerRunnable = new Runnable() {
        public void run() {
            try {
                if (multiCastServerSocket != null) {
                    multiCastServerSocket.close();
                }
                if (udpServerSocket != null) {
                    udpServerSocket.close();
                }
                devices.clear();
            } catch (Exception e) {
                log(Log.getStackTraceString(e));
            } finally {
                multiCastServerSocket = null;
                udpServerSocket = null;
                setStatus(CLOSED);
            }
        }
    };

}
