package com.px.network;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;

public class NetworkManager {

    private Application application;
    private NetStateReceiver receiver;
    private ConnectivityManager cmgr;
    private NetworkCallbackImpl networkCallback;

    private NetworkManager() {
        receiver = new NetStateReceiver();
    }

    public static NetworkManager getInstance() {
        return SingletonHolder.instance;
    }

    public void init(Application application) {
        this.application = application;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_CONNECTIVITY_CHANGE);
            application.registerReceiver(receiver, filter);
        } else {
            networkCallback = new NetworkCallbackImpl();
            NetworkRequest request = new NetworkRequest.Builder().build();
            cmgr = (ConnectivityManager) application
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cmgr != null) {
                cmgr.registerNetworkCallback(request,networkCallback);
            }
        }
    }

    public Application getApplication() {
        return application;
    }

    public ConnectivityManager getConnectivityManager(){
        return cmgr;
    }

    public void registerObserver(Object object) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            receiver.registerObserver(object);
        } else {
            networkCallback.registerObserver(object);
        }
    }

    public void unRegisterObserver(Object object) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            receiver.unRegisterObserver(object);
        }else{
            networkCallback.unRegisterObserver(object);
        }

    }

    public void unRegisterAllObserver() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            receiver.unRegisterAllObserver();
        }else {
            networkCallback.unRegisterAllObserver();
        }
    }

    private static class SingletonHolder {
        private static NetworkManager instance = new NetworkManager();
    }
}
