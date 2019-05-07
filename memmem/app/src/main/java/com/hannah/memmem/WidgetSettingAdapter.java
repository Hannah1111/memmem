package com.hannah.memmem;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.hannah.memmem.MemoWidgetSettingActivity.widgetid;
import static com.hannah.memmem.Utils.getLongToString;

/**
 * Created by hannah on 2017. 11. 3..
 */
public class WidgetSettingAdapter extends RecyclerView.Adapter {

    private ArrayList<MemoVo> items = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;


    public WidgetSettingAdapter(Context context, ArrayList<MemoVo> items) {
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
        return new MemoHolder(mInflater.inflate(R.layout.cell_widgetset_list, parent, false));
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
        public LinearLayout llview;


        public MemoHolder(View itemView) {
            super(itemView);

            llview = (LinearLayout) itemView.findViewById(R.id.llview);
            llview.setOnClickListener(this);
            tvIdx = (TextView) itemView.findViewById(R.id.tvIdx);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
        }

        void onBind(MemoVo data) {
            this.data = data;
            llview.setTag(data.getIDX());
            tvIdx.setText(data.getIDX()+"");
            tvTitle.setText(data.getTitle());
            tvDate.setText(getLongToString(data.getUpdateDate(),0));
        }


        @Override
        public void onClick(View v) {

            aLog.e("===click "+v.getId()+" : "+v.getTag() +" : "+ widgetid);
            DBHelper dbHelper = DBHelper.getInstance(context);
            MemoVo mem = dbHelper.getData((int) v.getTag());
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.memo_widget);
            views.setTextViewText(R.id.appwidget_text, mem.getMsg());
            ArrayList<Integer> arr = dbHelper.getWidgetId((int)v.getTag());
            if(arr!=null && arr.size()>0){
                for(int a : arr){
                    dbHelper.updateWidget(a,(int)v.getTag());
                }
            }else{
                dbHelper.insertWidget(widgetid,(int)v.getTag());
            }

            AppWidgetManager wm = AppWidgetManager.getInstance(context);
//            ComponentName widget = new ComponentName(context, MemoWidget.class);
            wm.updateAppWidget(widgetid,views);
            //wm.updateAppWidget(widget, views);
            Toast.makeText(context,"위젯설정이 완료되었습니다.",Toast.LENGTH_SHORT).show();
            ((Activity)context).finish();
        }
    }


}
