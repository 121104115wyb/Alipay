package com.zxl.alipay.hook;

import android.content.Context;
import android.content.Intent;

import com.zxl.alipay.utils.HookMain;
import com.zxl.alipay.utils.MWebSocket;
import com.zxl.alipay.utils.Utils;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Login {
	private static Context context;
	private static boolean t = false;

	public static void hook(ClassLoader cl, Context context) throws ClassNotFoundException {
		Class AlipayUtils = cl.loadClass("com.alipay.android.phone.o2o.o2ocommon.util.AlipayUtils");
		findMethod(AlipayUtils, context);
	}

	private static void findMethod(Class hookclass, Context c) {
		if (!hookclass.isInterface()) {
			for (Method method : hookclass.getDeclaredMethods()) {

				if (!Modifier.isAbstract(method.getModifiers())) {
					context = c;
					findParamFormMethod(method, hookclass.getName(), hookclass, context);

				}
			}
		}

	}

	private static void findParamFormMethod(final Method method, String name, final Class cc, final Context context) {
		// TODO Auto-generated method stub
		XposedBridge.hookMethod(method, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// TODO Auto-generated method stub
				if (method.getName().equals("getUserInfo") && t == false) {
					t = true;
					Object userinfo = param.getResult();
					if (userinfo != null) {
						String Loginid = "";
						String realName = "";
						String nickName = "";
						nickName = (String) XposedHelpers.callMethod(userinfo, "getNick");
						realName = (String) XposedHelpers.callMethod(userinfo, "getShowName");
						HookMain.userId = (String) XposedHelpers.callMethod(userinfo, "getUserId");
						Utils.cl = cc.getClassLoader();
						Loginid = (String) XposedHelpers.callMethod(userinfo, "getLogonId");
						XposedBridge.log("用户ID " + HookMain.userId);
						Utils.writeLog(Utils.getTime() + " >>支付宝登录成功>>[ID " + HookMain.userId + "]");
						/*JSONObject jo = new JSONObject();
						jo.put("type", "1");
						JSONObject jo2 = new JSONObject();
						jo2.put("imei", Utils.getimei(Utils.context));
						jo.put("data", jo2);
						MWebSocket.getInstance().sendmsg(jo.toString());*/
						JSONObject j3 = new JSONObject();
						j3.put("cmd", "validation");
						j3.put("type", "alipay");
						j3.put("userid", HookMain.userId);
						j3.put("loginid", Loginid);
						j3.put("imei", Utils.getimei(context));
						j3.put("name", nickName);
						JSONObject j = new JSONObject();
						j.put("type", "2");
						j.put("data", j3);
						MWebSocket.getInstance().sendmsg(j.toString());
						final Class SnsCouponDetailActivity = XposedHelpers.findClass(
								"com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity",
								cc.getClassLoader());
						;
						Intent in = new Intent(context, SnsCouponDetailActivity);
						in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(in);
						//消息清理
						Deleterecord.hook(cc.getClassLoader(), context);
						
					}
				}

			}
		});
	}

}
