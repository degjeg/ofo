package o.f.o.com.shareofo.bean;

import android.text.TextUtils;

/**
 * Created by Administrator on 2017/4/20.
 */

public class Device {
    private String id;
    private String ip;
    private String phoneModel;
    private String alias;

    public Device(String id, String ip) {
        this.id = id;
        this.ip = ip;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object obj) {
        String id = ((Device) obj).id;

        return TextUtils.equals(this.id, id);
    }

    public String getDisplayName() {
        StringBuilder b = new StringBuilder();
        if (!TextUtils.isEmpty(alias)) b.append(alias);
        if (!TextUtils.isEmpty(phoneModel)) {
            if (b.length() == 0) {
                b.append(phoneModel);
            } else {
                b.append("(");
                b.append(phoneModel);
                b.append(")");
            }
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(100);
        b/*.append("\nid:").append(id)*/
                .append("\nip:").append(ip)
                .append("\nphone:").append(phoneModel);

        return b.toString();
    }
}
