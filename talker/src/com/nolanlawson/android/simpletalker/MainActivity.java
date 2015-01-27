package com.nolanlawson.android.simpletalker;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends Activity implements OnClickListener, OnInitListener, OnUtteranceCompletedListener {

    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_SONG = "song";
    public static final String EXTRA_SONG_DURATION = "songDuration";
    
    private static final int DEFAULT_DURATION = 10; // 10 seconds
    
    private String text;
    private String song;
    private long songDuration;
    private TextView textView;
    private Button playAgainButton;
    private TextToSpeech textToSpeech;
    private MediaPlayer mPlayer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(android.R.id.text1);
        playAgainButton = (Button) findViewById(android.R.id.button1);
        playAgainButton.setOnClickListener(this);
        
        text = getIntent() != null && getIntent().hasExtra(EXTRA_TEXT) 
        	? getIntent().getStringExtra(EXTRA_TEXT) : getString(R.string.default_text);
        	
        textView.setText(text);
        
        song = getIntent() != null && getIntent().hasExtra(EXTRA_SONG) 
        	? getIntent().getStringExtra(EXTRA_SONG) : null;
        	
        songDuration = getIntent() != null && getIntent().hasExtra(EXTRA_SONG_DURATION)
        	? Long.parseLong(getIntent().getStringExtra(EXTRA_SONG_DURATION)) : DEFAULT_DURATION;
        
        
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onInit(int status) {
		if (status != TextToSpeech.SUCCESS)
		{
			Log.w("Talker", "Error: onInit() not TextToSpeech.SUCCESS");
			finish(); //System.exit(1);
		}
		textToSpeech.setOnUtteranceCompletedListener(this);
		textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onDone(String id) {
				Log.i("Talker", "onDone: " + id);
				finish();
				System.exit(0);
			}

			@Override
			public void onError(String id) {
				Log.w("Talker", "Error: " + id);
				finish();
				System.exit(0);
			}

			@Override
			public void onError(String id, int code) {
				Log.w("Talker", "Error: " + id + " code " + code);
				finish();
				System.exit(0);
			}

			@Override
			public void onStart(String id) {
				Log.i("Talker", "onStart: " + id);
			}
		});
		speakAndPlayMusic();
    }
    
    @Override
    public void onClick(View v) {
		// play it again clicked
		speakAndPlayMusic();
    }
    
    private void speakAndPlayMusic() {
	new AsyncTask<Void, Void, Void>(){

	    @Override
	    protected Void doInBackground(Void... params) {
		speakAndPlayMusicInBackground();
		return null;
	    }
	}.execute((Void)null);
    }
    
    private void speakAndPlayMusicInBackground() {
	
	File file;
	if (song != null && (file = new File(song)).exists()) {
	    // play song first
    	    mPlayer = MediaPlayer.create(this, Uri.fromFile(file));
    	    mPlayer.start();
    	    try {
		Thread.sleep(TimeUnit.SECONDS.toMillis(songDuration));
	    } catch (InterruptedException e) { /* shouldn't happen */ }
    	    mPlayer.stop();
    	    try {
		Thread.sleep(1000); // pause between music stop and TTS
	    } catch (InterruptedException e) { /* shouldn't happen */ }
    	    speak();
	} else {
	    // just speak
	    speak();
	}
    }
    
    private void speak() {
	HashMap<String, String> params = new HashMap<String, String>();
    	params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
    	//textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    	textToSpeech.synthesizeToFile(text, params, "/sdcard/Talk/" + text.replaceAll("[^a-zA-Z0-9]", "-") + ".wav");
		Log.i("Talker", "synthesizeToFile command sent");
    }

    @Override
    public void onUtteranceCompleted(String arg0) {
		Log.i("Talker", "onUtteranceCompleted: " + arg0);
		finish();
		System.exit(0);
    }

    @Override
    public void onStop() {
	super.onStop();
	if (mPlayer != null) {
	    mPlayer.release();
	}
	if (textToSpeech != null) {
	    textToSpeech.shutdown();
	}
    }
}
