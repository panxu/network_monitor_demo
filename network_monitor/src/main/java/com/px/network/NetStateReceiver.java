package com.px.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.px.utils.NetworkUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetStateReceiver";

    private Map<Object, List<MethodManager>> networkList = new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.e(TAG, "nothing receive");
            return;
        }
        if (Constants.ACTION_CONNECTIVITY_CHANGE.equalsIgnoreCase(intent.getAction())) {
            Log.e(TAG, "网络发生了改变");
            NetType netType = NetworkUtils.getNetType(context);
            post(netType);
        }
    }

    /**
     * 通知所有注册的方法，网络发生了改变
     *
     * @param netType
     */
    private void post(NetType netType) {
        Set<Object> sets = networkList.keySet();
        for (Object observer : sets) {
            List<MethodManager> methodList = networkList.get(observer);
            for (MethodManager method : methodList) {
                if (method.getType().isAssignableFrom(netType.getClass())) {
                    if (method.getNetType() == netType ||
                            netType == NetType.NONE ||
                            method.getNetType() == NetType.AUTO) {
                        invoke(method, observer, netType);
                    }
                }
            }
        }
    }

    private void invoke(MethodManager methodManager, Object observer, NetType netType) {
        try {
            Method execute = methodManager.getMethod();
            execute.invoke(observer, netType);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void registerObserver(Object observer) {
        List<MethodManager> methodList = networkList.get(observer);
        if (methodList == null) {
            methodList = getAnnotationMethod(observer);
            networkList.put(observer, methodList);
        }
    }

    private List<MethodManager> getAnnotationMethod(Object observer) {
        List<MethodManager> methodList = new ArrayList<>();
        Method[] methods = observer.getClass().getMethods();
        for (Method method : methods) {
            Network network = method.getAnnotation(Network.class);
            if (network == null) {
                continue;
            }
            Log.e(TAG, "NETWORK.....");
            //校验返回值
            Type returnType = method.getGenericReturnType();
            if (!"void".equals(returnType.toString())) {
                throw new RuntimeException(method.getName() + "return type should be null");
            }
            //校验参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException(method.getName() + "arguments should be one");
            }

            MethodManager methodManager = new MethodManager(parameterTypes[0],
                    network.netType(), method);
            methodList.add(methodManager);
        }
        return methodList;
    }

    public void unRegisterObserver(Object observer) {
        if (!networkList.isEmpty()) {
            networkList.remove(observer);
        }
    }

    public void unRegisterAllObserver() {
        if (!networkList.isEmpty()) {
            networkList.clear();
        }
        NetworkManager.getInstance().getApplication().unregisterReceiver(this);
        networkList = null;
    }
}
