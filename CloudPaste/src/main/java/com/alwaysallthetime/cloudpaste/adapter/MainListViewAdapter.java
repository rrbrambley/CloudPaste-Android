package com.alwaysallthetime.cloudpaste.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alwaysallthetime.adnlibutils.MessagePlus;
import com.alwaysallthetime.cloudpaste.R;

import java.util.List;

public class MainListViewAdapter extends ArrayAdapter<MessagePlus> {
    private List<MessagePlus> mMessages;

    public MainListViewAdapter(Context context, List<MessagePlus> messages) {
        super(context, -1, messages);
        mMessages = messages;
    }

    public void appendAndRefresh(List<MessagePlus> items) {
        mMessages.addAll(items);
        notifyDataSetChanged();
    }

    public void prependAndRefresh(List<MessagePlus> items) {
        mMessages.addAll(0, items);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessagePlus item = getItem(position);
        if(convertView == null) {
            convertView = new MainListViewAdapterLayout(getContext());
        }
        ((MainListViewAdapterLayout)convertView).setItem(item);
        return convertView;
    }

    private class MainListViewAdapterLayout extends LinearLayout {

        TextView mTextView;

        public MainListViewAdapterLayout(Context context) {
            super(context);

            LayoutInflater.from(context).inflate(R.layout.adapter_main_list, this, true);
            mTextView = (TextView) findViewById(R.id.MainListAdapterTextView);
        }

        public void setItem(MessagePlus message) {
            mTextView.setText(message.getMessage().getText());
        }
    }

}
