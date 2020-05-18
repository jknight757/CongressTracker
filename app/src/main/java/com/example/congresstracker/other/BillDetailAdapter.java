package com.example.congresstracker.other;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.congresstracker.R;
import com.google.android.material.resources.TextAppearance;

import java.util.ArrayList;

public class BillDetailAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> mDetails;

    public BillDetailAdapter(Context _context, ArrayList<String> details) {
        mContext = _context;
        mDetails = details;
    }

    @Override
    public int getCount() {
        if(mDetails != null){
            return mDetails.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {

        if(mDetails != null && position >=0 && position < mDetails.size()){
            return mDetails.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BillDetailAdapter.ViewHolder vh;

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bill_detail_list_item, parent,false);
            vh = new BillDetailAdapter.ViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh = (BillDetailAdapter.ViewHolder) convertView.getTag();
        }

        if(mDetails != null){
            String text = mDetails.get(position);
            vh.detailText.setText(text);
            if(position == 0){
                vh.detailText.setTextColor(mContext.getResources().getColor(R.color.hyperLinkBlue));
                vh.detailText.setTypeface(Typeface.DEFAULT_BOLD);
            }else{
                vh.detailText.setTextColor(mContext.getResources().getColor(R.color.textColor));
                vh.detailText.setTypeface(Typeface.DEFAULT);
            }
        }



        return convertView;
    }

    static class ViewHolder{

        final TextView detailText;
        public ViewHolder(View _layout) {
            this.detailText = _layout.findViewById(R.id.detail_text);

        }
    }
}
