package com.hannah.memmem;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class MemoWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        aLog.e("############ updateAppWidget appWidgetId : "+appWidgetId);
        DBHelper dbHelper = DBHelper.getInstance(context);
        int idx = dbHelper.getWidgetKey(appWidgetId);
        MemoVo memoVo = dbHelper.getData(idx);
        String text = "";
        Intent intent = null;
        if(memoVo!=null){
            text = memoVo.getMsg();
            intent = new Intent(context,MemoDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("IDX",idx);
        }else{
            intent = new Intent(context,MemoWidgetSettingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("widgetid",appWidgetId);
        }
        aLog.e("############ updateAppWidget text : "+text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.memo_widget);
        views.setTextViewText(R.id.appwidget_text, text);
        PendingIntent pe = PendingIntent.getActivity(context, appWidgetId , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.llWidget, pe);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        aLog.e("############ onReceive : "+intent.getAction());

        DBHelper dbHelper = DBHelper.getInstance(context);
        String action = intent.getAction();
        if(intent.getExtras()!=null){
            aLog.e("############ onReceive : "+intent.getAction() +" : "+intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
        }
        if(AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)){
            if(intent.getExtras()!=null){
                int widgetid = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                dbHelper.deleteWidget(widgetid);
                updateAppWidget(context, AppWidgetManager.getInstance(context), widgetid);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        aLog.e("############ updateAppWidget : "+appWidgetIds.length);
        for (int appWidgetId : appWidgetIds) {
            //위젯 아이디를 가져옵니다.
            //int widgetId = appWidgetId;
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        aLog.e("############ onEnabled : "+context);
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

