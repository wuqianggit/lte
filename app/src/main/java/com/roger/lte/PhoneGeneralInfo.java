package com.roger.lte;

import android.telephony.TelephonyManager;

/**
 * Created by 47641 on 2017/8/3.
 */

public class PhoneGeneralInfo {
    public String serialNumber;
    public String operaterName;
    public String operaterId;
    public String deviceId;
    public String deviceSoftwareVersion;
    public String Imsi;
    public String Imei;
    public int mnc;
    public int mcc;
    public int ratType= TelephonyManager.NETWORK_TYPE_UNKNOWN;
    public int phoneDatastate;
    public String phoneModel;
    public int sdk;

    @Override
    public String toString() {
        String s  = "this is phone info:"+this.deviceId;
        return s;
    }
}
