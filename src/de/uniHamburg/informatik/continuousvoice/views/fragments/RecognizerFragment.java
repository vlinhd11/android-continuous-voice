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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.constants.RecognitionConstants;

public class RecognizerFragment extends Fragment {

    private String name;
    private String contentHintString;
    private String minutesStringSchema;
    private String wordsStringSchema;
    private Class<?> serviceClazz;
    private boolean bound = false;
    private Messenger messenger;
    private String broadcastIdentifier;

    private ImageButton playBtn;
    private ImageButton stopBtn;
    private ImageButton clearBtn;
    private ImageButton shareBtn;

    private TextView titleText;
    private TextView timeText;
    private TextView wordCountText;
    private TextView contentText;
    private ScrollView scrollWrapper;

    private short currentState = STATE_1_READY;
    public static final short STATE_1_READY = 1;
    public static final short STATE_2_WORKING = 2;
    public static final short STATE_3_DONE = 3;

    public RecognizerFragment(String name, Class<?> serviceClazz) {
        this.name = name;
        this.serviceClazz = serviceClazz;
        this.broadcastIdentifier = serviceClazz.toString() + ".VOICE_RECOGNIZED";
    }

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
            int respCode = msg.what;

            switch (respCode) {
            case RecognitionConstants.SERVICE_CONTROL_RESPONSE:
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

        titleText = (TextView) view.findViewById(R.id.voiceRecognizerTitle);
        timeText = (TextView) view.findViewById(R.id.voiceRecognizerTime);
        wordCountText = (TextView) view.findViewById(R.id.voiceRecognizerWordCount);
        contentText = (TextView) view.findViewById(R.id.voiceRecognizerContent);
        scrollWrapper = (ScrollView) view.findViewById(R.id.voiceRecognizerScrollWrapper);

        Resources res = getResources();
        contentHintString = String.format(res.getString(R.string.fragment_hint), name);
        minutesStringSchema = res.getString(R.string.minutes);
        wordsStringSchema = res.getString(R.string.words);

        // Bind to service
        Intent intent = new Intent(getActivity(), serviceClazz);
        intent.putExtra("broadcastIdentifier", broadcastIdentifier);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        createListeners();
        resetTexts();
        updateButtonState();

        return view;
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

        BroadcastReceiver voiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String words = intent.getStringExtra("words");
                addTextToView(words);
            }
        };
        getActivity().registerReceiver(voiceReceiver, new IntentFilter(broadcastIdentifier));

    }

    private void resetTexts() {
        titleText.setText(name);
        contentText.setHint(contentHintString);
        timeText.setText(String.format(minutesStringSchema, "0:00"));
        wordCountText.setText(String.format(wordsStringSchema, "0"));
        
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
        scrollDown();
    }

    public void play(View view) {
        switchState(STATE_2_WORKING);
        addTextToView("» ");
        send(RecognitionConstants.START_RECOGNIZING);
    }

    public void stop(View view) {
        switchState(STATE_3_DONE);
        addTextToView(" «");
        send(RecognitionConstants.STOP_RECOGNIZING);
    }

    public void clear(View view) {
        switchState(STATE_1_READY);
        send(RecognitionConstants.RESET_SERVICE);
        contentText.setText("");
    }

    public void share(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, contentText.getText());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void send(int code) {
        Message msg = Message.obtain(null, code);
        msg.replyTo = new Messenger(responseHandler);

        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}