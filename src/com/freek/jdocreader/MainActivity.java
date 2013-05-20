package com.freek.jdocreader;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	public static final String JDOX_SHARED_PREFERENCES =  "come.freek.jdocreader.MainActivity.JDOX_SHARED_PREFERENCES";
	
	public static final String EXTRA_JDOC_PATH = "com.freek.jdocreader.MainActivity.EXTRA_JDOC_PATH";
	public static final String EXTRA_INTERNAL_PATH = "com.freek.jdocreader.MainActivity.EXTRA_INTERNAL_PATH";
	private static final String BUNDLE_JAVADOC = "com.freek.jdocreader.MainActivity.BUNDLE_JAVADOC";
	public static final int REQUEST_FILEDIALOG = 9001;
	
	ListView list;
	EditText searchField;
	
	Javadoc jdoc;
	
    /** Called when the activity is first created. */
    @SuppressWarnings("rawtypes")
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		
		list = (ListView)findViewById(R.id.list);
		searchField = (EditText)findViewById(R.id.searchField);		
		
		
		list.setOnItemClickListener( new android.widget.AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
//				toast(((ListItem)list.getItemAtPosition(position)).getPath());
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra(EXTRA_JDOC_PATH, jdoc.getZipPath());
				intent.putExtra(EXTRA_INTERNAL_PATH, ((ListItem)list.getItemAtPosition(position)).getPath());				
				startActivity(intent);
		
			}
		});
		
		searchField.addTextChangedListener(new TextWatcher()
		{
				@Override 
				public void onTextChanged(CharSequence s, int start, int before, int count) 
				{
					if(list.getAdapter() != null)
						((TwoLineArrayAdapter)list.getAdapter()).getFilter().filter(s);
				}

				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

				@Override public void afterTextChanged(Editable s) { }
		});
		
		if(savedInstanceState != null)
		{
			
			Javadoc temp = savedInstanceState.getParcelable(BUNDLE_JAVADOC);
			if(temp != null)
			{
				jdoc = temp;
				jdoc.setResults(list);
				((TwoLineArrayAdapter)list.getAdapter()).getFilter().filter(searchField.getText().toString());
				Log.d("JDOX","Returned from savedInstanceState");
			}
			
		}
		Intent intent = getIntent();
		Log.d("JDOX","Received Intent in MainActivity" + 
				"\n	Action: " + intent.getAction() + 
				"\n	Categories: " + intent.getCategories() +
				"\n	Data: " + intent.getData());
		if(intent.getAction().equals(Intent.ACTION_VIEW))
		{
			
			Uri uri = intent.getData();
			if(uri != null)
			{
				loadJavadoc(uri);
				FileDialog.addToRecentlyOpened(this, new File(uri.getPath()));
			}
		}
    }
	
	public void toast(int text)
	{
		Toast.makeText(this,text,Toast.LENGTH_LONG).show();
	}
	
//	public void getItems(InputStream input) throws IOException
//	{
//		TagNode root = cleaner.clean(input);
//		TagNode[] nodes = root.getElementsByName("dt",true);
//		ArrayList<String> array = new ArrayList<String>();
//		for(TagNode node:nodes)
//		{
//			TagNode linkNode = node.findElementByName("a",true);
//			array.add(linkNode.getText().toString());
//		}
//		
//		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array);
//		list.setAdapter(adapter);
//		
//	}
	
	@SuppressWarnings("rawtypes")
	public void loadJavadoc(Uri uri)
	{
		try
		{ 
			jdoc = new Javadoc(this,uri);
			
			jdoc.setResults(list);
			((TwoLineArrayAdapter)list.getAdapter()).getFilter().filter(searchField.getText().toString());
	
			} catch (IOException e) {
			toast(R.string.invalid);
			e.printStackTrace();
		}
	}
	
	@Override 
	public boolean onCreateOptionsMenu(Menu menu) 
	{ 
		MenuInflater inflater = getMenuInflater(); 
		inflater.inflate(R.menu.menu, menu); 
		return true; 
	}
	
		
	public boolean onOptionsItemSelected(MenuItem item)
	{
		String state = Environment.getExternalStorageState();
		switch(item.getItemId())
		{
			case R.id.loadArchive:
				if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
				{
					Intent intent = new Intent(this, FileDialog.class);
					intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
					startActivityForResult(intent,REQUEST_FILEDIALOG);	
				}
				else
					toast(R.string.unable_to_read);
				
				//loadJavadoc(Environment.getExternalStorageDirectory()+"/download/jdoc2.zip");
				return true;
			
			case R.id.loadFolder:
				if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
				{
					Intent intent = new Intent(this, FileDialog.class);
					intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
					intent.putExtra(FileDialog.SELECT_DIR, true);
					startActivityForResult(intent,REQUEST_FILEDIALOG);	
				}
				else
					toast(R.string.unable_to_read);
				return true;
				
			case R.id.loadRecentlyUsed:
				if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
				{
					Intent intent = new Intent(this, FileDialog.class);
					intent.putExtra(FileDialog.CHOOSE_RECENTLY_USED, true);
					startActivityForResult(intent,REQUEST_FILEDIALOG);	
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);	
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == REQUEST_FILEDIALOG)
		{
			if(resultCode == Activity.RESULT_OK)
			{
				String uriString = (data.getStringExtra(FileDialog.RESULT_URI));
				Uri uri = Uri.parse(uriString);
				loadJavadoc(uri);
			}
			else
			{
//				toast("File selection was cancelled");
			}
		}
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
//		outState.put
		if(jdoc != null)
		{
			outState.putParcelable(BUNDLE_JAVADOC, jdoc);
		}
	}
	
	@Override 
	protected void onDestroy() 
	{
		super.onDestroy();
    try {
        trimCache(this);
       // Toast.makeText(this,"onDestroy " ,Toast.LENGTH_LONG).show();
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

}
	public static void trimCache(Context context) {
	    try {
	       File dir = context.getCacheDir();
	       if (dir != null && dir.isDirectory()) {
	          deleteDir(dir);
	       }
	    } catch (Exception e) {
	       // TODO: handle exception
	    	e.printStackTrace();
	    }
	 }
	
	 public static boolean deleteDir(File dir) {
	    if (dir != null && dir.isDirectory()) {
	       String[] children = dir.list();
	       for (int i = 0; i < children.length; i++) {
	          boolean success = deleteDir(new File(dir, children[i]));
	          if (!success) {
	             return false;
	          }
	       }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	 }
}
