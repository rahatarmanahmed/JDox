package com.freek.jdocreader;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class Javadoc implements Parcelable
{
//	static HtmlCleaner cleaner = new HtmlCleaner();

	ArrayList<ListItem> items;
	String path;
	Activity activity;
	
	public Javadoc(Activity activity, String path) throws IOException
	{
		//TODO:just load classes by recursive directory search or find faster parser
		items = new ArrayList<ListItem>();
		this.path = path;
		this.activity = activity;
		
		
		File f = new File(path);
		if(!f.isDirectory()) // load archive
		{
			ZipFile file = new ZipFile(f);
			ZipEntry entry = file.getEntry("package-list");
			if(entry == null)
			{
				file.close();
				throw new IOException("Package list does not exist in archive");
			}
			InputStream is = file.getInputStream(entry);
		
			String packageList = WebViewActivity.readStringFromStream(is);
			Scanner scan = new Scanner(packageList);
			ArrayList<String> packages = new ArrayList<String>();
			while(scan.hasNextLine())
				packages.add(scan.nextLine().replace('.','/'));
		
			ArrayList<String> blacklist = new ArrayList<String>();
			blacklist.add("package-tree");
			blacklist.add("package-frame");
			blacklist.add("package-summary");
		
			Enumeration<? extends ZipEntry> entries = file.entries();
			while(entries.hasMoreElements())
			{
				entry = entries.nextElement();
				String name = entry.getName();
				if(!entry.isDirectory() && !containsAny(name, blacklist) && containsAny(name, packages))
				{
				
					name = name.substring(name.lastIndexOf('/')+1,name.length()-5);
					String lePath = entry.getName();
					String desc = entry.getName().substring(0, entry.getName().length()-5).replace('/','.');
					if(!lePath.contains("class-use") && !lePath.contains("package-use"))
						items.add(new ListItem(name, desc, lePath));
				}	
				
				if(!entry.isDirectory() && !entry.getName().toLowerCase().endsWith(".html") && !entry.getName().toLowerCase().endsWith(".mf"))
				{
					InputStream stream = file.getInputStream(entry);
					File out = new File(activity.getCacheDir(),entry.getName());
					Javadoc.writeStreamToFile(stream, out);
				}
			}
			file.close();
			scan.close();
		}
		else //load directory
		{
			String packageList = WebViewActivity.readStringFromStream(new FileInputStream(new File(f,"package-list")));
			Scanner scan = new Scanner(packageList);
			ArrayList<String> packages = new ArrayList<String>();
			while(scan.hasNextLine())
				packages.add(scan.nextLine().replace('.','/'));

			ArrayList<String> blacklist = new ArrayList<String>();
			blacklist.add("package-tree");
			blacklist.add("package-frame");
			blacklist.add("package-summary");
			ArrayList<File> entries = new ArrayList<File>();
			for(String p:packages)
			{
				File pack = new File(f,p);
				for(File foo:pack.listFiles())
					entries.add(foo);
			}
			for(File entry:entries)
			{
				String name = entry.getPath();
				if(!entry.isDirectory() && !containsAny(name, blacklist) && containsAny(name, packages))
				{
					name = name.substring(name.lastIndexOf('/')+1,name.length()-5);
					String lePath = entry.getPath();
					String desc = lePath.replace(f.getPath(),"").replace('/','.');
					desc = desc.substring(1,desc.length()-5);
					if(!lePath.contains("class-use") && !lePath.contains("package-use"))
						items.add(new ListItem(name, desc, lePath));
				}	
			}
			scan.close();
		}
	}

	public String getZipPath()
	{
		return path;
	}
	
	public void setResults(ListView list)
	{
		list.setAdapter(new TwoLineArrayAdapter((Activity)list.getContext(), items));
	}
	
	public int describeContents()
	{
		return 0;
	}
	
	public void writeToParcel(Parcel parcel, int flags)
	{
		parcel.writeString(path);
		parcel.writeList(items);
	}
	
	public static final Parcelable.Creator<Javadoc> CREATOR = new Parcelable.Creator<Javadoc>() 
	{
		public Javadoc createFromParcel(Parcel in) 
			{ return new Javadoc(in); }

		public Javadoc[] newArray(int size) 
		{ return new Javadoc[size]; } 
	};
	
	public Javadoc(Parcel in)
	{
		path = in.readString();
		items = new ArrayList<ListItem>();
		in.readList(items,null);
	}
	
	
	public static void writeStreamToFile(InputStream in, File file) throws IOException	{
		File parent = new File(file.getParent());
		if(file.exists())
		{
			file.delete();
		}
		if(!parent.exists())
		{
			parent.mkdirs();
		}
		FileOutputStream out = new FileOutputStream(file);
//		byte[] buffer = new byte[1024];
		int ch;
		while((ch = in.read())!=-1)
			out.write(ch);
		out.close();
	}	
	
	public boolean containsAny(String str, ArrayList<String> list)
	{
		for(String s:list)
			if(str.contains(s))
				return true;
		return false;		
	}
}
