package com.hannah.memmem;

import static com.hannah.memmem.Utils.getLongToString;

/**
 * Created by hannah on 2017. 11. 3..
 */

public class MemoVo {

    private int IDX = 0;
    private String Title = "";
    private String msg = "";
    private long UpdateDate = 0;
    private long InsertDate = 0;

    private String allstring = "";

    public MemoVo() {

    }

    public MemoVo(int idx, String title, String message, long insertDate,long updateDate) {
        IDX = idx;
        Title = title;
        msg = message;
        InsertDate = insertDate;
        UpdateDate = updateDate;

//        aLog.e("====MEMO VO===="+idx+" : "+Title+":"+msg);
        allstring = idx+" : "+Title+" : "+msg+ " : "+getLongToString(insertDate,0)+ " : "+getLongToString(updateDate,0);
    }

    public int getIDX() {
        return IDX;
    }

    public String getTitle(){
        return Title;
    }

    public String getMsg(){
        return msg;
    }

    public long getInsertDate(){
        return InsertDate;
    }

    public long getUpdateDate(){
        return UpdateDate;
    }

    @Override
    public String toString(){
        return  allstring;
    }

}
