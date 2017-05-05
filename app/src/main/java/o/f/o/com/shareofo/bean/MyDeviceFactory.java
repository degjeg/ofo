package o.f.o.com.shareofo.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;

import java.security.SecureRandom;

/**
 * Created by Administrator on 2017/4/21.
 */

public class MyDeviceFactory {
    static Device myDevice;

    public static Device create(Context context) {
        if (myDevice != null) {
            return myDevice;
        }
        myDevice = new Device(null, null);

        SharedPreferences preferences = context.getSharedPreferences("wifi_direct", Context.MODE_PRIVATE);

        String savedId = null;
        if (preferences.contains("ID")) {
            savedId = preferences.getString("ID", "");
        }

        if (savedId == null || savedId.length() != 32) {
            SecureRandom random = new SecureRandom();
            byte[] ID = new byte[24];
            random.nextBytes(ID);
            savedId = Base64.encodeToString(ID,
                    Base64.NO_CLOSE | Base64.NO_PADDING | Base64.NO_WRAP);

            preferences.edit().putString("ID", savedId).apply();

        }
        myDevice.setId(savedId);
        myDevice.setPhoneModel(Build.MODEL);

        return myDevice;
    }

    public static Device myDevice() {
        return myDevice;
    }

    public static boolean isSelef(Device device) {
        return myDevice.equals(device);
    }
}
