package com.zxl.alipay.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;

import com.zxl.alipay.hook.WechatHook;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class HookMain implements IXposedHookLoadPackage {
    private static String k = new String();
    public static ClassLoader c = null;
    public static LoadPackageParam params = null;
    public static boolean t = true;
    public static String userId = "";
    private boolean loaded = false;
    public static boolean WECHAT_PACKAGE_ISHOOK = false;
    private static String WECHAT_PACKAGE = "com.tencent.mm";


    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // TODO Auto-generated method stub
        if (this.loaded) {
            XposedBridge.log("已经加载handleLoadPackage过了,无需再次加载");
            return;
        }
        this.loaded = true;
        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }


        final String packageName = lpparam.packageName;
        final String processName = lpparam.processName;
        if (WECHAT_PACKAGE.equals(packageName)) {
            try {
                XposedHelpers.findAndHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        final Context context = (Context) param.args[0];
                        final ClassLoader appClassLoader = context.getClassLoader();
                        if (WECHAT_PACKAGE.equals(processName) && !WECHAT_PACKAGE_ISHOOK) {
                            WECHAT_PACKAGE_ISHOOK = true;
                            Utils.context = context;
                            XposedBridge.log("handleLoadPackage: " + packageName);
                            XposedBridge.log("-----微信Hook成功-- " + packageName+Utils.getVerName(context));
                            Utils.writeLog("微信Hook成功，当前微信版本:" + Utils.getVerName(context));
                            //Hook微信
                            WechatHook.getInstance().hook(appClassLoader, context);
                            //启动websocket客户端
                            MWebSocket.getInstance().init();
                        }
                    }
                });
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

    }

    public void log(Object str) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        XposedBridge.log("[" + df.format(new Date()) + "]:  "
                + str.toString());
    }


}
