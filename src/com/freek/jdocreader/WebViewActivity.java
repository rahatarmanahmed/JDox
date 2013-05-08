package com.freek.jdocreader;

import android.app.*;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class WebViewActivity extends Activity
{

	WebView webView;
	ZipFile jdoc;
	File jdocDir;
	String path;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.webview);
		webView = (WebView)findViewById(R.id.webview);

		if (savedInstanceState != null)
		{

		      ((WebView)findViewById(R.id.webview)).restoreState(savedInstanceState);
		}
		else
		{
			webView = (WebView)findViewById(R.id.webview);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setBuiltInZoomControls(true);
			
			// Let's display the progress in the activity title bar, like the
			// browser app does. 

			
			final Activity act =this;
			webView.setWebChromeClient(new WebChromeClient()
			{
				public void onProgressChanged(WebView view, int progress)
				{ 
				// Activities and WebViews measure progress with different scales. 
				// The progress meter will automatically disappear when we reach 100%
					act.setProgress(progress * 100);
				}
			});
			
			
			this.setContentView(webView);
			WebViewClient client = new WebViewClient ()
			{
				@Override
				public boolean shouldOverrideUrlLoading(WebView webView, String url)
				{
					if(url.contains("file://"))
					{
							url = url.replace("file://","");
							url = url.replace(getCacheDir().getPath()+"/","");
							loadPage(url);
							return true;
					}
					
					return false;
				}
			};
			webView.setWebViewClient(client);
			String path = this.getIntent().getStringExtra(MainActivity.EXTRA_INTERNAL_PATH);
			String jdocPath = this.getIntent().getStringExtra(MainActivity.EXTRA_JDOC_PATH);
			try
			{
			File f = new File(jdocPath);
			if(!f.isDirectory()) //use archive
				jdoc = new ZipFile(jdocPath);
			else //use dir
				jdocDir = new File(jdocPath);
			loadPage(path);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void toast(String text)
	{
		Toast.makeText(this,text,Toast.LENGTH_LONG).show();
	}
	
	public void loadPage(String path)
	{
		String url = path;
		String anchor = "";
		if(path.lastIndexOf('#') != -1)
		{
			 url = path.substring(0,path.lastIndexOf('#'));
			 anchor = path.substring(path.lastIndexOf('#'));
		}
		
		final String fUrl = url;
		final String fAnchor = anchor;
		if(jdoc != null) // load archive
		{
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(R.string.loading);
			dialog.setMessage(getString(R.string.extracting));
			
			dialog.show();
			Thread thread = new Thread(){
				@Override
				public void run()
				{
					try
					{
					InputStream in = jdoc.getInputStream(jdoc.getEntry(fUrl));
					File htmlFile = new File(getCacheDir().toString()+"/"+fUrl);
					Javadoc.writeStreamToFile(in, htmlFile);
					webView.loadUrl("file://"+htmlFile.toURL().getPath() + fAnchor);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						dialog.dismiss();	
					}
				}
			};
			thread.start();
		}
		else //load from dir
		{
			webView.loadUrl("file://"+path);
		}
	}
	
	public static String readStringFromStream(InputStream in)
	{
		try
		{
		Scanner scan = new Scanner(in);
		scan.useDelimiter("\\A");
		return scan.next();
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
		
	}
	
	protected void onSaveInstanceState(Bundle outState) {
	      webView.saveState(outState);
	   }
}
