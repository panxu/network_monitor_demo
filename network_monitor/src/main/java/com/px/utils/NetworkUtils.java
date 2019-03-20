package com.px.utils;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.px.network.NetType;

public class NetworkUtils {
    /**
     * 判断当前网络状态是否可用
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connMgr == null){
            return false;
        }
        NetworkInfo[] info = connMgr.getAllNetworkInfo();
        for(NetworkInfo info0 : info){
            if(info0.getState() == NetworkInfo.State.CONNECTED){
                return true;
            }
        }
        return false;
    }

    public static NetType getNetType(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connMgr == null){
            return NetType.NONE;
        }
        //获取当前激活的网络连接状态
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo == null){
            return NetType.NONE;
        }

        int nType = networkInfo.getType();
        if(nType == ConnectivityManager.TYPE_MOBILE){
            return NetType.MOBILE;
        }else if(nType == ConnectivityManager.TYPE_WIFI){
            return NetType.WIFI;
        }
        return NetType.NONE;
    }

    /**
     * 打开网络设置页面
     */
    public static void openSetting(Activity context,int requestCode){
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.View");
        context.startActivityForResult(intent,requestCode);
    }
}
