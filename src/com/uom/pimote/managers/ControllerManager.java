package com.uom.pimote.managers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.uom.pimote.Communicator;
import com.uom.pimote.R;
import com.uom.pimote.TCPClient;
import com.uom.pimote.mjpegvideo.MjpegView;

public class ControllerManager extends PimoteManager {

    boolean forwardPress, backPress, leftPress, rightPress = false;
    TCPClient tcp;
    String URL;
    ImageView hud;
    private MjpegView mv = null;
    final TextView ultrasonic;
    final TextView battery;

    public ControllerManager(final Context c, final TCPClient tcp, String ip, int videoV, int voiceV, int recurringV, int sleepTime, int orientation) {
        super(tcp, c, orientation);
        ((Communicator)c).getActionBar().hide();
        ((Communicator)c).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((Communicator)c).setContentView(R.layout.controllayout);


        this.tcp = tcp;
        final ImageView leftForward;
        final ImageView leftBackwards;
        final ImageView rightForward;
        final ImageView rightBackwards;
        final ImageView microphone;



        boolean video = videoV != 0;
        boolean voice = voiceV != 0;
        boolean recurring = recurringV != 0;
        leftForward = (ImageView) ((Communicator) c).findViewById(R.id.left_motor_forward);
        leftBackwards = (ImageView) ((Communicator) c).findViewById(R.id.left_motor_backwards);
        rightForward = (ImageView) ((Communicator) c).findViewById(R.id.right_motor_forward);
        rightBackwards = (ImageView) ((Communicator) c).findViewById(R.id.right_motor_backwards);
        microphone = (ImageView) ((Communicator) c).findViewById(R.id.microphone);
        hud = (ImageView) ((Communicator) c).findViewById(R.id.HUD);
        ultrasonic = (TextView) ((Communicator) c).findViewById(R.id.ultrasonicReading);
        battery = (TextView) ((Communicator) c).findViewById(R.id.batteryReading);


        leftForward.setClickable(true);
        leftBackwards.setClickable(true);
        rightForward.setClickable(true);
        rightBackwards.setClickable(true);
        microphone.setClickable(true);

        leftForward.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_UP)
                    toggleControl(1,
                            event.getAction() == MotionEvent.ACTION_DOWN);
                return false;
            }

        });
        leftBackwards.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_UP)
                    toggleControl(2,
                            event.getAction() == MotionEvent.ACTION_DOWN);
                return false;
            }

        });
        rightForward.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_UP)
                    toggleControl(3,
                            event.getAction() == MotionEvent.ACTION_DOWN);
                return false;
            }

        });
        rightBackwards.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_UP)
                    toggleControl(4,
                            event.getAction() == MotionEvent.ACTION_DOWN);
                return false;
            }

        });

        if (voice) {
            microphone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Communicator) c).startVoiceRecognition(1);
                } // onClick()
            });
            microphone.setVisibility(View.VISIBLE);
        } else {
            microphone.setVisibility(View.INVISIBLE);
        } // if - else

        URL = "http://" + ip + ":8080/?action=stream";

        mv = (MjpegView) ((Communicator) c).findViewById(R.id.mv);

        if (video) {
            mv.setVisibility(View.VISIBLE);
            hud.setVisibility(View.VISIBLE);
            startVideo(mv, URL);
        } else {
            mv.setVisibility(View.INVISIBLE);
            hud.setVisibility(View.INVISIBLE);
        }

        if(recurring){
            Log.e("RECURRING", "Sleep time: " + sleepTime);
            addRecurringInformation(2, sleepTime, tcp);
        }

    }
    @Override
    public void onMessage(String[] message){
        //DO WHATEVER
        Log.e("CONTROL", "Received message");
        battery.setText(message[0]);
        ultrasonic.setText(message[1]);
    }

    public void toggleControl(int position, boolean value) {
        int flag = value ? 1 : 0;
        switch (position) {
            case 1:
                if (forwardPress != value)
                    send("0," + (0 + flag));
                forwardPress = value;
                break;
            case 2:
                if (backPress != value)
                    send("0," + (2 + flag));
                backPress = value;
                break;
            case 3:
                if (leftPress != value)
                    send("0," + (4 + flag));
                leftPress = value;
                break;
            case 4:
                if (rightPress != value)
                    send("0," + (6 + flag));
                rightPress = value;
                break;
        }
    }
}
