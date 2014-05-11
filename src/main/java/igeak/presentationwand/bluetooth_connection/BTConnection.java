package igeak.presentationwand.bluetooth_connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.UUID;

/**
 * Created by Tong on 3/15/14.
 */
public class BTConnection {
    private PrintStream mPrintStream = null;
    private BufferedReader mBufferedReader = null;

    private BluetoothAdapter myBluetoothAdapter = null;
    BluetoothServerSocket mBThServer = null;
    BluetoothSocket mBTHSocket = null;
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    boolean isConnected;

    BTConnection() {

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myBluetoothAdapter.enable();

        Intent discoverableIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);//使得蓝牙处于可发现模式，持续时间150s
        discoverableIntent.putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 150);
    }

    int accept() {
        if (isConnected) {
            return 0;
        }
        try {
            mBThServer = myBluetoothAdapter
                    .listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }

        try {
            mBTHSocket = mBThServer.accept();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        try {
            mBufferedReader = new BufferedReader(new InputStreamReader(
                    mBTHSocket.getInputStream()));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return -1;
        }// 取得输入、输出流
        try {
            mPrintStream = new PrintStream(
                    mBTHSocket.getOutputStream(), true);
            isConnected = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    int send(int content) {

        mPrintStream.write(content);
        mPrintStream.flush();
        return 1;
    }
}
