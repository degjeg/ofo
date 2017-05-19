package o.f.o.com.shareofo.net;

/**
 * Created by Administrator on 2017/5/18.
 */

public interface ShareServerListener {
    void onTaskFinish(int code);

    void onExportingData(int prog, int total);

    void onSendingData(int prog, int total);

    void onReceivingData(int prog, int total);
}
