package com.hannah.memmem;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import static com.hannah.memmem.MemoWidget.updateAppWidget;
import static com.hannah.memmem.MemoWidgetSettingActivity.widgetid;
import static com.hannah.memmem.Utils.getLongToString;
import static com.hannah.memmem.Utils.getMobileDataState;

public class MemoDetailActivity extends AppCompatActivity implements View.OnClickListener {


    EditText etMemo,etTitle;
    Context ctx;
    int index = -1;
    DBHelper dbHelper = null;
    MemoVo memoVo  = null;
    TextView tvIdx,tvDate;
    Button btnUpdate,btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        aLog.e("########## onCreate #########");
        ctx = this;
        dbHelper = DBHelper.getInstance(ctx);

        etMemo = (EditText) findViewById(R.id.etMemo);


        tvIdx = (TextView) findViewById(R.id.tvIdx);
        etTitle = (EditText) findViewById(R.id.etTitle);
        tvDate = (TextView) findViewById(R.id.tvDate);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        Intent i = getIntent();
        if(i != null){
            index = i.getIntExtra("IDX",-1);
            if(index > -1){
                memoVo = dbHelper.getData(index);
                if(memoVo != null){
                    tvIdx.setText(memoVo.getIDX()+"");
                    etTitle.setText(memoVo.getTitle());
                    etTitle.setSelection(etTitle.getText().length());
                    tvDate.setText(getLongToString(memoVo.getUpdateDate(),1));
                    etMemo.setText(memoVo.getMsg());
                    etMemo.setSelection(etMemo.getText().length());
                }
            }
        }
        aLog.e("############ getMobileDataState : " + getMobileDataState(this));
    }


    @Override
    protected void onPause() {
        super.onPause();
        /**
         * 현재 텍스트를 저장
         */
        aLog.e("############ onPause : " + String.valueOf(etMemo.getText())+ " : "+index);
        saveText();


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnUpdate://수정
                saveText();
                finish();
                break;

            case R.id.btnDelete: //삭제
                if(index > -1){
                    dbHelper.deleteData(index);
                    ArrayList<Integer> arr = dbHelper.getWidgetId(index);
                    if(arr !=null && arr.size()>0){
                        for(int widgetId : arr){
                            dbHelper.deleteWidget(widgetId);
                            updateAppWidget(ctx, AppWidgetManager.getInstance(ctx), widgetid);
                        }
                    }
                }
                finish();
                break;

        }
    }


    private void saveText(){

        if(etTitle.getText().toString().trim().equals("") &&etMemo.getText().toString().trim().equals("")){
            return;
        }

        if(index == -1){
            dbHelper.insertData(etTitle.getText().toString(),etMemo.getText().toString());
            index = dbHelper.getData(etTitle.getText().toString());
        }else{
            dbHelper.updateData(index,etTitle.getText().toString(),etMemo.getText().toString());
            ArrayList<Integer> arr = dbHelper.getWidgetId(index);
            if(arr !=null && arr.size()>0){
                for(int widgetId : arr){
                    updateAppWidget(ctx, AppWidgetManager.getInstance(ctx), widgetid);
                }
            }

        }
        aLog.e("############ saveText : " + index);
    }
}
