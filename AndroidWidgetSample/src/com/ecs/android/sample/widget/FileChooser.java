package com.ecs.android.sample.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooser extends ListActivity implements OnSharedPreferenceChangeListener{
    /** Called when the activity is first created. */
	private static String ROOT_DIR = "sdcard/Music";
	private File currentDir;
	private FileArrayAdapter adapter;
	
	@Override    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        currentDir = new File("/"+ROOT_DIR+"/");
        fill(currentDir);
    }
	
	private void fill(File f)
    {
        File[]dirs = f.listFiles();
         this.setTitle("Current Dir: "+f.getName());
         List<Option>dir = new ArrayList<Option>();
         List<Option>fls = new ArrayList<Option>();
         
         try{
             for(File ff: dirs)
             {
            	 
                if(ff.isDirectory()) {
                	dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                }
                else
                {
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                }
             }
         }catch(Exception e)
         {
             
         }
         Collections.sort(dir);
         Collections.sort(fls);
         dir.addAll(fls);
         if(!f.getPath().equalsIgnoreCase("/" + ROOT_DIR))
             dir.add(0,new Option("..","Parent Directory",f.getParent()));
                  
         adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
		 this.setListAdapter(adapter);
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		}
		else {
			onFileClick(FileChooser.this, o);
		}
	}
	
	private void onFileClick(Context context, Option o)
    {
    	Toast.makeText(this, ""+o.getName(), Toast.LENGTH_SHORT).show();
    	
		//open the player
    	Intent i = new Intent(FileChooser.this, AndroidMediaPlayer.class);
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	i.putExtra("fPath", o.getPath());
        startActivity(i);
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		int x = 0;
	}

}