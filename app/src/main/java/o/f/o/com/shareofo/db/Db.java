package o.f.o.com.shareofo.db;

import android.content.Context;

import o.f.o.com.shareofo.db.dao.BicycleDao;
import o.f.o.com.shareofo.db.dao.DaoMaster;
import o.f.o.com.shareofo.db.dao.DaoSession;

/**
 * Created by Administrator on 2017/5/5.
 */
public class Db {

    public static final String DB_NAME = "ofo";

    private DaoMaster master;
    private DaoSession session;
    private static final Db db = new Db();

    private Db() {

    }

    public static Db get() {
        return db;
    }

    public void init(Context context) {
        OpenHelper helper = new OpenHelper(context, DB_NAME, null);
        master = new DaoMaster(helper.getWritableDatabase());
        session = master.newSession();
    }

    public DaoSession getSession() {
        return session;
    }

    public BicycleDao getBicycleDao() {
        return session.getBicycleDao();
    }
}
