package com.freek.jdocreader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class FileDialog extends Activity
{
	//required extras
	public static final String START_PATH = "com.freek.jdocreader.FileDialog.START_PATH";
	
	//optional extras
	public static final String SHOW_HIDDEN = "com.freek.jdocreader.FileDialog.SHOW_HIDDEN";
	public static final String SELECT_DIR = "com.freek.jdocreader.FileDialog.SELECT_DIR";
		/* if this is true, you don't need START_PATH */
	public static final String CHOOSE_RECENTLY_USED = "com.freek.jdocreader.FileDialog.CHOOSE_RECENTLY_USED";
	
	//result extras
	public static final String RESULT_PATH = "com.freek.jdocreader.FileDialog.RESULT_PATH";

	//recently used SharedPreferences key
	public static final String RECENTLY_USED_KEY = "com.freek.jdocreader.FileDialog.RECENTLY_USED_KEY";
	public static final int RECENTLY_USED_MAX = 5;
	
	File currentDir;
	List<File> files;
	List<String> filenames;
	
	
	ListView fileList;
	ArrayAdapter<String> listAdapter;
	
	Button selectBtn;
	
	boolean showHidden;
	boolean selectDir;
	boolean chooseRecentlyUsed;
	
	int backButtonStack; // How many times we can press the back button to go up a directory without exiting the dialog
	
	public void onCreate(Bundle savedInstanceState)
	{
		showHidden = getIntent().getBooleanExtra(SHOW_HIDDEN, false);
		selectDir = getIntent().getBooleanExtra(SELECT_DIR, false);
		chooseRecentlyUsed = getIntent().getBooleanExtra(CHOOSE_RECENTLY_USED, false);
		
		backButtonStack = 0;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filedialog);
		fileList = (ListView)findViewById(R.id.filelist);
		selectBtn = (Button)findViewById(R.id.selectBtn);
		
		selectBtn.setVisibility((selectDir)?View.VISIBLE:View.GONE);
		
		if(!chooseRecentlyUsed)
			setCurrentDir(new File(getIntent().getStringExtra(START_PATH)));
		else
			displayRecentlyUsed();
		
		fileList.setOnItemClickListener( new android.widget.AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					File file = files.get(position);
					if(file.isDirectory() && !chooseRecentlyUsed)
					{
						backButtonStack++;
						setCurrentDir(file);
					}
					else //if file is... file
					{
						if(!selectDir)
						{
							returnResult(file);	
						}
						else
						{
//							Toast.makeText(this,"Selection must be a folder",Toast.LENGTH_SHORT).show();	
						}
					}
				}
			});
			
		selectBtn.setOnClickListener( new View.OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				returnResult(currentDir);
			}
			
		} );
	}
		
	public void setCurrentDir(File dir)
	{
		currentDir = dir;
		String title = dir.getName();
		setTitle(title);
		if(files == null)
			files = new ArrayList<File>();
		else
			files.clear();
		for(File f:currentDir.listFiles())
		{
			if(showHidden == false && f.isHidden())
				continue;
			else if(f.canRead())
				files.add(f);
		}
			
		
		Collections.sort(files);
		
		if(filenames == null)
			filenames = new ArrayList<String>();
		else
			filenames.clear();
		for(File f:files)
			filenames.add(f.getName());
		files.add(0, new File(currentDir.getParent()));
		filenames.add(0, "..");
		
		
		if(listAdapter == null)
		{
			listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filenames);
			fileList.setAdapter(listAdapter);
		}
		else
			listAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onBackPressed()
	{
		if(backButtonStack > 0)
		{
			backButtonStack--;
			setCurrentDir(currentDir.getParentFile()); //assuming first item is always parent
		}
		else
			super.onBackPressed();
	}
	
	public void returnResult(File f)
	{
		addToRecentlyOpened(f);
		Intent result = new Intent();
		result.putExtra(RESULT_PATH,f.getPath());
		setResult(RESULT_OK,result);
		finish();	
	}
	
	public void addToRecentlyOpened(File f)
	{
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
		String[] recentlyUsed = new String[RECENTLY_USED_MAX];
		int alreadyExists = -1;
		for(int k=0;k<recentlyUsed.length;k++)
		{
			recentlyUsed[k] = prefs.getString(RECENTLY_USED_KEY+String.valueOf(k), "");
			if(recentlyUsed[k].equals(f.getPath()))
				alreadyExists = k;
		}
		if(alreadyExists == -1)
		{
			for(int k=recentlyUsed.length-2; k>=0; k--)
				recentlyUsed[k+1] = recentlyUsed[k];
			recentlyUsed[0] = f.getPath();
		}
		else
		{
			for(int k=alreadyExists-1; k>=0; k--)
				recentlyUsed[k+1] = recentlyUsed[k];
			recentlyUsed[0] = f.getPath();

		}
		Editor editor = prefs.edit();
		for(int k=0;k<recentlyUsed.length;k++)
		{
			editor.putString(RECENTLY_USED_KEY+String.valueOf(k),recentlyUsed[k]);
			Log.d("JDOX", "Recently used #"+k+": "+recentlyUsed[k]);
		}
		editor.commit();

	}
	
	public void displayRecentlyUsed()
	{
		setTitle("Recently Used Javadocs");
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
		filenames = new ArrayList<String>();
		files = new ArrayList<File>();
		for(int k=0;k<RECENTLY_USED_MAX;k++)
		{
			String item = prefs.getString(RECENTLY_USED_KEY+String.valueOf(k),"");
			if(item.length() != 0)
			{
				filenames.add(item);
				files.add(new File(item));
			}
				
		}
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filenames);
		fileList.setAdapter(listAdapter);
		
	}
	
}
