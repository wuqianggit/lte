package com.roger.lte;

import android.telephony.TelephonyManager;

import java.util.Date;

/**
 * Created by 47641 on 2017/8/3.
 */

public class CellGeneralInfo implements Cloneable {
    public int type;
    public int CId;
    public int lac;
    public int tac;
    public int psc;
    public int pci;
    public int RatType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    public int rsrp;
    public int rsrq;
    public int sinr;
    public int rssi;
    public int cqi;
    public int asulevel;
    public int getInfoType;
    public String time;
    private Date dateTime;

    /**
     * 向服务器需要发送的信息
     * rsrp  Reference Signal Receiving Power，参考信号接收功率
     * sinr  Signal to Interference plus Noise Ratio  信号与干扰加噪声比（信噪比）
     * @return 向服务器返回信息的字符串
     */
    public String getSockInfo(){
        StringBuilder sb=new StringBuilder();
        //数据格式封装
        sb.append(this.rsrp+";"+this.sinr);
        return sb.toString();
    }

    /**
     * 获取向文件中写入的格式
     * 每一行的格式：    年 月 日 时 分 秒 rsrp sinr ci
     * 每一行的格式例子：2017 11 11 18 47 36 -110 100 98765432
     * @return
     */
    public String getCellInfoData(){
        String str= this.rsrp+" "+this.sinr+" "+this.CId+"\n";
        return str;
    }

    public Date getDateTime() {
        dateTime=new Date();/*默认获取当前日期*/
        return dateTime;
    }

    @Override
    public Object clone() {
        CellGeneralInfo cellinfo = null;
        try {
            cellinfo = (CellGeneralInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return cellinfo;
    }
}
