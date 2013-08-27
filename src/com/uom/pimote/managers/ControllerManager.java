package com.uom.pimote.managers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.Html;
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
    private TextView ultrasonic;
    private TextView battery;
    ImageView microphone;

    public ControllerManager(final Context c, final TCPClient tcp, String ip) {
        super(tcp, c, PimoteManager.ORIENTATION_LANDSCAPE);
        ((Communicator) c).getActionBar().hide();
        ((Communicator) c).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((Communicator) c).setContentView(R.layout.controllayout);


        this.tcp = tcp;
        final ImageView leftForward;
        final ImageView leftBackwards;
        final ImageView rightForward;
        final ImageView rightBackwards;

        leftForward = (ImageView) ((Communicator) c).findViewById(R.id.left_motor_forward);
        leftBackwards = (ImageView) ((Communicator) c).findViewById(R.id.left_motor_backwards);
        rightForward = (ImageView) ((Communicator) c).findViewById(R.id.right_motor_forward);
        rightBackwards = (ImageView) ((Communicator) c).findViewById(R.id.right_motor_backwards);
        microphone = (ImageView) ((Communicator) c).findViewById(R.id.microphone);
        hud = (ImageView) ((Communicator) c).findViewById(R.id.HUD);


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

        URL = "http://" + ip + ":8080/?action=stream";

        mv = (MjpegView) ((Communicator) c).findViewById(R.id.mv);

        mv.setVisibility(View.VISIBLE);
        hud.setVisibility(View.VISIBLE);
        startVideo(mv, URL);


    }

    public void toggleControl(int position, boolean value) {
        int flag = value ? 1 : 0;
        switch (position) {
            case 1:
                if (forwardPress != value)
                    send("444," + (0 + flag));
                forwardPress = value;
                break;
            case 2:
                if (backPress != value)
                    send("444," + (2 + flag));
                backPress = value;
                break;
            case 3:
                if (leftPress != value)
                    send("444," + (4 + flag));
                leftPress = value;
                break;
            case 4:
                if (rightPress != value)
                    send("444," + (6 + flag));
                rightPress = value;
                break;
        }
    }

    @Override
    public void setup(String[] setup) {
        Log.e("Setup", "Setup");
        if (Integer.parseInt(setup[0]) == TEXT_OUTPUT) {
            Log.e("SETUP", "TEXT");
            if (battery == null) {
                battery = (TextView) ((Communicator) c).findViewById(R.id.batteryReading);
                battery.setId(Integer.parseInt(setup[1]));
                battery.setTextColor(Color.RED);
            } else if (ultrasonic == null) {
                ultrasonic = (TextView) ((Communicator) c).findViewById(R.id.ultrasonicReading);
                ultrasonic.setId(Integer.parseInt(setup[1]));
            }
        } else if (Integer.parseInt(setup[0]) == RECURRING_INFO) {
            Log.e("SETUP", "Adding poll");
            addRecurringInformation(Integer.parseInt(setup[1]), Integer.parseInt(setup[2]), tcp);
        } else if (Integer.parseInt(setup[0]) == VOICE_INPUT){
            microphone = (ImageView)((Communicator)c).findViewById(R.id.microphone);
            microphone.setId(Integer.parseInt(setup[1]));
            microphone.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    ((Communicator) c).startVoiceRecognition(microphone.getId());
                }
            });
        }
    }

    @Override
    public void changeOutput(String[] info) {
        int type = Integer.parseInt(info[0]);
        if (type == TEXT_OUTPUT || type == SCROLLED_OUTPUT_TEXT) {
            TextView output = getTextView(Integer.parseInt(info[1]));
            String out = addIllegalChars(info[2]);
            output.setText(Html.fromHtml(out));
        }
    }
}