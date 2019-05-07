package com.hannah.memmem;
import android.util.Log;

import static com.hannah.memmem.IMemo.isDEBUG;

/**
 * 디버그 모드일때 로그를 찍는다. 릴리즈 할떄 false로 바꿔줘야함
 * @FileName   : aLog.java
 * @프로그램 설명 : 로그찍기 클래스
 */
public class aLog {


	public static void d(String tag, String message){
		if(isDEBUG) Log.d(tag,setMessage(message));
	}
	public static void d(String message){
		if(isDEBUG) Log.d(setTag(),setMessage(message));
	}
	public static void e(String tag, String message){
		if(isDEBUG) Log.e(tag,setMessage(message));
	}
	public static void e(String message){
		if(isDEBUG) Log.e(setTag(),setMessage(message));
	}
	public static void i(String tag, String message){
		if(isDEBUG) Log.i(tag,setMessage(message));
	}
	public static void i(String message){
		if(isDEBUG) Log.i(setTag(),setMessage(message));
	}
	public static void w(String tag, String message){
		if(isDEBUG) Log.w(tag,setMessage(message));
	}
	public static void w(String message){
		if(isDEBUG) Log.w(setTag(),setMessage(message));
	}
	private static String setMessage(String msg){
		StringBuilder msgBuilder = new StringBuilder();

		msgBuilder.append(" (").append(Thread.currentThread().getStackTrace()[4].getFileName()).append(":")
				.append(Thread.currentThread().getStackTrace()[4].getLineNumber()).append(")     ").append(msg);

		return msgBuilder.toString();
	}

	private static String setTag(){
		String strClassName = Thread.currentThread().getStackTrace()[4].getClassName().substring(Thread.currentThread().getStackTrace()[4].getClassName().lastIndexOf(".")+1);
		return strClassName;
	}
}
