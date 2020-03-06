package com.example.congresstracker.other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.congresstracker.R;
import com.example.congresstracker.models.CongressMember;

import java.util.ArrayList;

public class MemberAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<CongressMember> members;

    public MemberAdapter(Context _context, ArrayList<CongressMember> _members){
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
        ViewHolder vh;

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.member_list_item,parent,false);
            vh= new ViewHolder(convertView);
            convertView.setTag(vh);
        }else {
           vh = (ViewHolder) convertView.getTag();
        }


        if(members != null){
            vh.nameTV.setText(members.get(position).getName());
            vh.partyTV.setText(members.get(position).getParty());
            vh.stateTV.setText(members.get(position).getState());
        }
        return convertView;
    }

    static class ViewHolder{
        final TextView nameTV;
        final TextView partyTV;
        final TextView stateTV;

        public ViewHolder(View _layout) {
            this.nameTV = _layout.findViewById(R.id.name_lbl);
            this.partyTV = _layout.findViewById(R.id.party_lbl);
            this.stateTV = _layout.findViewById(R.id.state_lbl);
        }
    }
}
