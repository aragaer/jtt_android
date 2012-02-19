package com.aragaer.jtt;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

public class JTTHelp extends ExpandableListView {
    private static final String TAG = JTTHelp.class.getSimpleName();
    private JTTTOCAdapter adapter;

    public JTTHelp(Context context) {
        super(context, null);
    }

    public JTTHelp(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public JTTHelp(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        adapter = new JTTTOCAdapter();
        setAdapter(adapter);
    }

    private final class JTTTOCAdapter extends BaseExpandableListAdapter {
        private ArrayList<String> categories = new ArrayList();
        private HashMap<String, ArrayList<String>> topics = new HashMap<String, ArrayList<String>>();

        public JTTTOCAdapter() {

        }

        public Object getChild(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getChildId(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
                ViewGroup arg4) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getChildrenCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Object getGroup(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getGroupCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long getGroupId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getGroupView(int arg0, boolean arg1, View arg2,
                ViewGroup arg3) {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }
    }

    private final class JTTCategoryAdapter extends BaseExpandableListAdapter {
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getChildrenCount(int groupPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getGroupCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
