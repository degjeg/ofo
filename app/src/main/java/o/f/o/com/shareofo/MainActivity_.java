package o.f.o.com.shareofo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity_ extends AppCompatActivity {

    private static final String TAG = "MainActivity-";
    // wifi direct
    IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    final List<WifiP2pDevice> wifiP2pDeviceList = new ArrayList<>();

    // view
    DeviceListAdapter deviceListAdapter;
    RecyclerView recyclerView;
    View emptyView;
    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity_.this, DeviceDiscoveryService.class);
        intent.setAction(DeviceDiscoveryService.ACTION_START_DISCOVERY);
        startService(intent);

        initView();
        // initWifiDirect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.rv);

        deviceListAdapter = new DeviceListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(deviceListAdapter);
    }

    private void initWifiDirect() {


        //表示Wi-Fi对等网络状态发生了改变
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        //表示可用的对等点的列表发生了改变
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        //表示Wi-Fi对等网络的连接状态发生了改变
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        //设备配置信息发生了改变
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // registerReceiver(receiver, intentFilter);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);


        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //查找初始化成功时的处理写在这里。

                //实际上并没有发现任何服务，所以该方法可以置空。
                //对等点搜索的代码在onReceive方法中，详见下文。
                Log.e(TAG, "discoverPeers success");
            }

            @Override
            public void onFailure(int reasonCode) {
                //查找初始化失败时的处理写在这里。
                //警告用户出错了。
                Log.e(TAG, "discoverPeers failure " + reasonCode);
            }
        });
    }




    private WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            // InetAddress在WifiP2pInfo结构体中。
            String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
            Log.e(TAG, "onConnectionInfoAvailable" + info);
            Log.e(TAG, "onConnectionInfoAvailable" + groupOwnerAddress);

            //组群协商后，就可以确定群主。
            if (info.groupFormed && info.isGroupOwner) {
                //针对群主做某些任务。
                //一种常用的做法是，创建一个服务器线程并接收连接请求。
            } else if (info.groupFormed) {
                //其他设备都作为客户端。在这种情况下，你会希望创建一个客户端线程来连接群主。
            }
        }
    };
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                Log.e(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");

                //确定Wi-Fi Direct模式是否已经启用，并提醒Activity。
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    setIsWifiP2pEnabled(true);
                } else {
                    setIsWifiP2pEnabled(false);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.e(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
                if (mManager != null) {
                    mManager.requestPeers(mChannel, peerListListener);
                }
                //对等点列表已经改变！我们可能需要对此做出处理。

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.e(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                //连接状态已经改变！我们可能需要对此做出处理。

                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {

                    //我们连上了其他的设备，请求连接信息，以找到群主的IP。
                    mManager.requestConnectionInfo(mChannel, connectionListener);
                }

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.e(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                // DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                //         .findFragmentById(R.id.frag_list);
                // fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                //         WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

            }
        }
    };

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Log.e(TAG, "onPeersAvailable");
            tvEmpty.clearAnimation();
            //旧的不去，新的不来
            wifiP2pDeviceList.clear();
            wifiP2pDeviceList.addAll(peerList.getDeviceList());
            notifyDeviceListChanged();
            //如果AdapterView可以处理该数据，则把变更通知它。比如，如果你有可用对等点的ListView，那就发起一次更新。
            // ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            // if (peers.size() == 0) {
            //     Log.d(WiFiDirectActivity.TAG, "No devices found");
            //     return;
            // }


        }
    };

    public void connect(WifiP2pDevice device) {

        // 使用在网络上找到的第一个设备。
        // WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver将会通知我们。现在可以先忽略。
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity_.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void notifyDeviceListChanged() {
        if (wifiP2pDeviceList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {

        if (!isWifiP2pEnabled) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            tvEmpty.setText("已被关闭");
        } else if (wifiP2pDeviceList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            tvEmpty.setText("已打开,点击搜索周围设备");
            tvEmpty.setOnClickListener(searchListener);
        }
    }

    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RotateAnimation rotateAnimation = new RotateAnimation(
                    0, 360,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(100000);

            tvEmpty.clearAnimation();
            tvEmpty.setAnimation(rotateAnimation);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mManager != null) {
                        mManager.requestPeers(mChannel, peerListListener);
                    }
                }
            }, 1000);

        }
    };

    class DeviceListAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(MainActivity_.this, R.layout.adapter_device_list, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(wifiP2pDeviceList.get(position));
        }

        @Override
        public int getItemCount() {
            return wifiP2pDeviceList.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDeviceName;
        final TextView tvDeviceIp;

        public ViewHolder(View v) {
            super(v);

            tvDeviceName = (TextView) v.findViewById(R.id.tv_device_name);
            tvDeviceIp = (TextView) v.findViewById(R.id.tv_device_ip);
        }

        public void bindData(WifiP2pDevice device) {
            tvDeviceName.setText(device.deviceName);
            tvDeviceIp.setText(device.deviceAddress);
        }
    }


}
