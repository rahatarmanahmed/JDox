package com.freek.jdocreader;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class FileDialog extends Activity
{
	//required extras
	public static final String START_PATH = "com.freek.jdocreader.FileDialog.START_PATH";
	
	//optional extras
	public static final String SHOW_HIDDEN = "com.freek.jdocreader.FileDialog.SHOW_HIDDEN";
	public static final String SELECT_DIR = "com.freek.jdocreader.FileDialog.SELECT_DIR";
	
	//result extras
	public static final String RESULT_PATH = "com.freek.jdocreader.FileDialog.RESULT_PATH";
	
	
	File currentDir;
	List<File> files;
	List<String> filenames;
	
	
	ListView fileList;
	Button selectBtn;
	
	boolean showHidden;
	boolean selectDir;
	
	public void onCreate(Bundle savedInstanceState)
	{
		showHidden = getIntent().getBooleanExtra(SHOW_HIDDEN, false);
		selectDir = getIntent().getBooleanExtra(SELECT_DIR, false);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filedialog);
		fileList = (ListView)findViewById(R.id.filelist);
		selectBtn = (Button)findViewById(R.id.selectBtn);
		
		selectBtn.setVisibility((selectDir)?View.VISIBLE:View.GONE);
		
		setCurrentDir(new File(getIntent().getStringExtra(START_PATH)));
		
		fileList.setOnItemClickListener( new android.widget.AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					File file = files.get(position);
					if(file.isDirectory())
					{
						setCurrentDir(file);
					}
					else //if file is... file
					{
						if(!selectDir)
						{
							Intent result = new Intent();
							result.putExtra(RESULT_PATH,file.getPath());
							setResult(RESULT_OK,result);
							finish();	
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
				Intent result = new Intent();
				result.putExtra(RESULT_PATH,currentDir.getPath());
				setResult(RESULT_OK,result);
				finish();	
			}
			
		} );
	}
		
	public void setCurrentDir(File dir)
	{
		currentDir = dir;
		this.setTitle(dir.getPath());
		
		files = new ArrayList<File>();
		for(File f:currentDir.listFiles())
		{
			if(showHidden == false && f.isHidden())
				continue;
			else if(f.canRead())
				files.add(f);
		}
			
		
		Collections.sort(files);
		
		filenames = new ArrayList<String>();
		for(File f:files)
			filenames.add(f.getName());
		
		files.add(0, new File(currentDir.getParent()));
		filenames.add(0, "..");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filenames);
		fileList.setAdapter(adapter);
	}
}
