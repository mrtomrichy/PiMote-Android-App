package com.uom.pimote.managers;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.uom.pimote.Communicator;
import com.uom.pimote.R;
import com.uom.pimote.TCPClient;
import com.uom.pimote.mjpegvideo.MjpegView;

import java.util.ArrayList;

public class RegularButtonManager extends PimoteManager {
    // Protocol Variables
    private static final int SETUP = 1;
    private static final int REQUEST_OUTPUT_CHANGE = 2;
    // Protocol Variables to decide what components to use
    private static final int BUTTON = 1, TEXT_INPUT = 2, TOGGLE_BUTTON = 3, TEXT_OUTPUT = 4,
            VIDEO_FEED = 5, VOICE_INPUT = 6, RECURRING_INFO = 7, PROGRESS_BAR = 8,
            SPACER = 9, CLEAR_ALL = 0;
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
        switch (Integer.parseInt(message[0])) {
            case SETUP:
                String[] setup = new String[message.length - 1];
                for (int i = 1; i < message.length; i++)
                    setup[i - 1] = message[i];
                addButtons(setup);
                break;

            case REQUEST_OUTPUT_CHANGE:
                if (Integer.parseInt(message[1]) == TEXT_OUTPUT) {
                    TextView output = getTextView(Integer.parseInt(message[2]));
                    String out = addIllegalChars(message[3]);
                    output.setText(Html.fromHtml(out));
                } else if (Integer.parseInt(message[1]) == PROGRESS_BAR) {
                    ProgressBar bar = getProgressBar(Integer.parseInt(message[2]));
                    bar.setProgress(Integer.parseInt(message[3]));
                } else if (Integer.parseInt(message[1]) == TOGGLE_BUTTON) {
                    ToggleButton t = getToggleButton(Integer.parseInt(message[2]));
                    t.setChecked(Integer.parseInt(message[3]) == 1);
                }
                break;
        }
    }

    // Add components to the screen
    public void addButtons(final String[] setup) {
        switch (Integer.parseInt(setup[0])) {
            case CLEAR_ALL:
                Log.e("SETUP", "Clearing");
                layout.removeAllViews();
                viewPosition = 0;
                stopAllThreads();
                deleteThreads();
                break;
            case BUTTON:
                addNewButton(setup);
                break;
            case TEXT_INPUT:
                addNewTextInput(setup);
                break;
            case TOGGLE_BUTTON:
                addNewToggle(setup);
                break;
            case TEXT_OUTPUT:
                addNewTextView(setup);
                break;
            case VIDEO_FEED:
                addNewFeed(setup, ip);
                break;
            case VOICE_INPUT:
                addVoiceInput(setup);
                break;
            case RECURRING_INFO:
                addRecurringInformation(Integer.parseInt(setup[1]),
                        Integer.parseInt(setup[2]), tcp);
                break;
            case PROGRESS_BAR:
                addProgressBar(Integer.parseInt(setup[1]), Integer.parseInt(setup[2]));
                break;
            case SPACER:
                addSpacer(Integer.parseInt(setup[1]));
                break;
            default:
                Log.e("SETUP", "Unknown component");
                break;
        }
    }

    public String addIllegalChars(String string){
        return string.replace("%/", ",");
    }

    // Adds a new button to the screen
    public void addNewButton(final String[] setup) {
        Button button = new Button(c);
        button.setText(addIllegalChars(setup[2]));
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (tcp != null)
                    send(setup[1] + "," + " ");
            }
        });
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 5, 10, 5);
        button.setLayoutParams(params);
        layout.addView(button, viewPosition++);
    }

    // Adds a text input to the screen
    public void addNewTextInput(final String[] setup) {
        LinearLayout textButtonLayout = new LinearLayout(c);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        textButtonLayout.setLayoutParams(params);
        final EditText addText = new EditText(c);
        addText.setHint(addIllegalChars(setup[2]));
        LayoutParams params2 = new TableRow.LayoutParams(0,
                LayoutParams.WRAP_CONTENT, 1f);
        addText.setLayoutParams(params2);
        Button button = new Button(c);
        button.setText("Send");
        button.setGravity(3);
        button.setOnClickListener(new OnClickListener() {
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
        layout.addView(textButtonLayout, viewPosition++);
    }

    // Adds a ToggleButton to the screen
    public void addNewToggle(final String[] setup) {
        LinearLayout textButtonLayout = new LinearLayout(c);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        textButtonLayout.setLayoutParams(params);
        TextView text = new TextView(c);
        text.setTextSize(16);
        text.setText(addIllegalChars(setup[2]));
        LayoutParams params2 = new TableRow.LayoutParams(0,
                LayoutParams.WRAP_CONTENT, 1f);
        text.setLayoutParams(params2);
        ToggleButton button = new ToggleButton(c);
        boolean checked = Integer.parseInt(setup[3]) == 1;
        button.setChecked(checked);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int tf = ((ToggleButton) view).isChecked() ? 1 : 0;
                send(setup[1] + "," + tf);
            }
        });
        textButtonLayout.addView(text);
        textButtonLayout.addView(button);
        button.setId(Integer.parseInt(setup[1]));
        layout.addView(textButtonLayout, viewPosition++);
    }

    // Adds an output TextView to the screen
    public void addNewTextView(final String[] setup) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        TextView text = new TextView(c);
        text.setTextSize(18);
        text.setLayoutParams(params);
        if (setup.length >= 3) {
            String out = addIllegalChars(setup[2]);
            text.setText(Html.fromHtml(out));
        }
        text.setTextSize(Integer.parseInt(setup[3]));
        layout.addView(text, viewPosition++);
        text.setId(Integer.parseInt(setup[1]));
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

    // Add a new Video Feed (MjpgView) to the screen
    public void addNewFeed(String[] setup, String ip) {
        String feedIp = ip;
        if (Integer.parseInt(setup[3]) == 1) feedIp = setup[4];
        String URL = "http://" + feedIp + ":8080/?action=stream";
        MjpegView mv = new MjpegView(c);
        startVideo(mv, URL);
        LayoutParams params = new LayoutParams(Integer.parseInt(setup[1]),
                Integer.parseInt(setup[2]));
        params.setMargins(0, 10, 0, 10);
        mv.setLayoutParams(params);
        mv.setVisibility(View.VISIBLE);
        layout.addView(mv, viewPosition++);
    }

    // Add a voice input button to the screen
    public void addVoiceInput(final String[] setup) {
        ImageButton voice = new ImageButton(c);
        voice.setImageDrawable(c.getResources().getDrawable(R.drawable.mic));
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        voice.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        voice.setLayoutParams(params);
        final int id = Integer.parseInt(setup[1]);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Communicator) c).startVoiceRecognition(id);
            } // onClick()
        });
        layout.addView(voice, viewPosition++);
    }

    // Add a progress bar to the screen
    public void addProgressBar(int id, int maxValue) {
        ProgressBar bar = new ProgressBar(c, null, android.R.attr.progressBarStyleHorizontal);
        bar.setProgress(0);
        bar.setMax(maxValue);
        bar.setId(id);
        layout.addView(bar, viewPosition++);
    }

    // Add a spacer (blank space) to the screen
    public void addSpacer(int size) {
        View v = new View(c);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
        v.setLayoutParams(params);
        layout.addView(v, viewPosition++);
    }


}
