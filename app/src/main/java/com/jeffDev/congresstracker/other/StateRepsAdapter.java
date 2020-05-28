package com.jeffDev.congresstracker.other;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jeffDev.congresstracker.R;
import com.jeffDev.congresstracker.models.CongressMember;

import java.util.ArrayList;

public class StateRepsAdapter extends BaseAdapter {
    private final Context mContext;
    private final ArrayList<CongressMember> members;

    public StateRepsAdapter(Context _context, ArrayList<CongressMember> _members){
        mContext = _context;
        members = _members;
    }

    @Override
    public int getCount() {

        if(members != null){
            return members.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(members != null && position >= 0 && position < members.size()){
            return  members.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StateRepsAdapter.ViewHolder vh;

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.state_reps_list_item,parent,false);
            vh= new StateRepsAdapter.ViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh = (StateRepsAdapter.ViewHolder) convertView.getTag();
        }


        if(members != null){
            vh.nameTV.setText(members.get(position).getName());
            vh.chamberTV.setText(members.get(position).getChamber());
            vh.partyTV.setText(members.get(position).getParty());

            Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
            Typeface regTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);


            if(members.get(position).getChamber().equals("Senate")){
                vh.nameTV.setTypeface(boldTypeface);
                vh.chamberTV.setTypeface(boldTypeface);
                vh.partyTV.setTypeface(boldTypeface);
//                ViewGroup.LayoutParams params = vh.listItem.getLayoutParams();
//                params.height = 200;
//                vh.listItem.requestLayout();
//                vh.nameTV.setTextSize(28);
//                vh.chamberTV.setTextSize(24);
                //vh.listItem.setBackgroundColor(ContextCompat.getColor(mContext,R.color.offWhite));
            }else {
                vh.nameTV.setTypeface(regTypeface);
                vh.chamberTV.setTypeface(regTypeface);
                vh.partyTV.setTypeface(regTypeface);
                //vh.listItem.setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
//                ViewGroup.LayoutParams params = vh.listItem.getLayoutParams();
//                params.height = 150;
//                vh.listItem.requestLayout();
//                vh.nameTV.setTextSize(24);
//                vh.chamberTV.setTextSize(18);
            }
        }
        return convertView;
    }

    static class ViewHolder{
        final TextView nameTV;
        final TextView chamberTV;
        final TextView partyTV;
        final View listItem;

        public ViewHolder(View _layout) {
            this.nameTV = _layout.findViewById(R.id.name_lbl);
            this.chamberTV = _layout.findViewById(R.id.chamber_lbl);
            this.partyTV = _layout.findViewById(R.id.local_party_lbl);
            this.listItem = _layout;
        }
    }
}
