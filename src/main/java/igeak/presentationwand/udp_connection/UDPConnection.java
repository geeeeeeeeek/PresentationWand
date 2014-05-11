package igeak.presentationwand.udp_connection;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Tong on 3/19/14.
 */
public class UDPConnection {
    private final int PORT = 1997;
    private InetAddress serverAddress;
    private DatagramSocket socket;
    private long lastConfirmedTimeStamp;
    public static boolean isConnected = true;
    private boolean isConfirmed = false;

    public String connect() {
        try {
            if (socket != null) socket.close();
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
            return "连接中断，请重新连接";
        }
        try {
            serverAddress = InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            e.printStackTrace();
            return "没有找到设备";
        }
        confirmConnection();
        lastConfirmedTimeStamp = System.currentTimeMillis();
        return "已连接演示设备";
    }

    private void confirmConnection() {
        new Thread() {
            public void run() {

                while (true) {
                    try {
                        send("r");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void watchConnection() {

        new Thread() {
            public void run() {

                while (true) {
                    try {
                        Log.d("connection", "begin");
                        DatagramPacket datagramPacket = new DatagramPacket("r".getBytes(), "r".getBytes().length);
                        socket.receive(datagramPacket);
                        Log.d("connection", "recv " + new String(datagramPacket.getData()));
                        if (new String(datagramPacket.getData()) == "r") {
                            Log.d("connection", "cuyagcuiavdguahskoygvadguigvgh");
                            isConnected = true;
                            lastConfirmedTimeStamp = System.currentTimeMillis();
                            isConfirmed = true;
                        }
                    } catch (IOException e) {
                        Log.d("connection", "error1");
                    }

                    Log.d("connection", lastConfirmedTimeStamp + "");
                }
            }
        }.start();

        new Thread() {
            public void run() {
                Log.d("connection", "detect1");
                long currentTimeStamp;
                while (true) {
                    if (isConfirmed) continue;
                    currentTimeStamp = System.currentTimeMillis();
                    if (currentTimeStamp % 1000 != 0) continue;
                    //Log.d("connection", "detect2");
                    if (currentTimeStamp - lastConfirmedTimeStamp > 3000) {
                        isConnected = false;
                        Log.d("connection", lastConfirmedTimeStamp + " , " + currentTimeStamp);
                    }
                }
            }
        }.start();
    }

    public boolean send(String str) throws Exception {
        byte data[] = str.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, serverAddress, PORT);
//        try {
        socket.send(datagramPacket);
        if (isConfirmed) lastConfirmedTimeStamp = System.currentTimeMillis();
        isConfirmed = false;

        Log.d("connection", "sent " + str);
//        } catch (Exception e) {
//            return false;
//        }
        return true;
    }

//    private void checkIsConnected() throws Exception {
//        DatagramPacket datagramPacket = new DatagramPacket("r".getBytes(), "r".getBytes().length);
//        socket.receive(datagramPacket);
//        if (datagramPacket.getData().toString() != "r") throw new Exception();
//    }
//
//    public String getHostIP() {
//        new Thread() {
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                int servPort = 1997;
//                try {
//                    DatagramSocket socket = new DatagramSocket(servPort);
//                    DatagramPacket packet = new DatagramPacket(
//                            new byte[1024], 1024);
//
//                    while (true) {
//                        try {
//                            socket.receive(packet);//阻塞
//                            System.out.println("Handling client at "
//                                    + packet.getAddress().getHostAddress()
//                                    + " on port " + packet.getPort());
//                            socket.send(packet);
//                            packet.setLength(1024);
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }.start();
//        return null;
//    }
}