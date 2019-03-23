package com.zxl.alipay.hook;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.zxl.alipay.utils.HookMain;
import com.zxl.alipay.utils.MWebSocket;
import com.zxl.alipay.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;


/**
 * 个人收款
 */
public class Personalreceipt {
	private static Context context;
	private static Object SocialPersonalobj;
	private static Method SocialPersonalm;
	public static String userid = "";
	private static boolean t = false, t2 = false;

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

	private static void findParamFormMethod(final Method method, String name, final Class cc, Context context) {
		// TODO Auto-generated method stub
		XposedBridge.hookMethod(method, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// TODO Auto-generated method stub
				if (method.getName().equals("getUserInfo")) {
					Object userinfo = param.getResult();
					if (userinfo != null) {
						Class c1 = cc.getClassLoader()
								.loadClass("com.alipay.mobile.framework.service.common.impl.RpcServiceImpl");
						Class c2 = cc.getClassLoader()
								.loadClass("com.alipay.android.phone.personalapp.socialpayee.rpc.SingleCollectRpc");
						Object o = c1.newInstance();
						Method getRpcProxy = c1.getMethod("getRpcProxy", Class.class);
						Object oo = getRpcProxy.invoke(o, c2);
						Method[] ms = c2.getMethods();
						for (final Method m : ms) {
							if (m.getName().equals("createBill")) {
								SocialPersonalobj = oo;
								SocialPersonalm = m;
								Utils.writeLog(Utils.getTime() + " >>支付宝个人收款准备完成");
								XposedBridge.log("[支付宝个人收款准备完成]");
							}

						}
					}
				}

			}
		});
	}

	public static void load(Class cc) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		Class c1 = cc.getClassLoader().loadClass("com.alipay.mobile.framework.service.common.impl.RpcServiceImpl");
		Class c2 = cc.getClassLoader()
				.loadClass("com.alipay.android.phone.personalapp.socialpayee.rpc.SingleCollectRpc");
		Object o = c1.newInstance();
		Method getRpcProxy = c1.getMethod("getRpcProxy", Class.class);
		Object oo = getRpcProxy.invoke(o, c2);
		Method[] ms = c2.getMethods();
		for (final Method m : ms) {
			if (m.getName().equals("createBill")) {
				SocialPersonalobj = oo;
				SocialPersonalm = m;
				Utils.writeLog(Utils.getTime() + " >>支付宝个人收款准备完成");
				XposedBridge.log("[支付宝个人收款准备完成]");
			}

		}
	}

	/**
	 * @author a 个人收款操作
	 */
	public static String social(String userId, ClassLoader cl, String money, String memo) throws Exception {
		String fromuserId = userId;
		String fromlogonId = userId;
		String m = memo;
		String str = "";
		/*
		 * for (int i = 0; i < 30; i++) { str =
		 * Utils.sendGet("http://www.baopays.com/api/active/searchorder", "oid=" + m);
		 * if (str.length() > 1) { break; } Thread.sleep(1000); }
		 */
		/*
		 * JSONObject job = JSON.parseObject(str); if (job.getString("state") == null) {
		 * return; }
		 */
		/*
		 * if (job.getString("state").equals("0") == false) { return; }
		 */
		if (Double.parseDouble(money) <= 0) {
			return "-1";
		}

		if (Utils.i3(m, "1")) {
			return "-1";
		}
		Object o = cl.loadClass("com.alipay.android.phone.personalapp.socialpayee.rpc.req.SingleCreateReq")
				.newInstance();
		Field f = o.getClass().getField("logonId");
		f.setAccessible(true);
		f.set(o, fromlogonId);
		Field f1 = o.getClass().getField("userId");
		f1.setAccessible(true);
		f1.set(o, fromuserId);
		Field f2 = o.getClass().getField("desc");
		f2.setAccessible(true);
		f2.set(o, m);

		Field f3 = o.getClass().getField("billName");
		f3.setAccessible(true);
		f3.set(o, "个人收款");

		Field f4 = o.getClass().getField("payAmount");
		f4.setAccessible(true);
		f4.set(o, money);

		Field f5 = o.getClass().getField("source");
		f5.setAccessible(true);
		f5.set(o, "chat");

		Object obj = SocialPersonalm.invoke(SocialPersonalobj, o);
		Field transferNo = obj.getClass().getDeclaredField("transferNo");
		transferNo.setAccessible(true);
		String transferNos = (String) transferNo.get(obj);

		if (transferNos != null && transferNos.length() > 1) {

			Utils.i3(m, fromuserId);
			Utils.i3(transferNos, m);
			Utils.writeLog(Utils.getTime() + " >>发起收款成功>>" + "[备注 " + userId + "] [ 订单号" + transferNos + " ]");
			JSONObject j3 = new JSONObject();
			j3.put("cmd", "personal");
			j3.put("type", "alipay");
			j3.put("userid", HookMain.userId);
			j3.put("imei", Utils.getimei(Utils.context));
			j3.put("touserid", userId);
			j3.put("money", money);
			j3.put("orderid", transferNos);
			j3.put("memo", m);
			j3.put("status", "success");
			JSONObject j = new JSONObject();
			j.put("type", "2");
			j.put("data", j3);
			MWebSocket.getInstance().sendmsg(j.toString());
			return transferNos;
		}else {
			JSONObject j3 = new JSONObject();
			j3.put("cmd", "personal");
			j3.put("type", "alipay");
			j3.put("userid", HookMain.userId);
			j3.put("status", "success");
			j3.put("imei", Utils.getimei(Utils.context));
			j3.put("touserid", userId);
			j3.put("money", money);
			j3.put("orderid", transferNos);
			j3.put("memo", m);
			j3.put("status", "error");
			JSONObject j = new JSONObject();
			j.put("type", "2");
			j.put("data", j3);
			MWebSocket.getInstance().sendmsg(j.toString());
		}
		return "1";

		/*
		 * // 消息已读 new Thread(new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 * 
		 * try { String fn =
		 * JO.getJSONArray("pushData").getJSONObject(0).getString("content"); final
		 * String ff = fn.replace("发来一个红包", ""); Thread.sleep(5000); Reddot.red(ff,
		 * parseObject.getString("from_u_id"), "1", 1); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 * 
		 * } }).start();
		 */
	}

}
