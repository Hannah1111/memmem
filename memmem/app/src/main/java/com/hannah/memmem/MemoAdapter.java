package com.hannah.memmem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Context.MODE_MULTI_PROCESS;
import static com.hannah.memmem.IMemo.IS_CHECK;
import static com.hannah.memmem.IMemo.IS_RADIO;
import static com.hannah.memmem.IMemo.PREF_NAME;
import static com.hannah.memmem.Utils.getLongToString;

/**
 * Created by hannah on 2017. 11. 3..
 */
public class MemoAdapter extends RecyclerView.Adapter {

    private ArrayList<MemoVo> items = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;


    public MemoAdapter(Context context, ArrayList<MemoVo> items) {
        setData(items);

        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<MemoVo> items) {
        if (items == null)
            items = new ArrayList<>();
        if (items != null) {
            this.items.clear();
            this.items.addAll(items);
        }

        notifyDataSetChanged();
    }


    public ArrayList<MemoVo> getData() {
        return items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MemoHolder(mInflater.inflate(R.layout.cell_memo_list, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MemoVo data = items.get(position);
        ((MemoHolder) holder).onBind(data);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MemoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        MemoVo data;
        public TextView tvIdx, tvTitle, tvDate;
        public CheckBox chkBox;
        public RadioButton rBox;
        public LinearLayout llview;


        public MemoHolder(View itemView) {
            super(itemView);
            llview = (LinearLayout) itemView.findViewById(R.id.llview);
            tvIdx = (TextView) itemView.findViewById(R.id.tvIdx);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            llview.setOnClickListener(this);
            chkBox = (CheckBox) itemView.findViewById(R.id.chkBox);
            rBox = (RadioButton) itemView.findViewById(R.id.rBox);
            rBox.setOnClickListener(this);
        }

        void onBind(MemoVo data) {
            this.data = data;
            tvIdx.setText(data.getIDX()+"");
            tvTitle.setText(data.getTitle());
            tvDate.setText(getLongToString(data.getUpdateDate(),0));
            chkBox.setTag(data.getIDX());
            rBox.setTag(data.getIDX());
            SharedPreferences sp = context.getSharedPreferences(PREF_NAME,MODE_MULTI_PROCESS);
            if(sp.getBoolean(IS_CHECK,false)){
                chkBox.setVisibility(View.VISIBLE);
            }else{
                chkBox.setVisibility(View.GONE);
            }
            if(sp.getBoolean(IS_RADIO,false)){
                rBox.setVisibility(View.VISIBLE);
            }else{
                rBox.setVisibility(View.GONE);
            }
        }


        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.llview){
                Intent intent = new Intent(context, MemoDetailActivity.class);
                intent.putExtra("IDX", data.getIDX());
                context.startActivity(intent);
            }else if(v.getId() == R.id.rBox){
                aLog.e("=======onclick "+v.getTag());

            }
        }
    }


}
