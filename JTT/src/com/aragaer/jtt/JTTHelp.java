package com.aragaer.jtt;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class JTTHelp extends JTTPager {
    private static final String TAG = JTTHelp.class.getSimpleName();
    private final ArrayList<String> categories = new ArrayList<String>();
    private final HashMap<String, ArrayList<String>> topics = new HashMap<String, ArrayList<String>>();
    private final HashMap<String, String> views = new HashMap<String, String>();

    public JTTHelp(Context context) {
        super(context, null);
        setOrientation(LinearLayout.VERTICAL);

        final Resources res = getResources();

        final TypedArray cats = res.obtainTypedArray(R.array.categories);
        for (int i = 0; i < cats.length(); i++) {
            int cid = cats.getResourceId(i, 0);
            if (cid == 0) {
                Log.w(TAG, "Expected category id, got spomething strange at "+i);
                continue;
            }

            TypedArray cat = res.obtainTypedArray(cid);
            String catname = cat.getString(0);
            categories.add(catname);
            topics.put(catname, new ArrayList<String>());

            for (int j = 1; j < cat.length(); j++) {
                int tid = cat.getResourceId(j, 0);
                if (tid == 0) {
                    Log.w(TAG, "Expected topic id, got spomething strange at "+i+":"+j);
                    continue;
                }
                TypedArray top = res.obtainTypedArray(tid);
                String topname = top.getString(0);
                topics.get(catname).add(topname);
                views.put(topname, top.getString(1));
                top.recycle();
            }
            cat.recycle();
        }
        cats.recycle();

        // we've ingested the help file, good
        final ListView toc = new ListView(context);
        toc.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, categories));
        addTab(toc, context.getString(R.string.TOC));
    }
}
