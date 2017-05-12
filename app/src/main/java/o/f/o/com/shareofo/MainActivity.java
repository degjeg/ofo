package o.f.o.com.shareofo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import o.f.o.com.shareofo.bean.Device;
import o.f.o.com.shareofo.db.Db;
import o.f.o.com.shareofo.db.dao.BicycleDao;
import o.f.o.com.shareofo.db.model.BicycleData;
import o.f.o.com.shareofo.net.ShareOfoClient;
import o.f.o.com.shareofo.net.ShareOfoServer;
import o.f.o.com.shareofo.net.bean.ShareRequestRequest;
import o.f.o.com.shareofo.net.common.TcpConnection;
import o.f.o.com.shareofo.net.common.TcpServer;
import o.f.o.com.shareofo.net.handlers.ShareDataRequestHandler;
import o.f.o.com.shareofo.utils.T;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        View.OnLongClickListener,
        ShareDataRequestHandler {

    private static final String TAG = "MainActivity-";

    // view
    MainAdapter mainAdapter;
    RecyclerView recyclerView;

    EditText etId;

    String currentKey;
    final List<Device> deviceList = new ArrayList<>();
    final List<MainData> dataList = new ArrayList<>();
    final List<BicycleData> searchResultList = new ArrayList<>();

    HandlerThread searchThread = new HandlerThread("");
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        fakeData();
        setContentView(R.layout.activity_main);

        initDeviceFounding();

        initView();
        searchThread.start();
        handler = new Handler(searchThread.getLooper());
        // initWifiDirect();

        ShareOfoServer.get().setShareDataRequestHandler(this);
        ShareOfoServer.get().startServer();
    }

    private void fakeData() {

        dataList.add(new MainData(new Device("dsfasdfasd", "193.3.3.3")));
        dataList.add(new MainData(new Device("sfasf", "193.3.31.3")));
        dataList.add(new MainData(new Device("asdf", "193.31.3.31")));
        dataList.add(new MainData(new Device("dsfasdfasdf", "193.3.31.3")));

        dataList.add(new MainData(new BicycleData("331333", "444444")));
        dataList.add(new MainData(new BicycleData("3313313", "4441444")));
        dataList.add(new MainData(new BicycleData("3133313", "444444")));
        dataList.add(new MainData(new BicycleData("4345235424", "444444")));


    }

    private void initDeviceFounding() {
        Intent intent = new Intent(MainActivity.this, DeviceDiscoveryService.class);
        intent.setAction(DeviceDiscoveryService.ACTION_START_DISCOVERY);
        startService(intent);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // String s = intent.getStringExtra("log");
                // log(s);
                deviceList.clear();
                deviceList.addAll(DeviceDiscoveryService.getDevices());
                reInitDataList();

            }
        };

        IntentFilter filter = new IntentFilter(DeviceDiscoveryService.ACTION_DEVICE_FOUND);
        registerReceiver(receiver, filter);
    }

    private void reInitDataList() {
        dataList.clear();

        dataList.add(new MainData(MainData.TYPE_DEVICE_LIST_EMPTY));

        if (!deviceList.isEmpty()) {
            for (Device device : deviceList) {
                dataList.add(new MainData(device));
            }
        } else {

        }

        for (BicycleData ofoData : searchResultList) {
            dataList.add(new MainData(ofoData));
        }
        mainAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.rv);

        etId = (EditText) findViewById(R.id.et_id);
        mainAdapter = new MainAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mainAdapter);

        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnLongClickListener(this);
        etId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentKey = s.toString();
                //  mainAdapter.notifyDataSetChanged();
                handler.removeCallbacksAndMessages(null);
                handler.post(new SearchTask(currentKey));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                onclickAdd();
                break;
            case R.id.btn_clear:
                onCLickClear();
                break;
        }
    }

    private void onclickAdd() {
        String code = etId.getText().toString().trim();
        String password = code + "pw";
        if (code.isEmpty()) return;

        BicycleData ofoData = new BicycleData(code, password);
        BicycleDao dao = Db.get().getBicycleDao();

        if (dao.queryBuilder()
                .where(BicycleDao.Properties.Code.eq(code))
                .where(BicycleDao.Properties.Password.eq(password))
                .build().list().size() == 0) {
            dao.insert(ofoData);
        }
    }

    private void onCLickClear() {
        etId.setText("");
    }

    @Override
    public boolean onLongClick(View v) {
        Db.get().getBicycleDao().deleteAll();
        return true;
    }

    @Override
    public void onShardDataRequest(ShareRequestRequest request, TcpConnection connection) {

    }


    class MainAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case MainData.TYPE_DEVICE:
                    return new DeviceHolder(View.inflate(MainActivity.this, R.layout.adapter_device_list, null));
                case MainData.TYPE_DEVICE_LIST_EMPTY:
                    return new DeviceListEmptyHolder(View.inflate(MainActivity.this, R.layout.adapter_device_empty, null));
                default:
                    return new SearchResultHolder(View.inflate(MainActivity.this, R.layout.adapter_search_result, null));
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(getItem(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size(); // wifiP2pDeviceList.size();
        }

        MainData getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {


        public ViewHolder(View v) {
            super(v);

        }

        abstract void bindData(MainData device);
    }

    class DeviceHolder extends ViewHolder implements View.OnClickListener {

        TextView tvIp, tvName;
        Device device;

        public DeviceHolder(View v) {
            super(v);
            tvIp = (TextView) v.findViewById(R.id.tv_device_ip);
            tvName = (TextView) v.findViewById(R.id.tv_device_name);
            v.setOnClickListener(this);
        }

        @Override
        void bindData(MainData device) {
            this.device = device.device;

            tvIp.setText(device.device.getIp());
            tvName.setText(device.device.getDisplayName());
        }

        @Override
        public void onClick(View v) {
            ShareOfoClient.get().requestShareData(device.getIp(), ShareOfoServer.PORT);
           // lertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
           // uilder.setTitle("提示")
           //        .setMessage("和" + device.toString() + " 共享数据?")
           //        .setPositiveButton("共享", new DialogInterface.OnClickListener() {
           //            @Override
           //            public void onClick(DialogInterface dialog, int which) {
           //                // T.show("分享数据");
           //
           //            }
           //        })
           //        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
           //            @Override
           //            public void onClick(DialogInterface dialog, int which) {
           //                dialog.dismiss();
           //            }
           //        })
           //        .create()
           //        .show();
        }
    }

    class DeviceListEmptyHolder extends ViewHolder {
        public DeviceListEmptyHolder(View v) {
            super(v);
        }

        @Override
        void bindData(MainData device) {
        }
    }


    class SearchResultHolder extends ViewHolder {
        TextView tvId, tvKey;

        public SearchResultHolder(View v) {
            super(v);
            tvId = (TextView) v.findViewById(R.id.tv_id);
            tvKey = (TextView) v.findViewById(R.id.tv_key);
        }

        @Override
        void bindData(MainData device) {
            SpannableStringBuilder b = new SpannableStringBuilder();

            b.append(device.ofoData.getCode());

            if (currentKey != null) {
                int pos = device.ofoData.getCode().indexOf(currentKey);
                int len = currentKey.length();
                b.setSpan(new ForegroundColorSpan(0xffff0000), pos, pos + len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            tvId.setText(b);
            tvKey.setText(device.ofoData.getPassword());
        }
    }

    class MainData {
        public static final int TYPE_DEVICE = 1;
        public static final int TYPE_DEVICE_LIST_EMPTY = 2;
        public static final int TYPE_SEARCH_RESULT = 3;

        int type;
        Device device;
        BicycleData ofoData;

        public MainData(int type) {
            this.type = type;
        }

        public MainData(Device device) {
            this.device = device;
            type = TYPE_DEVICE;
        }

        public MainData(BicycleData ofoData) {
            this.ofoData = ofoData;
            type = TYPE_SEARCH_RESULT;
        }
    }


    class SearchTask implements Runnable {
        String keyword;
        boolean isCanel = false;

        public SearchTask(String keyword) {
            this.keyword = keyword;
        }

        public void cancel() {
            isCanel = true;
        }

        @Override
        public void run() {
            BicycleDao dao = Db.get().getBicycleDao();
            QueryBuilder<BicycleData> qb;

            if (TextUtils.isEmpty(keyword.trim())) {
                qb = dao.queryBuilder()
                        .orderDesc(BicycleDao.Properties.Time)
                        .limit(100);
            } else {
                qb = dao.queryBuilder()
                        .orderDesc(BicycleDao.Properties.Time)
                        .where(BicycleDao.Properties.Code.like("%" + keyword + "%"));
            }


            final List<BicycleData> result = qb.build().list();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isCanel) return;
                    searchResultList.clear();
                    searchResultList.addAll(result);
                    reInitDataList();
                }
            });
        }
    }
}
