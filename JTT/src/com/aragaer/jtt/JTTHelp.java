package com.aragaer.jtt;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class JTTHelp extends JTTPager {
    private static final String TAG = JTTHelp.class.getSimpleName();
    private final ArrayList<String> shown = new ArrayList<String>();
    private final ArrayList<String> categories = new ArrayList<String>();
    private final HashMap<String, ArrayList<String>> topics = new HashMap<String, ArrayList<String>>();
    private final HashMap<String, String> t_views = new HashMap<String, String>();
    private ListView c_toc; // list of topics in a category
    private TextView t_cont; // topic contents

    private Context ctx;

    public JTTHelp(Context context) {
        super(context, JTTPager.TABS_FRONT | JTTPager.TABS_WRAP);
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
                String cn = (String) arg0.getItemAtPosition(arg2);
                switchToCat(cn);
            }
        });
        addTab(toc, ctx.getString(R.string.TOC));
        shown.add(ctx.getString(R.string.TOC));

        c_toc = new ListView(ctx);
        c_toc.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                String tn = (String) arg0.getItemAtPosition(arg2);
                switchToTopic(tn);
            }
        });
        t_cont = new TextView(ctx);
        t_cont.setMovementMethod(LinkMovementMethod.getInstance());
        t_cont.setLinksClickable(true);
        t_cont.setLineSpacing(0f, 1.5f);
        t_cont.setLinkTextColor(Color.parseColor(ctx.getString(R.color.link)));
    }

    private void switchToCat(String cn) {
        /* check if the category is already opened */
        if (shown.size() > 1 && shown.get(1) == cn) {
            Log.d(TAG, "category "+cn+" is already opened, switch to it");
            pageview.snapToScreen(1);
            return;
        }

        removeTabsStartingFrom(1);
        while (shown.size() > 2)
            shown.remove(1);

        c_toc.setAdapter(new ArrayAdapter<String>(ctx,
                android.R.layout.simple_list_item_1, topics.get(cn)));
        if (shown.size() == 1)
            addTab(c_toc, cn);
        else
            renameTabAt(1, cn);
        shown.add(cn);
        pageview.snapToScreen(1);
    }

    private void switchToTopic(String tn) {
        /* check if the topic is already opened */
        if (shown.size() == 3 && shown.get(2) == tn) {
            pageview.snapToScreen(2);
            return;
        }

        t_cont.setText(Html.fromHtml(t_views.get(tn)), BufferType.SPANNABLE);
        if (shown.size() <= 2)
            addTab(t_cont, tn);
        else
            renameTabAt(2, tn);
        if (shown.size() == 3)
            shown.remove(2);
        shown.add(tn);
        pageview.snapToScreen(2);
    }
}
