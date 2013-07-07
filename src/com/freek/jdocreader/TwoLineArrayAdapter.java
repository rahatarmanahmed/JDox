package com.freek.jdocreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

//TODO: Make this take in an interface so it can be used on other objects
public abstract class TwoLineArrayAdapter<T> extends ArrayAdapter<T>
{
	Activity context;
	
	List<T> originalItems;
	
	private final Object lock = new Object();
	
	FuzzyFilter fuzzyFilter;
	
	public TwoLineArrayAdapter(Activity context, List<T> items)
	{
		super(context, R.layout.listitem, items);
		this.context = context;
		synchronized(lock)
		{
			this.originalItems = new ArrayList<T>(items);	
		}
		
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
	
	public Filter getFuzzyFilter()
	{
		if(fuzzyFilter == null)
			return fuzzyFilter = new FuzzyFilter();
		return fuzzyFilter;
	}
	
	private class FuzzyFilter extends Filter
	{

		@Override
		protected FilterResults performFiltering(CharSequence query)
		{
			Log.d("JDOX", "Performing Filtering with '" + query + "'");
			// Mostly copied from the source of ArrayAdapter$ArrayFilter, except it uses contains not startsWith
			FilterResults results = new FilterResults();

            if (query == null || query.length() == 0) {
                ArrayList<T> list;
                synchronized (lock) {
                    list = new ArrayList<T>(originalItems);
                }
                results.values = list;
                results.count = list.size();
            } else {
//                String queryStr = query.toString().toLowerCase(Locale.getDefault());
            	StringBuilder regex = new StringBuilder();
            	for(int k=0; k<query.length(); k++)
            	{
            		regex.append("[^");
            		regex.append(query.charAt(k));
            		regex.append("]*");
            		regex.append(query.charAt(k));
            	}
            	regex.append(".*");
            	Pattern pattern = Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);

                ArrayList<T> values;
                synchronized (lock) {
                    values = new ArrayList<T>(originalItems);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<T>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase(Locale.getDefault());

                    // First match against the whole, non-splitted value
                    if (pattern.matcher(valueText).matches()) {
                        newValues.add(value);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results)
		{
			clear();
			addAll((List<T>)results.values);
			if(results.count > 0)
				notifyDataSetChanged();
			else
				notifyDataSetInvalidated();
		}
		
	}
}
