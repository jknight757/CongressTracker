package com.example.congresstracker.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.congresstracker.R;

import java.util.ArrayList;

public class VoteAdapter extends BaseAdapter {

    private final Context mContext;
    private ArrayList<BillVote> memberVotes;

    public VoteAdapter(Context _context, ArrayList<BillVote> _memberVotes) {
        this.mContext = _context;
        this.memberVotes = _memberVotes;
    }

    @Override
    public int getCount() {

        if(memberVotes != null){
            return memberVotes.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(memberVotes != null && position >=0 && position < memberVotes.size()){
            return memberVotes.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vote_history_item, parent,false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }


        if(memberVotes != null){

            vh.billIdTV.setText(memberVotes.get(position).getId());
            vh.billDateTV.setText(memberVotes.get(position).getDate());
            String title = "";
            if(memberVotes.get(position).getTitle().equals("null")){
                title = memberVotes.get(position).getDescription();
            }else {
                title = memberVotes.get(position).getTitle();
            }
            vh.billTitleTV.setText(title);

            String result = "Result: " + memberVotes.get(position).getResult();

            vh.billResultTV.setText(result);

            String votePosition = "Postion: " + memberVotes.get(position).getPosition();
            vh.memberPositionTV.setText(votePosition);

        }




        return convertView;
    }

    static class ViewHolder{
        final TextView billIdTV;
        final TextView billDateTV;
        final TextView billTitleTV;
        final TextView billResultTV;
        final TextView memberPositionTV;

        public ViewHolder(View _layout) {
            this.billIdTV = _layout.findViewById(R.id.bill_id);
            this.billDateTV = _layout.findViewById(R.id.bill_date);
            this.billTitleTV = _layout.findViewById(R.id.bill_title);
            this.billResultTV = _layout.findViewById(R.id.bill_result);
            this.memberPositionTV = _layout.findViewById(R.id.member_position);
        }
    }
}
