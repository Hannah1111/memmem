package com.hannah.memmem;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by enuri_lab_036 on 2017. 4. 17..
 */

public class Utils {


    public static String getMyIP(){
        try {

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();

                //네트워크 중에서 IP가 할당된 넘들에 대해서 뺑뺑이를 한 번 더 돕니다.
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {

                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress()){
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        aLog.e("***** IP="+ ip);
                        if(inetAddress instanceof Inet6Address){
                            aLog.e(intf.getDisplayName()+" : "+inetAddress.getHostAddress().toString());
                        }
                        if(inetAddress instanceof Inet4Address){
                            aLog.e(intf.getDisplayName()+ " : "+inetAddress.getHostAddress().toString());
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void changeWifiConfiguration(Context ctx, boolean dhcp, String ip, int prefix, String dns1, String gateway) {
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if(!wm.isWifiEnabled()) {
            // wifi is disabled
            return;
        }
        // get the current wifi configuration
        WifiConfiguration wifiConf = null;
        WifiInfo connectionInfo = wm.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wm.getConfiguredNetworks();
        if(configuredNetworks != null) {
            for (WifiConfiguration conf : configuredNetworks){
                if (conf.networkId == connectionInfo.getNetworkId()){
                    wifiConf = conf;
                    break;
                }
            }
        }
        if(wifiConf == null) {
            // wifi is not connected
            return;
        }
        try {
            Class<?> ipAssignment = wifiConf.getClass().getMethod("getIpAssignment").invoke(wifiConf).getClass();
            Object staticConf = wifiConf.getClass().getMethod("getStaticIpConfiguration").invoke(wifiConf);
            if(dhcp) {
                wifiConf.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConf, Enum.valueOf((Class<Enum>) ipAssignment, "DHCP"));
                if(staticConf != null) {
                    staticConf.getClass().getMethod("clear").invoke(staticConf);
                }
            } else {
                wifiConf.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConf, Enum.valueOf((Class<Enum>) ipAssignment, "STATIC"));
                if(staticConf == null) {
                    Class<?> staticConfigClass = Class.forName("android.net.StaticIpConfiguration");
                    staticConf = staticConfigClass.newInstance();
                }
                // STATIC IP AND MASK PREFIX
                Constructor<?> laConstructor = LinkAddress.class.getConstructor(InetAddress.class, int.class);
                LinkAddress linkAddress = (LinkAddress) laConstructor.newInstance(
                        InetAddress.getByName(ip),
                        prefix);
                staticConf.getClass().getField("ipAddress").set(staticConf, linkAddress);
                // GATEWAY
                staticConf.getClass().getField("gateway").set(staticConf, InetAddress.getByName(gateway));
                // DNS
                List<InetAddress> dnsServers = (List<InetAddress>) staticConf.getClass().getField("dnsServers").get(staticConf);
                dnsServers.clear();
                dnsServers.add(InetAddress.getByName(dns1));
                dnsServers.add(InetAddress.getByName("8.8.8.8")); // Google DNS as DNS2 for safety
                // apply the new static configuration
                wifiConf.getClass().getMethod("setStaticIpConfiguration", staticConf.getClass()).invoke(wifiConf, staticConf);
            }
            // apply the configuration change
            boolean result = wm.updateNetwork(wifiConf) != -1; //apply the setting
            if(result) result = wm.saveConfiguration(); //Save it
            if(result) wm.reassociate(); // reconnect with the new static IP
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    public static WifiConfiguration getCurrentWiFiConfiguration(Context context) {
        WifiConfiguration wifiConf = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                if(configuredNetworks != null){
                    for (WifiConfiguration conf : configuredNetworks) {
                        if (conf.networkId == connectionInfo.getNetworkId()) {
                            wifiConf = conf;
                            break;
                        }
                    }
                }
            }
        }
        return wifiConf;
    }


    static public void setMobileDataState(Context ctx,boolean mobileDataEnabled)
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            aLog.e("Error setting mobile data state" + ex.getMessage());
        }
    }

    static public boolean getMobileDataState(Context ctx)
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            if (null != getMobileDataEnabledMethod)
            {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);

                return mobileDataEnabled;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            aLog.e("Error getting mobile data state"+ex.getMessage());
        }

        return false;
    }


    static public void disconnectDATA(Context ctx){
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            Class localClass = connectivityManager.getClass();
            Class[] arrayClass = new Class[1];
            arrayClass[0] = Boolean.TYPE;
            Method localMethod = localClass.getMethod("setMobileDataEnabled",arrayClass);
            ConnectivityManager localManage = connectivityManager;
            Object[] arrayO = new Object[1];
            arrayO[0] = Boolean.valueOf(false);
            localMethod.invoke(localManage,arrayO);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static public String getLongToString(long now,int type){
        Date date = new Date(now);
        String result = "";
        SimpleDateFormat sdf;
        if(type == 1){
            sdf = new SimpleDateFormat("yyyy/MM/dd");
        }else{
            sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
        result = sdf.format(date);
        return result;
    }

    public static String encodeStr(String src){
        String encStr = "";
        try {
            encStr = Base64.encodeToString(src.getBytes("UTF-8"), Base64.DEFAULT); //B64인코딩
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        aLog.e("## encodeStr ##  "+encStr);
        return encStr;
    }

    public static String decodeStr(String src){
        String decStr = "";
        try {
            byte decoded[] = Base64.decode(src.getBytes(),Base64.DEFAULT);
            decStr = new String(decoded, "UTF-8"); //B64디코딩
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        aLog.e("## decodeStr ##  "+decStr);
        return decStr;
    }

/*
    public static void setMobileNetworkfromLollipop(Context context) throws Exception {
        String command = null;
        int state = 0;
        try {
            // Get the current state of the mobile network.
            state = isMobileDataEnabledFromLollipop(context) ? 0 : 1;
            aLog.e("isMobileDataEnabledFromLollipop : "+state);
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            String transactionCode = getTransactionCode(context);
            aLog.e("transactionCode : "+transactionCode);
            // Android 5.1+ (API 22) and later.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                // Loop through the subscription list i.e. SIM list.
                for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                    aLog.e("mSubscriptionManager.getActiveSubscriptionInfoCountMax() : "+mSubscriptionManager.getActiveSubscriptionInfoCountMax());
                    if (transactionCode != null && transactionCode.length() > 0) {
                        // Get the active subscription ID for a given SIM card.
                        int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                        // Execute the command via `su` to turn off
                        // mobile network for a subscription service.
                        command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                        executeCommandViaSu(context, "-c", command);
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0 (API 21) only.
                if (transactionCode != null && transactionCode.length() > 0) {
                    // Execute the command via `su` to turn off mobile network.
                    command = "service call phone " + transactionCode + " i32 " + state;
                    executeCommandViaSu(context, "-c", command);
                }
            }
        } catch(Exception e) {
            // Oops! Something went wrong, so we throw the exception here.
            throw e;
        }
    }

//    To check if the mobile network is enabled or not:

    private static boolean isMobileDataEnabledFromLollipop(Context context) {
        boolean state = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            state = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        return state;
    }

//    To get the value of the TRANSACTION_setDataEnabled field:

    private static String getTransactionCode(Context context) throws Exception {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            throw e;
        }
    }

//    To execute command via su:

    private static void executeCommandViaSu(Context context, String option, String command) {
        aLog.e("executeCommandViaSu : "+command);
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            aLog.e("su : "+su);
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
            } finally {
                success = true;
            }
        }
    }
*/
}
