package com.freek.jdocreader;

import android.app.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class TwoLineArrayAdapter extends ArrayAdapter<ListItem>
{
	Activity context;
	public TwoLineArrayAdapter(Activity context, List<ListItem> items)
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
		
		label1.setText(this.getItem(position).getName());
		label2.setText(this.getItem(position).getDescription());
		
		return newView;
	}
}
