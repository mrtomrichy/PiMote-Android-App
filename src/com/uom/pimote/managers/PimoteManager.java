package com.uom.pimote.managers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;

import com.uom.pimote.Communicator;
import com.uom.pimote.SensorManagement;
import com.uom.pimote.TCPClient;
import com.uom.pimote.mjpegvideo.MjpegStreamManager;
import com.uom.pimote.mjpegvideo.MjpegView;

import java.util.ArrayList;

/**
 * Created by Tom on 02/07/2013.
 */
public class PimoteManager {

    // Global Variables
    ArrayList<RecurringInfo> threads; // All threads
    TCPClient tcp; // TCP Client for communication with Pi
    ArrayList<MjpegStreamManager> streams; // All streams
    Context c;
    SensorManagement sensors = null;


    // Constructor (takes tcp client)
    public PimoteManager(TCPClient tcp, Context c, int orientation) {
        this.tcp = tcp;
        threads = new ArrayList<RecurringInfo>();
        streams = new ArrayList<MjpegStreamManager>();
        this.c = c;
        int orient = orientation == 0 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        ((Communicator) c).setRequestedOrientation(orient);
    }

    // Called when a message is received. Overridden by manager
    public void onMessage(String[] message) {
    }

    // Start the video on a certain stream
    public void startVideo(MjpegView mv, String URL) {
        MjpegStreamManager stream = new MjpegStreamManager();
        streams.add(stream);
        stream.startVideo(mv, URL);
    }

    public void stopVideo() {
        for(MjpegStreamManager s : streams){
            s.stopVideo();
        }
    }

    public void pause() {
        for(MjpegStreamManager s : streams){
            s.pauseVideo();
        }
        if(sensors!=null)sensors.pause();
    }

    public void resume() {
        for(MjpegStreamManager s : streams){
            s.resumeVideo();
        }
        if(sensors!=null)sensors.resume();
    }

    public void startSensors(int speed, TCPClient tcp){
        sensors = new SensorManagement(c, speed, tcp);
    }

    // Recurring poll to the Pi
    public void addRecurringInformation(int id, int sleepTime, TCPClient tcp) {
        RecurringInfo t = new RecurringInfo(id, sleepTime, tcp);
        threads.add(t);
        t.start();
    }

    // Stop all threads from running
    public void stopAllThreads() {
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).stopThread();
        }
        stopVideo();
        if(sensors!= null)sensors.pause();
    }

    public void deleteThreads(){
        threads.clear();
        streams.clear();
    }


    // Send a message back to the pi
    public void send(String message){
        tcp.sendMessage(Communicator.SEND_DATA + "," + message);
    }

    // Recurring info thread. Sends a message every x milliseconds
    class RecurringInfo extends Thread {
        boolean running = true;
        int id;
        int sleepTime;
        TCPClient tcp;

        public RecurringInfo(int id, int sleepTime, TCPClient tcp) {
            this.tcp = tcp;
            this.id = id;
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    tcp.sendMessage(Communicator.SEND_DATA + "," + id);
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    Log.e("THREAD", "Sleep error");
                }
            }
        }

        public void stopThread() {
            Log.e("SETUP", "Thread stopped");
            running = false;
        }
    }

}
