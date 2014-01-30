package com.alwaysallthetime.cloudpaste.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alwaysallthetime.cloudpaste.R;
import com.alwaysallthetime.messagebeast.model.MessagePlus;

import java.util.List;

public class MainListViewAdapter extends ArrayAdapter<MessagePlus> {
    private List<MessagePlus> mMessages;

    public MainListViewAdapter(Context context, List<MessagePlus> messages) {
        super(context, -1, messages);
        mMessages = messages;
    }

    public void refresh(List<MessagePlus> items) {
        mMessages.clear();
        mMessages.addAll(items);
        notifyDataSetChanged();
    }

    public void removeItemAt(int position) {
        mMessages.remove(position);
        notifyDataSetChanged();;
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

    public class MainListViewAdapterLayout extends LinearLayout {

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
