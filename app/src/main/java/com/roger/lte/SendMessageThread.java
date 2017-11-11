package com.roger.lte;

import com.roger.lte.utils.TimeUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Administrator on 2017/8/21.
 */

public class SendMessageThread extends Thread {

    public static final StringBuilder CELL_INFO_DATA=new StringBuilder();/*写入文件的方法*/

    public Socket socket = null;
    DataOutputStream out=null;
    //IP 地址
    private String ipAddress;
    //端口号
    private int portNum;

    private String msg="";
    private Boolean isOn=false;

    public SendMessageThread(String ipAddress, int portNum){
        this.ipAddress=ipAddress;
        this.portNum=portNum;
    }

    @Override
    public void run() {
        try{
            while(true){
                if(Boolean.TRUE.equals(getOn())){
                    String nowMsg= TimeUtil.parser2DateTime(new Date())+" "+msg;//时间+内容
                    CELL_INFO_DATA.append(nowMsg);
                    //没隔一秒钟发一次
                    Thread.currentThread().sleep(1000);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //不能在UI线程调用这个方法
    private void sendMessage(String message) throws IOException {
        try{
            socket = new Socket(ipAddress, portNum);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("客户端异常:" + e.getMessage());
        }finally{
            if(socket != null){
                socket.close();
            }
            if(out!=null){
                out.close();
            }
        }
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getOn() {
        return isOn;
    }

    public void setOn(Boolean on) {
        isOn = on;
    }
}
