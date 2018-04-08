package kr.playar.playar_2;

import android.app.Activity;


/**
 * Created by Erroneous on 2018-03-25.
 */

public class DelayThread extends Thread{
    private MainActivity activity;
    public DelayThread(MainActivity activity){
        this.activity = activity;
    }
    @Override
    public void run() {
        try{
          if(! activity.isDelayed()){
              sleep(5000);
              activity.setDelayed(true);
          }
        }catch (InterruptedException ex){

        }
    }
}
