package com.zxl.alipay.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.zxl.alipay.hook.Login;
import com.zxl.alipay.hook.PaymentAssistant;
import com.zxl.alipay.hook.Reddot;
import com.zxl.alipay.hook.Redpacket;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * ## hook 主要包括三大功能点 aa（支付宝收款） 个人收款（Personalreceipt），红包（Redpacket）
 *
 * ## 其中还包括： 支付宝启动界面的hook,支付收款通知的hook，去红点的hook
 *
 * 当前功能主要仅使用 红包的hook
 *
 */

public class HookMain implements IXposedHookLoadPackage {
	private static String k = new String();
	public static ClassLoader c = null;
	public static LoadPackageParam params = null;
	public static boolean t = true;
	public static String userId="";
	private boolean loaded = false;
	public static boolean ALIPAY_PACKAGE_ISHOOK = false;
	private static String ALIPAY_PACKAGE = "com.eg.android.AlipayGphone";
	

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
        if(ALIPAY_PACKAGE.equals(packageName) && ALIPAY_PACKAGE.equals(processName)){
        	XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
    		    @Override
    		    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    		    	
    		    		final Context context = (Context) param.args[0];
    			    	final ClassLoader cl = ((Context) param.args[0]).getClassLoader();
    			    	if(ALIPAY_PACKAGE.equals(processName) && !ALIPAY_PACKAGE_ISHOOK){
	    					try {
	    						//登录
	    						synchronized (this) {
	    							if(ALIPAY_PACKAGE_ISHOOK == false) {
	    								ALIPAY_PACKAGE_ISHOOK = true;
										Utils.context = context;
	    								MWebSocket.getInstance().init();
	    								Login.hook(cl, context);
	    								//消息列表aa收款
	    								//Messagemanipulation.hook(cl, context,true,true,true);
	    								//红包+心跳
	    								Redpacket.hook(cl, context);
	    								//去红点
	    								Reddot.hook(cl, context);
	    								//个人收款
	    								//Personalreceipt.hook(cl, context);
	    								PaymentAssistant.hook(cl, context);
	    							}
	    						
	    						}

	    						
	    					} catch (Exception e) {
	    						// TODO: handle exception
    					}
    				}
    		    	
    		    }
    		});
        }
		
	}	

	 public void log(Object str) {
	            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
	            XposedBridge.log("[" + df.format(new Date()) + "]:  "
	                    + str.toString());  
	        }  
	          

	 
	
	

}
