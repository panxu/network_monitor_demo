package com.px.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
    private static final String TAG = "NetworkCallbackImpl";
    private Map<Object, List<MethodManager>> networkList = new HashMap<>();

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        Log.e(TAG,"网络连接了");
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        post(NetType.NONE);
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        if(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)){
            if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                post(NetType.WIFI);
            }else {
                post(NetType.MOBILE);
            }
        }
    }

    /**
     * 通知所有注册的方法，网络发生了改变
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

    /**
     * 注册监听
     * @param observer
     */
    public void registerObserver(Object observer) {
        List<MethodManager> methodList = networkList.get(observer);
        if (methodList == null) {
            methodList = getAnnotationMethod(observer);
            networkList.put(observer, methodList);
        }
    }

    /**
     * 遍历注册类中的所有方法，收集被注解方法的信息
     * @param observer
     * @return
     */
    private List<MethodManager> getAnnotationMethod(Object observer) {
        List<MethodManager> methodList = new ArrayList<>();
        Method[] methods = observer.getClass().getMethods();
        for (Method method : methods) {
            com.px.network.Network network = method.getAnnotation(com.px.network.Network.class);
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
        NetworkManager.getInstance().getConnectivityManager().unregisterNetworkCallback(this);
        networkList = null;
    }
}
