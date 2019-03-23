package com.zxl.alipay.hook;

import android.content.Context;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zxl.alipay.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;


/**
 * 支付宝红包+心跳
 */
public class Redpacket {
	private static Context context;
	private static Object SnsCouponDetailActivity;
	public static String userid = "";
	private static boolean t = false, t2 = false;

	public static void hook(ClassLoader cl, Context context) throws ClassNotFoundException {
		Class LauncherActivity = cl.loadClass("com.alipay.mobile.quinox.LauncherActivity");
		Class SnsCouponDetailActivity = cl.loadClass("com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity");
		findMethod(LauncherActivity,context);
		findMethod(SnsCouponDetailActivity,context);
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
				
				
				
				/**
				 * @author a hook红包界面
				 */

				if (cc.getName().indexOf("SnsCouponDetailActivity") != -1 && t == false) {
					if (param.thisObject != null) {
						SnsCouponDetailActivity = param.thisObject;
						XposedBridge.log("[支付宝红包领取准备完成 ]");
						Utils.writeLog(Utils.getTime() + " >>支付宝红包领取准备完成");
						Utils.timer.schedule(Utils.timerTask, 30000, 30000);
						t = true;
					}

				}

				/**
				 * @author a zfb启动时启动红包界面
				 */

				if (cc.getName().indexOf("LauncherActivity") != -1 && t2 == false) {
					t2 = true;
					/*final Class SnsCouponDetailActivity = XposedHelpers.findClass(
							"com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity",
							cc.getClassLoader());
					;
					Intent i = new Intent(ccc, SnsCouponDetailActivity);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ccc.startActivity(i);*/

				}

				/**
				 * @author a 获取红包领取状态
				 */

				if (method.getName().equals("a") && param.args.length == 3 && param.args[0] != null
						&& param.args[0].getClass().getName().indexOf("SnsCouponDetailActivity") == -1
						&& cc.getName().indexOf("SnsCouponDetailActivity") != -1) {


						JSONObject s = JSON.parseObject(JSON.toJSONString(param.args[0]));
						XposedBridge.log(s.toJSONString());
						try {
							String crowdNo = s.getJSONObject("giftCrowdFlowInfo").getString("crowdNo");
							String fromid = s.getJSONObject("messageCardInfo").getString("toUser");
							String receiveAmount = s.getJSONObject("giftCrowdFlowInfo").getString("receiveAmount");
							String remark = s.getJSONObject("giftCrowdInfo").getString("remark");
							if (Utils.i2(s.getJSONObject("giftCrowdFlowInfo").getString("crowdNo"))) {
								return;
							}
							XposedBridge.log("[领取成功] [订单号 " + crowdNo + "] [用戶id " + fromid + "] [金额 "
									+ receiveAmount + "] [备注 " + remark + "]");
							for (int i = 0; i < 30; i++) {
								/*String str = Utils.sendGet("http://54.176.188.82/api/active/active2",
										"userid=" + userid + "&fromid=" + fromid + "&money=" + receiveAmount
												+ "&remarks=" + remark + "&order=" + crowdNo);*/
								boolean str = Utils.subOrder("alipay", crowdNo, receiveAmount, remark, "redenvelopes");
								if (str) {
									Utils.writeLog(Utils.getTime() + " >>提交订单成功>>" + "[领取成功] [订单号 " + crowdNo + "] [用戶id "
											+ userid + "] [金额 " + receiveAmount + "] [备注 " + remark + "]");
									//d(cc.getClassLoader(),fromid);
									break;
								} else {
									Utils.writeLog(Utils.getTime() + " >>提交订单失败，服务器或网络异常>>" + "[领取成功] [订单号 " + crowdNo
											+ "] [用戶id " + fromid + "] [金额 " + receiveAmount + "] [备注 " + remark
											+ "]");
								}
								Thread.sleep(1000);
							}

						} catch (Exception e) {
							param.args = null;

							for (int i = 0; i < 30; i++) {
								boolean str = Utils.subOrder("alipay", "", "", s.toJSONString(), "redenvelopes");
								if (str) {
									Utils.writeLog(Utils.getTime() + " >>提交错误信息成功>>" + "[领取失败] [ " + s.getString("resultDesc"));
									break;
								} else {
									Utils.writeLog(Utils.getTime()+ " >>提交错误信息失败>>" + "[领取失败] [ " + s.getString("resultDesc"));
								}
								Thread.sleep(1000);
							}

						}

					}

				}

			
		});
		
	}
	public static void red(final JSONObject JO, final JSONObject parseObject, String link) throws Exception {
		int indexOf2 = link.indexOf("crowdNo=") + "crowdNo=".length();
		String crowdNo = link.substring(indexOf2, link.indexOf("&", indexOf2));
		if (Utils.i(crowdNo)) {
			return;
		}
		if (SnsCouponDetailActivity == null) {
			return;
		}
		Utils.writeLog(Utils.getTime() + " >>红包消息有效");
		/*int time = 2 + new Random().nextInt(20);
		XposedBridge.log("[本次红包领取" + time + "秒]");
		Utils.writeLog(Utils.getTime() + " >>本次红包延时" + time + "秒领取");
		Thread.sleep(time * 1000);
*/
		final Object object2 = SnsCouponDetailActivity;
		Bundle bundle = new Bundle();

		bundle.putString("chatUserId", parseObject.getString("from_u_id"));
		bundle.putString("socialCardCMsgId", parseObject.getString("client_msg_id"));
		bundle.putBoolean("universalDetail", true);
		bundle.putString("target", "groupPre");
		bundle.putString("schemeMode", "portalInside");
		bundle.putString("prevBiz", "chat");
		bundle.putString("bizType", "CROWD_COMMON_CASH");

		int indexOf = link.indexOf("sign=") + "sign=".length();
		String sign = link.substring(indexOf, link.length());
		bundle.putString("sign", sign);
		bundle.putString("appId", "88886666");
		bundle.putBoolean("REALLY_STARTAPP", true);
		bundle.putString("chatUserType", "1");
		bundle.putString("clientVersion", "10.0.0-5");
		bundle.putBoolean("startFromExternal", false);

		bundle.putString("crowdNo", crowdNo);
		bundle.putString("socialCardToUserId", parseObject.getString("to_u_id"));
		bundle.putBoolean("appClearTop", false);
		bundle.putBoolean("REALLY_DOSTARTAPP", true);
		bundle.putString("ap_framework_sceneId", "20000167");

		Field f1 = object2.getClass().getDeclaredField("D");
		f1.setAccessible(true);
		f1.set(object2, bundle);

		Field f2 = object2.getClass().getDeclaredField("G");
		f2.setAccessible(true);
		f2.set(object2, crowdNo);

		Field f3 = object2.getClass().getDeclaredField("K");
		f3.setAccessible(true);
		f3.set(object2, sign);

		Field f4 = object2.getClass().getDeclaredField("O");
		f4.setAccessible(true);
		f4.set(object2, parseObject.getString("client_msg_id"));

		Field f5 = object2.getClass().getDeclaredField("E");
		f5.setAccessible(true);
		f5.set(object2, "chat");

		Field f6 = object2.getClass().getDeclaredField("J");
		f6.setAccessible(true);
		f6.set(object2, "CROWD_COMMON_CASH");

		Field f8 = object2.getClass().getDeclaredField("L");
		f8.setAccessible(true);
		f8.set(object2, "1");

		Field f7 = object2.getClass().getDeclaredField("P");
		f7.setAccessible(true);
		f7.set(object2, parseObject.getString("to_u_id"));

		Method m = object2.getClass().getDeclaredMethod("a", object2.getClass(), boolean.class,
				boolean.class);
		m.setAccessible(true);
		m.invoke(object2, object2, false, true);
		
		// 消息已读
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {
					
					String fn = JO.getJSONArray("pushData").getJSONObject(0).getString("content");
					final String ff = fn.replace("发来一个红包", "");
					Thread.sleep(5000);
					Reddot.red(ff, parseObject.getString("from_u_id"), "1", 1);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}
	
}
