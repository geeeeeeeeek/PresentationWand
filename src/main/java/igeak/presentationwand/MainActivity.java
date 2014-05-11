package igeak.presentationwand;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import igeak.presentationwand.udp_connection.UDPConnection;

public class MainActivity extends Activity implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private int VIBRATE_INTENSITY = 10;
    private long UPDATE_INTERVAL = 600;
    private GestureDetector gestureDetector;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private Vibrator vibrator;
    private SensorManager mSensorManager;
    private long lastUpdatedTime;
    private UDPConnection udpConnection;

    private boolean isScreenAlwaysOn = true;
    private boolean isSensorOn = true;
    private int SENSOR_INTENSITY = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setVisible(false);
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.board));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.isScreenAlwaysOn = sharedPref.getBoolean("pref_key_screen_always_on", true);
        this.isSensorOn = sharedPref.getBoolean("pref_key_sensor_on", true);
        this.SENSOR_INTENSITY = Integer.parseInt(sharedPref.getString("pref_key_sensor_intensity", "14"));

        //start gesture detection
        startGestureService();

        this.powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        this.wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        setVisible(true);
    }

    private void startGestureService() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gestureDetector = new BuileGestureExt(this, new BuileGestureExt.OnGestureResult() {
            @Override
            public void onGestureResult(int direction) {

                if (udpConnection == null) {
                    Log.d("connection","connect");
                    udpConnection = new UDPConnection();
                    udpConnection.connect();
                }

                request(direction);
            }


        }
        ).Buile();
    }

    private void request(int direction) {
        //if (!udpConnection.isConnected) showToast("连接中断，请重新连接");
        new Thread("" + direction) {
            @Override
            public void run() {
                super.run();
                try {
//                    if (!udpConnection.isConnected) {
//                        Looper.prepare();
//                        showToast("连接中断，请重新连接");
//                        Looper.loop();
//                    }
                    udpConnection.send(Thread.currentThread().getName());
                    Log.d("connection", "sent");
                    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VIBRATE_INTENSITY);
                } catch (Exception ioe) {
                    Looper.prepare();
                    showToast("连接中断，请重新连接");
                    Looper.loop();
                }
            }
        }.start();
    }

    private void showToast(String toastContent) {
        Toast.makeText(MainActivity.this, toastContent, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //加速度传感器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                //还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，
                //根据不同应用，需要的反应速率不同，具体根据实际情况设定
                SensorManager.SENSOR_DELAY_FASTEST);
        this.wakeLock.acquire();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();

    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(new Date());
        if (!getTitle().equals("PPT魔杖  " + time)) setTitle("PPT魔杖  " + time);
        int sensorType = sensorEvent.sensor.getType();


        if (isScreenAlwaysOn && !this.wakeLock.isHeld()) {
            this.wakeLock.acquire();
        }
        if (!isScreenAlwaysOn && this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }

        if (!isSensorOn) {
            return;
        }


        //values[0]:X轴，values[1]：Y轴，values[2]：Z轴
        float[] values = sensorEvent.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if ((Math.abs(values[0]) > SENSOR_INTENSITY || Math.abs(values[1]) > SENSOR_INTENSITY || Math.abs(values[2]) > SENSOR_INTENSITY)) {
                long timeInterval = System.currentTimeMillis() - lastUpdatedTime;
                lastUpdatedTime = System.currentTimeMillis();
                if (timeInterval < UPDATE_INTERVAL) {
                    return;
                }
                Log.d("z", "" + values[0] + " " + values[1] + " " + values[2]);
                request(3);
//                new Thread("SendRequestOnSensor") {
//                    @Override
//                    public void run() {
//                        super.run();
//                        try {
//                            udpConnection.send("3");
//                            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(VIBRATE_INTENSITY);
//
//                        } catch (IOException ioe) {
//                            Looper.prepare();
//                            showToast("连接失败，请检查网络情况以及PC端是否开启");
//                            Looper.loop();
//                        } catch (Exception e) {
//                            //do nothing
//                        }
//                    }
//                }.start();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Toast.makeText(this, "asaa", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.isScreenAlwaysOn = sharedPref.getBoolean("pref_key_screen_always_on", true);
        this.isSensorOn = sharedPref.getBoolean("pref_key_sensor_on", true);
        this.SENSOR_INTENSITY = Integer.parseInt(sharedPref.getString("pref_key_sensor_intensity", "14"));
    }
}
