package com.uom.pimote.managers;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.uom.pimote.Communicator;
import com.uom.pimote.R;
import com.uom.pimote.TCPClient;

import java.util.ArrayList;

public class RegularButtonManager extends PimoteManager {
    // Global Variables
    TCPClient tcp; // TCP Client for communication
    Context c; // Communicator context
    LinearLayout layout; // Linear Layout
    String ip; // IP of Pi
    int viewPosition; // Current position

    // Constructor. Sets up variables and action bar
    public RegularButtonManager(Context c, TCPClient tcp, String ip, String name, int id, int sensors, int orientation) {
        super(tcp, c, orientation);
        this.c = c;
        this.tcp = tcp;
        this.ip = ip;
        this.viewPosition = 0;

        ((Communicator) c).getActionBar().show();
        ((Communicator) c).getActionBar().setTitle(name + "(" + id + ")");
        ((Communicator) c).setContentView(R.layout.activity_main);
        this.layout = (LinearLayout) ((Communicator) c).findViewById(R.id.mainlayout);
        threads = new ArrayList<RecurringInfo>();
        if(sensors!=0){
            startSensors(sensors, tcp);
        }
    }

    @Override
    //Receives the messages from the Pi
    public void onMessage(String[] message) {
        super.onMessage(message);
    }
    @Override
    public void setup(String[] setup){
        switch (Integer.parseInt(setup[0])) {
            case CLEAR_ALL:
                Log.e("SETUP", "Clearing");
                layout.removeAllViews();
                viewPosition = 0;
                stopAllThreads();
                deleteThreads();
                break;
            case BUTTON:
                layout.addView(createButton(setup), viewPosition++);
                break;
            case TEXT_INPUT:
                layout.addView(createTextInput(setup), viewPosition++);
                break;
            case TOGGLE_BUTTON:
                layout.addView(createToggleButton(setup), viewPosition++);
                break;
            case TEXT_OUTPUT:
                layout.addView(createOutputText(setup), viewPosition++);
                break;
            case VIDEO_FEED:
                layout.addView(createVideoFeed(setup, ip), viewPosition++);
                break;
            case VOICE_INPUT:
                layout.addView(createVoiceInput(setup), viewPosition++);
                break;
            case RECURRING_INFO:
                addRecurringInformation(Integer.parseInt(setup[1]),
                        Integer.parseInt(setup[2]), tcp);
                break;
            case PROGRESS_BAR:
                layout.addView(createProgressBar(setup), viewPosition++);
                break;
            case SPACER:
                layout.addView(createSpacer(setup), viewPosition++);
                break;
            default:
                Log.e("SETUP", "Unknown component");
                break;
        }
    }
    @Override
    public void changeOutput(String[] info){
        if (Integer.parseInt(info[0]) == TEXT_OUTPUT) {
            TextView output = getTextView(Integer.parseInt(info[1]));
            String out = addIllegalChars(info[2]);
            output.setText(Html.fromHtml(out));
        } else if (Integer.parseInt(info[0]) == PROGRESS_BAR) {
            ProgressBar bar = getProgressBar(Integer.parseInt(info[1]));
            bar.setProgress(Integer.parseInt(info[2]));
        } else if (Integer.parseInt(info[0]) == TOGGLE_BUTTON) {
            ToggleButton t = getToggleButton(Integer.parseInt(info[1]));
            t.setChecked(Integer.parseInt(info[2]) == 1);
        }
    }


}
