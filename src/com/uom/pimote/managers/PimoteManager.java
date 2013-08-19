package com.uom.pimote.managers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.uom.pimote.Communicator;
import com.uom.pimote.R;
import com.uom.pimote.SensorManagement;
import com.uom.pimote.TCPClient;
import com.uom.pimote.mjpegvideo.MjpegStreamManager;
import com.uom.pimote.mjpegvideo.MjpegView;

import java.util.ArrayList;

/**
 * Created by Tom on 02/07/2013.
 */
public class PimoteManager {

    // Protocol Variables
    public static final int SETUP = 1;
    public static final int REQUEST_OUTPUT_CHANGE = 2;
    // Protocol Variables to decide what components to use
    public static final int BUTTON = 1, TEXT_INPUT = 2, TOGGLE_BUTTON = 3, TEXT_OUTPUT = 4,
            VIDEO_FEED = 5, VOICE_INPUT = 6, RECURRING_INFO = 7, PROGRESS_BAR = 8,
            SPACER = 9, SCROLLED_OUTPUT_TEXT = 10, CLEAR_ALL = 0;

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
        switch (Integer.parseInt(message[0])) {
            case SETUP:
                String[] setup = new String[message.length - 1];
                System.arraycopy(message, 1, setup, 0, message.length-1);
                setup(setup);
                break;

            case REQUEST_OUTPUT_CHANGE:
                String[] info = new String[message.length - 1];
                System.arraycopy(message, 1, info, 0, message.length-1);
                changeOutput(info);
                break;
        }
    }

    public void setup(String[] setup) {
    }

    public void changeOutput(String[] info) {
    }

    public String addIllegalChars(String string) {
        return string.replace("%/", ",");
    }

    //Component type, ID
    public Button createButton(final String[] setup) {
        Button button = new Button(c);
        button.setText(addIllegalChars(setup[2]));
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (tcp != null)
                    send(setup[1] + "," + " ");
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 5, 10, 5);
        button.setLayoutParams(params);
        return button;
    }

    //Component type, Id, Hint
    public LinearLayout createTextInput(final String[] setup) {
        LinearLayout textButtonLayout = new LinearLayout(c);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        textButtonLayout.setLayoutParams(params);
        final EditText addText = new EditText(c);
        addText.setHint(addIllegalChars(setup[2]));
        LinearLayout.LayoutParams params2 = new TableRow.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        addText.setLayoutParams(params2);
        Button button = new Button(c);
        button.setText("Send");
        button.setGravity(3);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (tcp != null) {
                    String text = addText.getText().toString();
                    if (text.equals("")) text = "null";
                    send(setup[1] + "," + text);
                }
                addText.setText("");
            }
        });
        textButtonLayout.addView(addText);
        textButtonLayout.addView(button);

        return textButtonLayout;
    }

    //Component type, Id, Text, Initial
    public LinearLayout createToggleButton(final String[] setup) {
        LinearLayout textButtonLayout = new LinearLayout(c);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        textButtonLayout.setLayoutParams(params);
        TextView text = new TextView(c);
        text.setTextSize(16);
        text.setText(addIllegalChars(setup[2]));
        LinearLayout.LayoutParams params2 = new TableRow.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        text.setLayoutParams(params2);
        ToggleButton button = new ToggleButton(c);
        boolean checked = Integer.parseInt(setup[3]) == 1;
        button.setChecked(checked);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tf = ((ToggleButton) view).isChecked() ? 1 : 0;
                send(setup[1] + "," + tf);
            }
        });
        textButtonLayout.addView(text);
        textButtonLayout.addView(button);
        button.setId(Integer.parseInt(setup[1]));

        return textButtonLayout;
    }

    //Component type, Id, Initial text, Text Size
    public TextView createOutputText(final String[] setup) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        TextView text = new TextView(c);
        text.setTextSize(18);
        text.setLayoutParams(params);
        if (setup.length >= 3) {
            String out = addIllegalChars(setup[2]);
            text.setText(Html.fromHtml(out));
        }
        text.setTextSize(Integer.parseInt(setup[3]));
        text.setId(Integer.parseInt(setup[1]));

        return text;
    }

    //Component type, Id, Initial text, Text Size
    public ScrollView createScrolledOutputText(final String[] setup) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        TextView text = new TextView(c);
        text.setTextSize(18);
        text.setLayoutParams(params);
        if (setup.length >= 3) {
            String out = addIllegalChars(setup[2]);
            text.setText(Html.fromHtml(out));
        }
        text.setTextSize(Integer.parseInt(setup[3]));
        text.setId(Integer.parseInt(setup[1]));
        final ScrollView s = new ScrollView(c);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Integer.parseInt(setup[4]));
        s.addView(text);
        s.setLayoutParams(p);
        s.post(new Runnable() {
            @Override
            public void run() {
                s.fullScroll(View.FOCUS_DOWN);
            }
        });
        return s;
    }

    //Component type, Id, Outside Ip, IP
    public MjpegView createVideoFeed(final String[] setup, String ip) {
        String feedIp = ip;
        if (Integer.parseInt(setup[3]) == 1) feedIp = setup[4];
        String URL = "http://" + feedIp + ":8080/?action=stream";
        MjpegView mv = new MjpegView(c);
        startVideo(mv, URL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Integer.parseInt(setup[1]),
                Integer.parseInt(setup[2]));
        params.setMargins(0, 10, 0, 10);
        mv.setLayoutParams(params);
        mv.setVisibility(View.VISIBLE);

        return mv;
    }

    //Component type, Id
    public ImageButton createVoiceInput(final String[] setup) {
        ImageButton voice = new ImageButton(c);
        voice.setImageDrawable(c.getResources().getDrawable(R.drawable.mic));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        voice.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        voice.setLayoutParams(params);
        final int id = Integer.parseInt(setup[1]);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Communicator) c).startVoiceRecognition(id);
            } // onClick()
        });

        return voice;
    }

    //Component type, Id, MaxValue
    public ProgressBar createProgressBar(String[] setup) {
        ProgressBar bar = new ProgressBar(c, null, android.R.attr.progressBarStyleHorizontal);
        bar.setProgress(0);
        bar.setMax(Integer.parseInt(setup[2]));
        bar.setId(Integer.parseInt(setup[1]));
        return bar;
    }

    //Component type, Id, Height
    public View createSpacer(String[] setup) {
        View v = new View(c);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Integer.parseInt(setup[1]));
        v.setLayoutParams(params);
        return v;
    }

    // Returns the TextView with the matching ID
    public TextView getTextView(int id) {
        return (TextView) ((Communicator) c).findViewById(id);
    }

    // Returns the ProgressBar with the matching ID
    public ProgressBar getProgressBar(int id) {
        return (ProgressBar) ((Communicator) c).findViewById(id);
    }

    // Returns the ToggleButton with the matching ID
    public ToggleButton getToggleButton(int id) {
        return (ToggleButton) ((Communicator) c).findViewById(id);
    }

    // Start the video on a certain stream
    public void startVideo(MjpegView mv, String URL) {
        MjpegStreamManager stream = new MjpegStreamManager();
        streams.add(stream);
        stream.startVideo(mv, URL);
    }

    public void stopVideo() {
        for (MjpegStreamManager s : streams) {
            s.stopVideo();
        }
    }

    public void pause() {
        for (MjpegStreamManager s : streams) {
            s.pauseVideo();
        }
        if (sensors != null) sensors.pause();
    }

    public void resume() {
        for (MjpegStreamManager s : streams) {
            s.resumeVideo();
        }
        if (sensors != null) sensors.resume();
    }

    public void startSensors(int speed, TCPClient tcp) {
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
        for (RecurringInfo i : threads) {
            i.stopThread();
        }
        stopVideo();
        if (sensors != null) sensors.pause();
    }

    public void deleteThreads() {
        threads.clear();
        streams.clear();
    }


    // Send a message back to the pi
    public void send(String message) {
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
