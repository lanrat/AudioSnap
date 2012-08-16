package com.vorsk.audiosnap;

import java.util.Iterator;
import java.util.List;

import com.dropbox.client2.DropboxAPI.Entry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DBFileArrayAdapter extends ArrayAdapter<Entry>{

	private final Context context;
	private final List<Entry> values;

	public DBFileArrayAdapter(Context context, List<Entry> values) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		
		
		//remove all folders
		Iterator<Entry> it = values.iterator();
		Entry current;
		while (it.hasNext()){
			current = it.next();
			if (current.isDir){
				it.remove();
			}
		}
		
		
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
		
		TextView textView = (TextView) rowView.findViewById(R.id.name);
		//an array would be faster, but I'm gona use a list....
		String filename = values.get(position).fileName();
		int offset = filename.lastIndexOf(AudioSnapActivity.EXT);
		if (offset != -1){
			//remove the ext
			filename = filename.substring(0, offset);
		}
		textView.setText(filename); //set the text

		return rowView;
	}
	
}
