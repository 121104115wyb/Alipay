package com.zxl.alipay.hook;

import android.content.Context;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zxl.alipay.utils.HookMain;
import com.zxl.alipay.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


public class Messagemanipulation {
	private static Context context;

	/**
	 * 
	 * @param cl
	 * @param context
	 * @param red 红包功能消息开启
	 * @param personal 个人收款消息开启
	 * @param aa AA收款消息开启
	 * @throws ClassNotFoundException
	 */
	public static void hook(ClassLoader cl, Context context, boolean red, boolean personal, boolean aa) throws ClassNotFoundException {
		Class ChatDataSyncCallback = cl.loadClass("com.alipay.mobile.socialchatsdk.chat.data.ChatDataSyncCallback");
		findMethod(ChatDataSyncCallback,context,red,personal,aa);
	}

	private static void findMethod(Class hookclass, Context c, boolean red, boolean personal, boolean aa) {
		
		if (!hookclass.isInterface()) {
			for (Method method : hookclass.getDeclaredMethods()) {

				if (!Modifier.isAbstract(method.getModifiers())) {
					context = c;
					if(method.getName().equals("onReceiveMessage")) {
						findParamFormMethod(method, hookclass.getName(), hookclass, context,red,personal,aa);
					}

				}
			}
		}

	}

	private static void findParamFormMethod(final Method method, String name, final Class cc, final Context ccc, final boolean red, final boolean personal, final boolean aa) {
		// TODO Auto-generated method stub
		XposedBridge.hookMethod(method, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// TODO Auto-generated method stub
				if (method.getName().equals("onReceiveMessage") && param.args.length == 1) {
					final JSONObject JO = JSON.parseObject(JSON.toJSONString(param.args[0]));
					XposedBridge.log(JO.toJSONString());
					Object object = param.args[0];
					Field b = object.getClass().getDeclaredField("msgData");
					b.setAccessible(true);
					String str = (String) b.get(object);
					JSONArray parseArray = JSON.parseArray(str);
					String str2 = (String) parseArray.getJSONObject(0).getString("pl");
					final Class findClass6 = XposedHelpers.findClass("com.squareup.wire.Wire", cc.getClassLoader());
					final Class findClass7 = XposedHelpers.findClass(
							"com.alipay.mobilechat.core.model.message.MessagePayloadModel", cc.getClassLoader());
					Object[] objArr = new Object[] { new ArrayList() };
					Object[] objArr2 = new Object[] { Base64.decode(str2, 0), findClass7 };
					final JSONObject parseObject = JSON.parseObject(JSON.toJSONString(XposedHelpers
							.callMethod(XposedHelpers.newInstance(findClass6, objArr), "parseFrom", objArr2)));

					String link = parseObject.getString("link");
					XposedBridge.log(parseObject.toJSONString());
					if (link != null) {
						// 红包自动领取
						if (parseObject.toString().indexOf("红包") != -1 && red == true) {
							Redpacket.red(JO, parseObject, link);
						} else if (parseObject.toString().indexOf("向你付款") != -1 && personal == true) {

							String money = Utils.getTextCenter(parseObject.getString("biz_memo"), "向你付款", "元");
							String tradeNo = Utils.getTextCenter(parseObject.getString("client_msg_id"), "COLLET", "_P1");
							String mark = (String) Utils.m3.get(tradeNo);
							String fromuserId = parseObject.getString("from_u_id");
							boolean strs = false;
							for (int i = 0; i < 30; i++) {
								strs = Utils.subOrder("alipay", tradeNo, money, mark, "personalorder");
								if (strs) {
									Utils.writeLog(Utils.getTime() + " >>提交订单成功>>聊天窗口监控>>" + "[收款成功] [订单号 " + tradeNo + "] [用戶id "
											+ HookMain.userId + "] [金额 " + money + "] [备注 " + mark + "]");
									//d(cc.getClassLoader(),fromuserId);
									
									break;
								}
								Thread.sleep(1000);
							}
							
						} 

					} else if(parseObject.toString().indexOf("支付了你的") != -1 && aa == true) {
						
						String tradeNo = Utils.getTextCenter(parseObject.getString("client_msg_id"), "AA_PAY", "_PP");
						if (Utils.i4(tradeNo)) {
							return;
						}
						String fromuserId = parseObject.getString("from_u_id");
						for (int i = 0; i < 30; i++) {
							str = Utils.sendGet("http://54.176.188.82/api/active/aa",
									"userid=" + HookMain.userId + "&money=0&remarks=0&order="
											+ tradeNo + "&fromid=" + fromuserId);
							if (str.length() > 1 && str.equals("success")) {
								Utils.writeLog(Utils.getTime() + " >>提交订单成功>>聊天窗口监控>>" + "[AA收款成功] [订单号 " + tradeNo + "] [用戶id "
										+ HookMain.userId + "] [支付用户ID " + fromuserId + "]");
								
								break;
							}
							Thread.sleep(1000);
						}
					}
					
					else if(personal == true){
						//Personalreceipt.social(JO, parseObject, cc.getClassLoader());
					}

					
					// 消息已读
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							try {
								String fn = "";
								try {
									fn = JO.getJSONArray("pushData").getJSONObject(0).getString("content");
								} catch (Exception e) {
									// TODO: handle exception
									fn = parseObject.getJSONObject("template_data").getString("m");
									fn = Utils.getTextCenter(fn, "你已经添加了", "，现在");
								}
								
								final String ff = fn.replace("发来一个红包", "");
								Thread.sleep(3000);
								Reddot.red(ff, parseObject.getString("from_u_id"), "1", 1);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}).start();

				}
				
			}

			
		});
		
	}
	
	
	
}
