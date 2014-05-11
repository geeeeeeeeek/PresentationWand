package igeak.presentationwand.udp_connection;

/**
 * Created by Tong on 2/11/14.
 */

import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class NetTool {


    private String locAddress;//存储本机ip，例：本地ip ：192.168.1.
    private Runtime run = Runtime.getRuntime();//获取当前运行环境，来执行ping，相当于windows的cmd
    private Process proc = null;
    private String ping = "ping -c 1 -w 0.5 ";//其中 -c 1为发送的次数，-w 表示发送后等待响应的时间
    private int lastCode;//存放ip最后一位地址 0-255
    private String result;
    private int currentThreadIndex;

    //获取本机设备名称
    public static String getLocDeviceName() {

        return android.os.Build.MODEL;

    }

    /**
     * 扫描局域网内ip，找到对应服务器
     */
    public String getSeverIP() throws InterruptedException {

        locAddress = getLocAddrIndex();//获取本地ip前缀

        if (locAddress == null || locAddress.equals("")) {
            return "ERROR:WIFI_NOT_CONNECTED";
        }

        for (lastCode = 0; lastCode < 256; lastCode++) {//创建256个线程分别去ping
            new Thread(new Runnable() {
                public void run() {
                    String currentIP = locAddress + NetTool.this.lastCode;
                    try {
                        Socket socketForTest = new Socket(currentIP, 1997);
                        result = (socketForTest == null) ? null : currentIP;
                        socketForTest.close();
                    } catch (IOException e) {
                    }
                    NetTool.this.currentThreadIndex++;
                }
            }).start();
        }
        int time = 0;
        while (currentThreadIndex < 255 && result == null && time < 10) {
            Thread.sleep(100);
            time += 0.1;
        }

        return result;
    }

    //获取本地ip地址
    public String getLocAddress() {

        String ipaddress = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                // 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> address = networks.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
                        ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("", "获取本地ip地址失败");
            e.printStackTrace();
        }

        return ipaddress;

    }

    //获取IP前缀
    private String getLocAddrIndex() {

        String str = getLocAddress();

        if (!str.equals("")) {
            return str.substring(0, str.lastIndexOf(".") + 1);
        }

        return null;
    }


}