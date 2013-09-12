package com.ecs.android.sample.widget;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidMediaPlayer extends Activity implements SeekBar.OnSeekBarChangeListener, OnSharedPreferenceChangeListener {
	private SharedPreferences prefs;
	MediaPlayer mediaPlayer;
	Button buttonPrev, buttonNext, buttonPlayPause, buttonQuit;
	TextView textState;
	
	private int stateMediaPlayer;
	private final int stateMP_Error = 0;
	private final int stateMP_NotStarter = 1;
	private final int stateMP_Playing = 2;
	private final int stateMP_Pausing = 3;
	
	String mArtist = "";
	String mAlbum = "";
	String mTrack = "";
	
	String mCurFolder = "";
	String mCurPathToFile = "";
	String mCurFileName = "";	
	
	String mNextPathToFile = "";
	String mNextFileName = "";

	String mPrevPathToFile = "";
	String mPrevFileName = "";
	
	TextView timer;
	int cur_time = 0;
	int timer_time = 0;
    SeekBar seekbar;
    
    static boolean CurrentlyRunning = false;

	private Handler mHandler = new Handler();
	
	//progress callback
	private Runnable mProgressCallback = new Runnable() {
		public void run() {
			if (mediaPlayer != null) {
				int x = mediaPlayer.getCurrentPosition();
				int y = mediaPlayer.getDuration() - 200;
				
				if (x >= y || x == 0) {
					//we've passed the end, stop it or restart loop
					Stop();
					
					if (mNextFileName != mCurFileName) {
						songNext();
					}
					return;
				} else {
					//update progress
					seekbar.setProgress(x);
					SetTimerText();
				}
			}
			mHandler.postDelayed(this, 200);
		}
	};
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.prefs.registerOnSharedPreferenceChangeListener(this);
        
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
      	    @Override
      	    public void onCallStateChanged(int state, String incomingNumber) {
      	        if (state == TelephonyManager.CALL_STATE_RINGING) {
      	            //Incoming call: Pause music
      	        	Pause();
      	        } else if(state == TelephonyManager.CALL_STATE_IDLE) {
      	            //Not in call: Play music
      	        } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
      	            //A call is dialing, active or on hold
      	        }
      	        super.onCallStateChanged(state, incomingNumber);
      	    }
      	};
      	
      	//Listen for incoming calls
      	TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                     
        setContentView(R.layout.player);
        
        mCurPathToFile = getIntent().getExtras().getString("fPath");
        mCurFolder = mCurPathToFile.substring(0, mCurPathToFile.lastIndexOf("/"));      		
        
        buttonPrev = (Button)findViewById(R.id.prev);
        buttonNext = (Button)findViewById(R.id.next);
        buttonPlayPause = (Button)findViewById(R.id.playpause);
        buttonQuit = (Button)findViewById(R.id.quit);
        textState = (TextView)findViewById(R.id.state);
        
        buttonPrev.setOnClickListener(buttonPrevOnClickListener);
        buttonNext.setOnClickListener(buttonNextOnClickListener);
        buttonPlayPause.setOnClickListener(buttonPlayPauseOnClickListener);
        buttonQuit.setOnClickListener(buttonQuitOnClickListener);
        
//        Intent intent = new Intent(getBaseContext(), SampleWidgetProvider4_1.class);
//		intent.setAction(Constants.ACTION_WIDGET_UPDATE_STATE);
//		intent.putExtra(Constants.INTENT_EXTRA_WIDGET_TEXT,"Play/Pause");
//
//		RemoteViews remoteViews = new RemoteViews(getBaseContext().getPackageName(), R.layout.widget_layout_4_1);		
//        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
//        remoteViews.setOnClickPendingIntent(R.id.icon,actionPendingIntent);
        
        seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setProgress(0);
        seekbar.setOnSeekBarChangeListener(this);
        
        timer = (TextView) findViewById(R.id.songtimer);		
        
        initMediaPlayer();
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		this.prefs.unregisterOnSharedPreferenceChangeListener(this);
	}
    
    @Override
    protected void onPause() {
        super.onPause();

//        SharedPreferences prefs = getSharedPreferences("X", MODE_PRIVATE);
//        Editor editor = prefs.edit();
//        editor.putString("lastActivity", getClass().getName());
//        editor.commit();
    }
    
    void updateSongList() {
    	File f = new File(mCurFolder);
    	File[]files = f.listFiles();
    	
    	Arrays.sort(files);
    	
    	for(int i = 0; i < files.length; i++)
        {    	
    		if (files[i].getName().equals(mCurFileName)) {
    			// tracks > 1 link back to previous track
    			if (i > 0) {
    				mPrevFileName = files[i-1].getName();
        			mPrevPathToFile = files[i-1].toString();    				
    			}
    			
    			// track 1 links previous to max track
    			else {
    				mPrevFileName = files[files.length-1].getName();
        			mPrevPathToFile = files[files.length-1].toString();
    			}
    			
    			//tracks > 1 < max link to next track
    			if (i < files.length - 1) {
    				mNextFileName = files[i+1].getName();
        			mNextPathToFile = files[i+1].toString();
    			}
    			
    			//max track links back to track 1
    			else {
    				mNextFileName = files[0].getName();
    				mNextPathToFile = files[0].toString();
    			}
    			break;
    		}
        }
    	
    	//set next song
    	if (mNextFileName != "") {
	    	TextView tSong = (TextView) findViewById(R.id.filename);
	        tSong.setText(tSong.getText() + "\r\n\r\nNext Track: " + mNextFileName + "\r\n");
    	}
    }
    
    private void initMediaPlayer()
    {
    	// split the file/directory info to fill in artist/album/track
        String[] fInfo = mCurPathToFile.split("/");     
        String strArtist = fInfo[fInfo.length-3];
        String strAlbum = fInfo[fInfo.length-2];
        String strTrack = fInfo[fInfo.length-1];
        
        TextView t = (TextView) findViewById(R.id.artist);
        t.setText("Artist: " + strArtist);        
        
        t = (TextView) findViewById(R.id.album);
        t.setText("Album: " + strAlbum);
        
        t = (TextView) findViewById(R.id.filename);
        mCurFileName = fInfo[fInfo.length-1];
        t.setText("Track: " + strTrack);
        
        mArtist = strArtist;
        mAlbum = strAlbum;
        mTrack = strTrack;
        
        //Update the widget
        UpdateWidget();
                
    	updateSongList();
    	
    	Intent bat = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
        int level = bat.getIntExtra("level", 0); 
        int scale = bat.getIntExtra("scale", 100);
        
        try {
        	textState.setText("Battery: " + level * 100 / scale + "%");
        } catch (Exception ex) {
        	textState.setText("- ERROR!!! -\r\n" + ex.getMessage());
        }
    	
    	mediaPlayer = new  MediaPlayer();
    	
    	try {
			mediaPlayer.setDataSource(mCurPathToFile);
			mediaPlayer.prepare();
			stateMediaPlayer = stateMP_NotStarter;
	        //textState.setText("- IDLE -");
			SetTimerText();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			stateMediaPlayer = stateMP_Error;
	        textState.setText("- ERROR!!! -");
		} catch (IllegalStateException e) {
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			stateMediaPlayer = stateMP_Error;
	        textState.setText("- ERROR!!! -");
		} catch (IOException e) {
			e.printStackTrace();
			//Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			//stateMediaPlayer = stateMP_Error;
	        //textState.setText("- ERROR!!! -");
		}
    	
    	CurrentlyRunning = true;
    }
    
    void UpdateWidget() {
    	//update the widget with the song info
        Intent i = new Intent(Constants.ACTION_WIDGET_UPDATE_FROM_ACTIVITY);
    	i.putExtra(Constants.INTENT_EXTRA_WIDGET_TITLE, mArtist);
    	i.putExtra(Constants.INTENT_EXTRA_WIDGET_TEXT, mTrack);
    	
    	//Play/pause
    	i.putExtra(Constants.INTENT_EXTRA_WIDGET_STATE, stateMediaPlayer);
    	
		sendBroadcast(i);
    }
    
    private void SetTimerText() {
    	if (mediaPlayer != null) {
	    	Date songTotal = new Date(mediaPlayer.getDuration());
	    	Date songCurrent = new Date(mediaPlayer.getCurrentPosition());

	    	timer.setText("Time: " + DateFormat.format("mm:ss", songCurrent) + "/" + DateFormat.format("mm:ss", songTotal));
    	}
    }
    
Button.OnClickListener buttonPrevOnClickListener = new Button.OnClickListener(){
	public void onClick(View v) {
		songPrev();
	}
};

Button.OnClickListener buttonNextOnClickListener = new Button.OnClickListener(){
	public void onClick(View v) {
		songNext();
	}
};
    
Button.OnClickListener buttonPlayPauseOnClickListener = new Button.OnClickListener(){
	public void onClick(View v) {
		// TODO Auto-generated method stub						    	
		switch(stateMediaPlayer){
		case stateMP_Error:
			break;
		case stateMP_NotStarter:					
			Play();
			break;
		case stateMP_Playing:					
			Pause();			        							        
			break;
		case stateMP_Pausing:
			Play();
			break;
		}
		
	}
};
    
    public void Play() {
    	mediaPlayer.start();
		buttonPlayPause.setText("Pause");
		//textState.setText("- PLAYING -");
		stateMediaPlayer = stateMP_Playing;
		
		//update the widget
		UpdateWidget();
		
		//start the progress timer
		seekbar.setMax(mediaPlayer.getDuration());
		mHandler.postDelayed(mProgressCallback, 100);
        SetTimerText();
    }
    
    public void Pause() {
    	buttonPlayPause.setText("Play");
		//textState.setText("- PAUSING -");
		stateMediaPlayer = stateMP_Pausing;
		
		//update the widget
		UpdateWidget();
		
		mediaPlayer.pause();
    }
    
    public void Stop() {
		//release mp
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		
		buttonPlayPause.setText("Play");
		seekbar.setProgress(0);
		
		//kill the progress
		mHandler.removeCallbacks(mProgressCallback);
		
		initMediaPlayer();
    }
    
    public void songNext() {
    	Stop();
		
		mCurPathToFile = mNextPathToFile;
                
        initMediaPlayer();
        Play();
    }
    
    public void songPrev() {
    	Stop();
		
		mCurPathToFile = mPrevPathToFile;
                
        initMediaPlayer();
        Play();
    }
    
    Button.OnClickListener buttonQuitOnClickListener = new Button.OnClickListener(){
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Stop();
			finish();
		}	
    };

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		//If user is dragging it, update the media player
  		if (fromUser && mediaPlayer != null) {
  			mediaPlayer.seekTo(progress);
  		}
  	}
    
  	//Progress bar
  	public void onStartTrackingTouch(SeekBar seekBar) {
  		//If user is dragging it, pause playback
  		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
  			mediaPlayer.pause();
  		}
  	}

  	//Progress bar
  	public void onStopTrackingTouch(SeekBar seekBar) {
  		//If user is done dragging it, resume playback.
  		if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
  			//mediaPlayer.start();  			
  			Play();
  			mediaPlayer.seekTo(seekBar.getProgress());
  		} 
  		else if (mediaPlayer == null) {
  			//it's not playing & not created, start it
  			Play();
  		}
  	}
  	
  	@Override
    public void onBackPressed() {
        if (mediaPlayer.isPlaying()) {
        	Pause();
        }        
    }
  	
  	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		if (Constants.ALARM_STATUS.equals(key)) {
			//toggleButtonText();
		}
	}
  	
  	public boolean IsRunning() {
  		return CurrentlyRunning;
  	}

}