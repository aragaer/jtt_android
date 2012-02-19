package com.aragaer.jtt;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class JTTHelp extends JTTPager {
    private static final String TAG = JTTHelp.class.getSimpleName();
    private final ArrayList<String> shown = new ArrayList<String>();
    private final ArrayList<String> categories = new ArrayList<String>();
    private final HashMap<String, ArrayList<String>> topics = new HashMap<String, ArrayList<String>>();
    private final HashMap<String, View> c_views = new HashMap<String, View>();
    private final HashMap<String, String> t_views = new HashMap<String, String>();

    private Context ctx;

    public JTTHelp(Context context) {
        super(context, null);
        ctx = context;
        setOrientation(LinearLayout.VERTICAL);

        final Resources res = getResources();

        final TypedArray cats = res.obtainTypedArray(R.array.categories);
        for (int i = 0; i < cats.length(); i++) {
            int cid = cats.getResourceId(i, 0);
            if (cid == 0) {
                Log.w(TAG, "Expected category id, got spomething strange at "
                        + i);
                continue;
            }

            TypedArray cat = res.obtainTypedArray(cid);
            String catname = cat.getString(0);
            categories.add(catname);
            topics.put(catname, new ArrayList<String>());

            for (int j = 1; j < cat.length(); j++) {
                int tid = cat.getResourceId(j, 0);
                if (tid == 0) {
                    Log.w(TAG, "Expected topic id, got spomething strange at "
                            + i + ":" + j);
                    continue;
                }
                TypedArray top = res.obtainTypedArray(tid);
                String topname = top.getString(0);
                topics.get(catname).add(topname);
                t_views.put(topname, top.getString(1));
                top.recycle();
            }
            cat.recycle();
        }
        cats.recycle();

        // we've ingested the help file, good
        final ListView toc = new ListView(ctx);
        toc.setAdapter(new ArrayAdapter<String>(ctx,
                android.R.layout.simple_list_item_1, categories));
        toc.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                String catname = (String) arg0.getItemAtPosition(arg2);
                switchToCat(catname);
            }
        });
        addTab(toc, ctx.getString(R.string.TOC));
        shown.add(ctx.getString(R.string.TOC));
    }

    private void switchToCat(String cn) {
        if (c_views.get(cn) == null)
            c_views.put(cn, makeTList(cn));
        if (!shown.contains(cn)) {
            if (shown.size() > 1) {
                // remove other tabs here
            }
            addTab(c_views.get(cn), cn);
            shown.add(cn);
        }
        pageview.snapToScreen(1);
    }

    private View makeTList(String cn) {
        final ArrayList<String> l = topics.get(cn);
        if (l == null)
            return null;
        final ListView v = new ListView(ctx);
        v.setAdapter(new ArrayAdapter<String>(ctx,
                android.R.layout.simple_list_item_1, l));
        return v;
    }
}
