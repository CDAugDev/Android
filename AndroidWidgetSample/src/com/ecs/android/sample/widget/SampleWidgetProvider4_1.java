package com.ecs.android.sample.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class SampleWidgetProvider4_1 extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(Constants.TAG, "Onupdate called for " + this);
		
		  RemoteViews remoteViews = new RemoteViews(context.getPackageName (), R.layout.widget_layout_4_1); 

		  // When the icon on the widget is clicked, we send out a broadcast
		  // containing the ACTION_WIDGET_UPDATE_FROM_WIDGET.
		  // In the manifest, the widget is configured with an intent filter to match this action.
		  Intent intent = new Intent(context, SampleWidgetProvider4_1.class);
		  intent.putExtra(Constants.INTENT_EXTRA_WIDGET_TEXT,"Icon clicked on Widget");

          PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
          remoteViews.setOnClickPendingIntent(R.id.icon,actionPendingIntent);
		  
          // When we click the widget, we want to open our main activity.
          Intent defineIntent2 = new Intent(context,FileChooser.class);
          PendingIntent pendingIntent2 = PendingIntent.getActivity(context,0 /* no requestCode */, defineIntent2, 0 /* no flags */);          
          remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent2);
          
          ComponentName thisWidget = new ComponentName(context, SampleWidgetProvider4_1.class);
          AppWidgetManager manager = AppWidgetManager.getInstance(context);
          manager.updateAppWidget(thisWidget, remoteViews);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(Constants.TAG, "onReceive called with " + intent.getAction());
		RemoteViews remoteViews = new RemoteViews(context.getPackageName (), R.layout.widget_layout_4_1);
		if (intent.getAction().equals(Constants.ACTION_WIDGET_UPDATE_FROM_ACTIVITY)) {
			String widgetText = intent.getExtras().getString(Constants.INTENT_EXTRA_WIDGET_TEXT);
			String widgetTitle = intent.getExtras().getString(Constants.INTENT_EXTRA_WIDGET_TITLE);
			remoteViews.setTextViewText(R.id.word_text, widgetText);
			remoteViews.setTextViewText(R.id.word_title, widgetTitle);
			
			//set play button state
			Integer mediaState = intent.getExtras().getInt(Constants.INTENT_EXTRA_WIDGET_STATE);
			if (mediaState == 2) {
				remoteViews.setImageViewResource(R.id.icon, R.drawable.pause_blue);
			}
			else {
				remoteViews.setImageViewResource(R.id.icon, R.drawable.play_blue);
			}
		} else if (intent.getAction().equals(Constants.ACTION_WIDGET_UPDATE_FROM_WIDGET)) {    
        	//set the play or pause icon
//        	ImageView image = (ImageView)findViewById(R.id.icon);        	
//        	if (image.getTag() == "pause_blue") {
//        		remoteViews.setImageViewResource(R.id.icon, R.drawable.play_blue);
//        	}
//        	else {
//        		remoteViews.setImageViewResource(R.id.icon, R.drawable.pause_blue);
//        	}
        	remoteViews.setImageViewResource(R.id.icon, R.drawable.pause_blue);
			String widgetText = null;
			if (intent.getExtras()!= null) {
				widgetText = intent.getExtras().getString(Constants.INTENT_EXTRA_WIDGET_TEXT);
			} else {
				widgetText = "Text was null";
			}
        	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
			remoteViews.setTextViewText(R.id.word_text, widgetText + " at " + sdf.format(new Date()) );
        } else {
        	super.onReceive(context, intent);
        }
		ComponentName cn = new ComponentName(context, SampleWidgetProvider4_1.class);  
		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);

	}
	
}
