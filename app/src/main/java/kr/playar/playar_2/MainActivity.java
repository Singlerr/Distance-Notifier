package kr.playar.playar_2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    static final int REQUEST_ENABLE_BT = 10;
    int mPariedDeviceCount = 0;
    private Set<BluetoothDevice> mDevices;
    private boolean delayed = false;

    private BluetoothAdapter mBluetoothAdapter;
    private static VibrationService service;
    private static DelayThread delayThread;
    private static NotificationManager manager;
    private BluetoothDevice mRemoteDevie;


   private BluetoothSocket mSocket = null;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private final String mStrDelimiter = "\n";
   private final char mCharDelimiter =  '\n';


    private Thread mWorkerThread = null;
    public static VibrationService getService(){
        return service;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = new VibrationService(this);
        delayThread = new DelayThread(this);
  /*    mEditReceive = (EditText)findViewById(R.id.receiveString);
        mEditSend = (EditText)findViewById(R.id.sendString);
       mButtonSend = (Button)findViewById(R.id.sendButton);
        manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mButtonSend.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {

                sendData(mEditSend.getText().toString());
                mEditSend.setText("");
            }
        });
*/

        checkBluetooth();
    }

    public static DelayThread getDelayThread() {
        return delayThread;
    }

    public boolean isDelayed() {
        return delayed;
    }

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
    }

    public void notify_(){
        Notification notification = new Notification(R.drawable.ic_launcher_foreground,"앞길에 장애물이 있습니다.",1);
        notification.defaults = Notification.DEFAULT_SOUND;

        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        notification.flags = Notification.FLAG_AUTO_CANCEL;

        manager.notify(0, notification);
    }

    BluetoothDevice getDeviceFromBondedList(String name) {
        // BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
        BluetoothDevice selectedDevice = null;

        for(BluetoothDevice deivce : mDevices) {
            if(name.equals(deivce.getName())) {
                selectedDevice = deivce;
                break;
            }
        }
        return selectedDevice;
    }


   private void connectToSelectedDevice(String selectedDeviceName) {
        // BluetoothDevice 원격 블루투스 기기를 나타냄.
        mRemoteDevie = getDeviceFromBondedList(selectedDeviceName);
        // java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            mSocket = mRemoteDevie.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            new SocketThread(mSocket,this).start();
        }catch(Exception e) {
            Toast.makeText(getApplicationContext(),
                    "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }



 private void selectDevice() {

        mDevices = mBluetoothAdapter.getBondedDevices();
        mPariedDeviceCount = mDevices.size();

        if(mPariedDeviceCount == 0 ) {
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");

        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : mDevices) {

            listItems.add(device.getName());
        }
        listItems.add("취소");



        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                // TODO Auto-generated method stub
                if(item == mPariedDeviceCount) {
                    Toast.makeText(getApplicationContext(), "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
                else {
                    connectToSelectedDevice(items[item].toString());
                }
            }

        });

        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }


   private void checkBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null ) {
            Toast.makeText(getApplicationContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
        else {

            if(!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
                selectDevice();
        }
    }




    @Override
    protected void onDestroy() {
        try{
            mWorkerThread.interrupt(); // 데이터 수신 쓰레드 종료
            mInputStream.close();
            mSocket.close();
        }catch(Exception e){}
        super.onDestroy();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    selectDevice();
                }
                else if(resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "블루투수를 사용할 수 없어 프로그램을 종료합니다",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
