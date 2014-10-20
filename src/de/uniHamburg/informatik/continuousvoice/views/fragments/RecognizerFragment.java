package de.uniHamburg.informatik.continuousvoice.views.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.constants.ServiceControlConstants;

public class RecognizerFragment extends Fragment {

    private static final String TAG = RecognizerFragment.class.getName();
    private String minutesStringSchema;
    private String wordsStringSchema;
    private boolean bound = false;
    private Messenger messenger;
    private static final String BROADCAST_IDENTIFIER = "voicerecognition.VOICE_RECOGNIZED";
    private static final String STATUS_BROADCAST_IDENTIFIER = "voicerecognition.STATUS";
    private int seconds = 0;
    private String completeText = "";
    private int words = 0;

    private ImageButton playBtn;
    private ImageButton stopBtn;
    private ImageButton clearBtn;
    private ImageButton shareBtn;

    private Spinner serviceSpinner;
    private TextView timeText;
    private TextView statusTextLine1;
    private TextView statusTextLine2;
    private TextView wordCountText;
    private TextView contentText;
    private ScrollView scrollWrapper;

    private short currentState = STATE_1_READY;
    public static final short STATE_1_READY = 1;
    public static final short STATE_2_WORKING = 2;
    public static final short STATE_3_DONE = 3;

    private Handler handler = new Handler();
    private boolean running = false;
    private Intent serviceIntent;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                seconds++;
                updateTimeText();
                handler.postDelayed(this, 1000);
            }
        }
    };

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            messenger = new Messenger(service);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    // This class handles the Service response
    private Handler responseHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ServiceControlConstants.SERVICE_CONTROL_RESPONSE:
                // boolean result = msg.getData().getBoolean("success");
                // addTextToView("success: " + result);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.voice_recognizer, container, false);

        playBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnPlay);
        stopBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnStop);
        clearBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnClear);
        shareBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnShare);

        timeText = (TextView) view.findViewById(R.id.voiceRecognizerTime);
        wordCountText = (TextView) view.findViewById(R.id.voiceRecognizerWordCount);
        contentText = (TextView) view.findViewById(R.id.voiceRecognizerContent);
        scrollWrapper = (ScrollView) view.findViewById(R.id.voiceRecognizerScrollWrapper);
        statusTextLine1 = (TextView) view.findViewById(R.id.voiceRecognizerState1);
        statusTextLine2 = (TextView) view.findViewById(R.id.voiceRecognizerState2);

        serviceSpinner = (Spinner) view.findViewById(R.id.serviceSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.services_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);

        Resources res = getResources();
        minutesStringSchema = res.getString(R.string.minutes);
        wordsStringSchema = res.getString(R.string.words);

        createListeners();
        resetTexts();
        updateButtonState();

        return view;
    }

    private void bindToService(String serviceClassName) {
        unbindFromService();

        try {
            // Bind to service
            serviceIntent = new Intent(getActivity(), Class.forName(serviceClassName));
            serviceIntent.putExtra("broadcastIdentifier", BROADCAST_IDENTIFIER);
            serviceIntent.putExtra("statusBroadcastIdentifier", STATUS_BROADCAST_IDENTIFIER);
            getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            bound = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "could not bind to " + serviceClassName + "\n" + e.getMessage());
            Toast.makeText(RecognizerFragment.this.getActivity(),
                    "Could not switch to " + serviceClassName + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void unbindFromService() {
        if (bound) {
            if (serviceConnection != null) {
                getActivity().unbindService(serviceConnection);
            }
            bound = false;
        }
    }

    private void startTimer() {
        running = true;
        handler.postDelayed(runnable, 1000);
    }

    private void stopTimer() {
        running = false;
    }

    private void resetTime() {
        seconds = 0;
    }

    private void createListeners() {
        playBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                play(v);
            }
        });
        stopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(v);
            }
        });
        clearBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clear(v);
            }
        });
        shareBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                share(v);
            }
        });

        serviceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String title = (String) parent.getItemAtPosition(position);
                String className = getResources().getString(
                        getResources().getIdentifier(title.replace(' ', '_'), "string",
                                "de.uniHamburg.informatik.continuousvoice"));

                Toast.makeText(RecognizerFragment.this.getActivity(), "title: " + title + "\nclass:" + className,
                        Toast.LENGTH_LONG).show();

                bindToService(className);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        BroadcastReceiver voiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String words = intent.getStringExtra("words");
                completeText += " " + words;
                addTextToView(words);
            }
        };
        getActivity().registerReceiver(voiceReceiver, new IntentFilter(BROADCAST_IDENTIFIER));

        BroadcastReceiver statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra("message");
                setStatus(status);
            }
        };
        getActivity().registerReceiver(statusReceiver, new IntentFilter(STATUS_BROADCAST_IDENTIFIER));
    }

    private void resetTexts() {
        timeText.setText(String.format(minutesStringSchema, "00:00:00"));
        wordCountText.setText(String.format(wordsStringSchema, "0"));
    }

    private void updateWordCount() {
        wordCountText.setText(String.format(wordsStringSchema, words + ""));
    }

    private void updateTimeText() {
        int h = seconds / 3600;
        int min = (seconds % 3600) / 60;
        int sec = seconds % 60;
        String formattedTime = String.format("%02d:%02d:%02d", h, min, sec);
        timeText.setText(String.format(minutesStringSchema, formattedTime));
    }

    private void setStatus(final String newStatus) {
        Log.e(TAG, newStatus);

        statusTextLine2.setText(statusTextLine1.getText());
        statusTextLine1.setText(newStatus);

        Animation inAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        statusTextLine1.setAnimation(inAnim);
    }

    private void switchState(short currentState) {
        this.currentState = currentState;
        updateButtonState();
    }

    private void updateButtonState() {

        switch (this.currentState) {
        case STATE_1_READY:
            playBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            clearBtn.setVisibility(View.GONE);
            shareBtn.setVisibility(View.GONE);
            break;
        case STATE_2_WORKING:
            playBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            clearBtn.setVisibility(View.GONE);
            shareBtn.setVisibility(View.GONE);
            break;
        case STATE_3_DONE:
            playBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.GONE);
            clearBtn.setVisibility(View.VISIBLE);
            shareBtn.setVisibility(View.VISIBLE);
            break;
        }
    }

    private void scrollDown() {
        scrollWrapper.post(new Runnable() {
            @Override
            public void run() {
                scrollWrapper.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void addTextToView(String toAdd) {
        contentText.append(" " + toAdd);
        completeText.replaceAll("\\s+", " ");
        words = completeText.trim().split("\\s+").length;
        updateWordCount();
        scrollDown();
    }

    public void play(View view) {
        if (send(ServiceControlConstants.START)) {
            switchState(STATE_2_WORKING);
            resetTime();
            startTimer();
        }
    }

    public void stop(View view) {
        if (send(ServiceControlConstants.STOP)) {
            switchState(STATE_3_DONE);
            stopTimer();
        }
    }

    public void clear(View view) {
        if (send(ServiceControlConstants.RESET)) {
            switchState(STATE_1_READY);
            stopTimer();
            resetTime();
            updateTimeText();
            completeText = "";
            words = 0;
            updateWordCount();
            contentText.setText("");
        }
    }

    public void share(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, completeText);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private boolean send(int code) {
        Message msg = Message.obtain(null, code);
        msg.replyTo = new Messenger(responseHandler);

        try {
            messenger.send(msg);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Could not send code " + code + ". " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onDestroy() {
        unbindFromService();
    }

}