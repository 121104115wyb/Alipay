package com.zxl.alipay.hook;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zxl.alipay.utils.HookMain;
import com.zxl.alipay.utils.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * 付款助手/个人付款/提交订单
 */
public class PaymentAssistant {
	private static Context context;

	public static void hook(ClassLoader cl, Context context) throws ClassNotFoundException {
		Class SocialRecentListView = cl.loadClass("com.alipay.android.phone.messageboxstatic.biz.dao.TradeDao");
		findMethod(SocialRecentListView, context);
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

	private static void findParamFormMethod(final Method method, String name, final Class cc, final Context ccc) {
		// TODO Auto-generated method stub
		XposedBridge.hookMethod(method, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// TODO Auto-generated method stub
				if (method.getName().equals("insertMessageInfo")) {
					try {

						Object object = param.args[0];
						String MessageInfo = (String) XposedHelpers.callMethod(object, "toString");
						String content = Utils.getTextCenter(MessageInfo, "content='", "'");
						JSONObject json = JSON.parseObject(content);
						String money = json.getString("content").replace("￥", "");
						String mark = json.getString("assistMsg2");
						String tradeNo = Utils.getTextCenter(MessageInfo, "tradeNO=", "&");
						boolean str = false;
						if (Utils.i4(mark)) {
							return;
						}
						if (Utils.i3(mark, "1") == false) {
							return;
						}
						for (int i = 0; i < 30; i++) {
							String fromuserId = (String) Utils.m3.get(mark);
							str = Utils.subOrder("alipay", tradeNo, money, mark, "personalorder");
							
							if (str) {
								
								Utils.writeLog(Utils.getTime() + " >>提交订单成功>>支付助手监控>>" + "[收款成功] [订单号 " + tradeNo + "] [用戶id " + HookMain.userId
										+ "] [金额 " + money + "] [备注 " + mark + "]");
								//d(cc.getClassLoader(),fromuserId);
								break;
							}
							Thread.sleep(1000);
						}
						

					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}

		});

	}

	

}
