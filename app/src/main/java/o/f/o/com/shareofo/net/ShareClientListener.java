package o.f.o.com.shareofo.net;

/**
 * Created by Administrator on 2017/5/18.
 */

public interface ShareClientListener {
    void onWaitingForAnswer();

    void onRejected();

    void onAccepted();

    void onTaskFinish(int code);

    void onExportingData(int prog, int total);

    void onWaitingServerExportingData(int prog, int total);

    void onSendingData(int prog, int total);

    void onReceivingData(int prog, int total);
}
