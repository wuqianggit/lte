package com.roger.lte;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

/**
 * Created by 47641 on 2017/8/24.
 */

public class SendTask extends TimerTask {
    private static Socket socket = null;
    private String ipAddress; //ip地址
    public static final int PORT = 10086;//服务器端口号
    private String data; //数据

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public void run() {
        DataOutputStream out = null;
        try {
            socket = new Socket(getIpAddress(), PORT);
            out = new DataOutputStream(socket.getOutputStream());
            //out.writeUTF(str.getBytes());
            System.out.println("客户端的数据：" + getData());
            out.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
