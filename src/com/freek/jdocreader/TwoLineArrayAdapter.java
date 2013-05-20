package com.freek.jdocreader;

import android.app.*;
import android.view.*;
import android.widget.*;
import java.util.*;

//TODO: Make this take in an interface so it can be used on other objects
public abstract class TwoLineArrayAdapter<T> extends ArrayAdapter<T>
{
	Activity context;
	public TwoLineArrayAdapter(Activity context, List<T> items)
	{
		super(context, R.layout.listitem, items);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View newView = convertView;
		if(newView == null)
		{
			LayoutInflater inflator = context.getLayoutInflater();
			newView = inflator.inflate(R.layout.listitem, null);
		}
		
		TextView label1 = (TextView)newView.findViewById(R.id.text1);
		TextView label2 = (TextView)newView.findViewById(R.id.text2);
		
		label1.setText(getPrimaryText(this.getItem(position)));
		label2.setText(getSecondaryText(this.getItem(position)));
		
		return newView;
	}
	
	public abstract String getPrimaryText(T t);
	public abstract String getSecondaryText(T t);
}
