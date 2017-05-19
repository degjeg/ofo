package o.f.o.com.shareofo.net;

import java.util.ArrayList;
import java.util.List;

import o.f.o.com.shareofo.db.Db;
import o.f.o.com.shareofo.db.dao.BicycleDao;
import o.f.o.com.shareofo.db.model.BicycleData;
import o.f.o.com.shareofo.net.bean.Cmds;
import o.f.o.com.shareofo.net.bean.DataPack;
import o.f.o.com.shareofo.net.bean.OfoDate;
import o.f.o.com.shareofo.net.common.Packet;
import o.f.o.com.shareofo.net.common.TcpConnection;

/**
 * Created by Administrator on 2017/5/18.
 */

public class DataExportor {
    public static final int PAGE_SIZE = 2;

    List<OfoDate> dataList;

    int current = 0;
    boolean finished = false;

    public DataExportor() {
        List<BicycleData> datas = Db.get().getBicycleDao().loadAll();
        this.dataList = new ArrayList<>();

        for (BicycleData bd : datas) {
            dataList.add(new OfoDate.Builder()
                    .code(bd.getCode())
                    .desc(bd.getDesc())
                    .time(bd.getTime().getTime())
                    .deleted((bd.getDeleted() != null
                            && bd.getDeleted()) ? 1 : 0)
                    .password(bd.getPassword())
                    .build());
        }
    }

    public Packet sendOne() {
        Packet p = new Packet();
        DataPack.Builder dataPack = new DataPack.Builder();

        int end = Math.min(current + PAGE_SIZE, dataList.size());
        dataPack.data(dataList.subList(current, end));

        current = end;

        dataPack.current(current);
        dataPack.total(dataList.size());

        if (current >= dataList.size()) {
            finished = true;
        }

        p.setCmd(Cmds.PUSH_DATA);
        p.setPackData(dataPack.build().encode());
        return p;
    }

    boolean isFinished() {
        return finished;
    }

    public int getCurrent() {
        return current;
    }

    public int getTotal() {
        return dataList.size();
    }
}
