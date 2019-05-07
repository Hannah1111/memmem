package com.hannah.memmem;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;

import java.util.ArrayList;

import static com.hannah.memmem.IMemo.IS_CHECK;
import static com.hannah.memmem.IMemo.IS_RADIO;
import static com.hannah.memmem.IMemo.PREF_NAME;
import static com.hannah.memmem.MemoWidget.updateAppWidget;
import static com.hannah.memmem.MemoWidgetSettingActivity.widgetid;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    Context ctx;
    private RecyclerView lvMemo;
    private ArrayList<MemoVo> dataList = new ArrayList<>();
    private MemoAdapter adapter;
    private LinearLayout llcheck;
    DBHelper dbHelper = null;
    Button btnCommit;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    enum BTN_TYPE{ IDLE, DELETE, WIDGET };

    BTN_TYPE btnType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aLog.e("########## onCreate #########");
        verifyStoragePermissions(this);
        ctx = this;
        sp = ctx.getSharedPreferences(PREF_NAME,MODE_MULTI_PROCESS);
        editor = sp.edit();
        dbHelper = DBHelper.getInstance(ctx);

        adapter = new MemoAdapter(ctx, null);
        lvMemo = (RecyclerView) findViewById(R.id.lvMemo);
        lvMemo.setAdapter(adapter);
        llcheck = (LinearLayout) findViewById(R.id.llcheck);
        lvMemo.setLayoutManager(new LinearLayoutManager(this));
        btnCommit = (Button) findViewById(R.id.btnCommit);
        btnCommit.setOnClickListener(this);
        dataList.clear();
        btnType = btnType.IDLE;
//        dataList = dbHelper.getData();
//
//        if(dataList!=null){
//            adapter.setData(dataList);
//        }



    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {

        if(dbHelper!=null){
            editor.putBoolean(IS_CHECK,false).commit();
            editor.putBoolean(IS_RADIO,false).commit();
            refreshList();
        }
        super.onResume();
    }


    //액션바 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,Menu.NONE,"추가");
        menu.add(0,1,Menu.NONE,"삭제");
        menu.add(0,2,Menu.NONE,"위젯설정");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()){
            case 0 :
                intent = new Intent(ctx, MemoDetailActivity.class);
                intent.putExtra("IDX",-1);
                ctx.startActivity(intent);
                break;

            case 1 :
                btnType = BTN_TYPE.DELETE;
                editor.putBoolean(IS_CHECK,true).commit();
                editor.putBoolean(IS_RADIO,false).commit();
                refreshList();
                btnCommit.setVisibility(View.VISIBLE);
                btnCommit.setText("삭제하기");
                break;

            case 2 :
                intent = new Intent(ctx, MemoDetailActivity.class);
                intent.putExtra("IDX",-1);
                ctx.startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCommit:
                SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sp.edit();
                if(btnType == BTN_TYPE.DELETE){
                    for(int i =0; i < lvMemo.getChildCount() ; i++){
                        CheckBox ch = (CheckBox) lvMemo.getChildAt(i).findViewById(R.id.chkBox);
                        if(ch.isChecked()){
                            aLog.e("CHECKBOX ::: "+ch.getTag());
                            int idx = (Integer) ch.getTag();
                            dbHelper.deleteData(idx);
                            ArrayList<Integer> arr = dbHelper.getWidgetId(idx);
                            if(arr !=null && arr.size()>0){
                                for(int widgetId : arr){
                                    dbHelper.deleteWidget(widgetId);
                                    updateAppWidget(ctx, AppWidgetManager.getInstance(ctx), widgetid);
                                }
                            }
                        }
                    }
                    editor.putBoolean(IS_CHECK,false).commit();
                    btnCommit.setVisibility(View.GONE);
                }else if(btnType == BTN_TYPE.WIDGET){

                }
                refreshList();
                break;
        }
    }

    public void refreshList(){
        dataList = dbHelper.getData();
        if(dataList != null){
            adapter.setData(dataList);
            adapter.notifyDataSetChanged();
            lvMemo.setAdapter(adapter);
        }
    }

    // Storage Permissions variables
    private static final int PERMISSION = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE
    };

    //persmission method.
    public void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int perm1 =
                ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_PHONE_STATE);
        int perm2 =
                ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_NETWORK_STATE);

        int perm3 =
                ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.CHANGE_NETWORK_STATE);

        if (perm1 != PackageManager.PERMISSION_GRANTED ||perm2 != PackageManager.PERMISSION_GRANTED ||
                perm3 != PackageManager.PERMISSION_GRANTED) {

            aLog.d("paulaner80", "퍼미션이 없잖아!");
            ActivityCompat.requestPermissions(activity,
                    PERMISSIONS_STORAGE, PERMISSION);
        } else {
            aLog.d("paulaner80", "퍼미션이 있습니다");
//            new FileOverWriter(ctx).execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // save file
                aLog.d("paulaner80", "퍼미션 허락 받았습니다");
            } else {
                aLog.d("paulaner80", "퍼미션 거부 받았습니다");
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

}
