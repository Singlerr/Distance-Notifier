package kr.playar.playar_2;

import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Erroneous on 2018-03-24.
 */

public class SocketThread extends Thread{
    private BluetoothSocket socket;
    private MainActivity activity;
    public SocketThread(BluetoothSocket socket, MainActivity activity){
        this.socket = socket;
        this.activity =activity;
    }
    @Override
    public void run() {
        try {
            while (true) {
                InputStream in = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String arg = reader.readLine();
                if(arg.trim().startsWith("access")){
                    MainActivity.getService().vibrate();
                    MainActivity.getService().notify_();
                    MainActivity.getDelayThread().run();
                }
            }
        }catch (IOException ex){
            try{
                socket.close();
                return;
            }catch (IOException e){
                return;
            }

        }
    }
}
