package o.f.o.com.shareofo;

import android.app.Application;

import o.f.o.com.shareofo.db.Db;
import o.f.o.com.shareofo.utils.T;

/**
 * Created by Administrator on 2017/5/5.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Db.get().init(this);
        Db.get().init(this);
        T.init(this);
    }
}
