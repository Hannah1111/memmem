package com.hannah.memmem;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MemoWidgetSettingActivity extends AppCompatActivity {


    Context ctx;
    private RecyclerView lvMemo;
    private ArrayList<MemoVo> dataList = new ArrayList<>();
    private WidgetSettingAdapter adapter;
    private Button btnGo;
    DBHelper dbHelper = null;
    static int widgetid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_setting);
        aLog.e("########## onCreate #########");
        ctx = this;
        dbHelper = DBHelper.getInstance(ctx);
        adapter = new WidgetSettingAdapter(ctx, null);
        lvMemo = (RecyclerView) findViewById(R.id.lvMemo);
        lvMemo.setAdapter(adapter);
        lvMemo.setLayoutManager(new LinearLayoutManager(this));

        btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MemoWidgetSettingActivity.this, MainActivity.class));
            }
        });

        dataList.clear();

        Intent i = getIntent();
        if(i != null){
            widgetid = i.getIntExtra("widgetid",-1);
            aLog.e("########## widgetid #########"+widgetid);
        }

    }

    @Override
    protected void onDestroy() {

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetid);
        setResult(RESULT_OK, resultValue);
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
        aLog.e("############ onPause : ");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Intent i = getIntent();
        if(i != null){
            widgetid = i.getIntExtra("widgetid",-1);
            aLog.e("########## onNewIntent widgetid #########"+widgetid);
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {

        if(dbHelper!=null){
            dataList = dbHelper.getData();
            if(dataList != null){
                adapter.setData(dataList);
                adapter.notifyDataSetChanged();
                lvMemo.setAdapter(adapter);
            }
        }
        super.onResume();
    }

    public void getWidget(View view) {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetid);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
