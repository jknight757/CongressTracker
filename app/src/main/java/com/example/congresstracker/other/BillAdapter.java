package com.example.congresstracker.other;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.congresstracker.R;
import com.example.congresstracker.models.Bill;

import java.util.ArrayList;

public class BillAdapter extends BaseAdapter {

    private final Context mContext;
    private ArrayList<Bill> bills;

    public BillAdapter(Context _context, ArrayList<Bill> bills) {
        this.mContext = _context;
        this.bills = bills;
        Log.i("TAG", "BillAdapter: started");
    }

    @Override
    public int getCount() {

        if(bills != null){
            return bills.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(bills != null && position >=0 && position < bills.size()){
            return bills.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BillAdapter.ViewHolder vh;

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bill_list_item, parent,false);
            vh = new BillAdapter.ViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh = (BillAdapter.ViewHolder) convertView.getTag();
        }


        if(bills != null){

            vh.billIdTV.setText(bills.get(position).getBillNum());
            vh.billDateTV.setText(bills.get(position).getDateIntroduced());
            String title = "";

            title = bills.get(position).getShortTitle();

            vh.billTitleTV.setText(title);

            String result = "Active: " + bills.get(position).isActive();

            vh.billResultTV.setText(result);


        }

        return convertView;
    }

    static class ViewHolder{
        final TextView billIdTV;
        final TextView billDateTV;
        final TextView billTitleTV;
        final TextView billResultTV;

        public ViewHolder(View _layout) {
            this.billIdTV = _layout.findViewById(R.id.bill_id);
            this.billDateTV = _layout.findViewById(R.id.bill_date);
            this.billTitleTV = _layout.findViewById(R.id.bill_title);
            this.billResultTV = _layout.findViewById(R.id.bill_result);
        }
    }
}
