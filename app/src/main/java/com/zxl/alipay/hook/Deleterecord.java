package com.zxl.alipay.hook;

import android.content.Context;
import android.database.Cursor;

import com.zxl.alipay.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;

public class Deleterecord {
	private static Context context;
	private static MethodHookParam profileSettingActivity, loadRecentListCursorSimple;
	private static Class profileSettingActivityc;
	private static boolean t = false, t2 = false;

	public static void hook(ClassLoader cl, Context context) throws ClassNotFoundException {
		Class RecentSessionDaoOp = cl
				.loadClass("com.alipay.mobile.socialcommonsdk.bizdata.contact.data.RecentSessionDaoOp");
		Class ProfileSettingActivity = cl
				.loadClass("com.alipay.android.phone.wallet.profileapp.ui.ProfileSettingActivity");
		findMethod(RecentSessionDaoOp,context);
		findMethod(ProfileSettingActivity,context);
	}

	private static void findMethod(Class hookclass, Context c) {
		if (!hookclass.isInterface()) {
			for (Method method : hookclass.getDeclaredMethods()) {

				if (!Modifier.isAbstract(method.getModifiers())) {
					context = c;
					if (hookclass.getName().equals("com.alipay.mobile.socialchatsdk.chat.data.ChatDataSyncCallback")) {
						if (method.getName().equals("onReceiveMessage")) {
							findParamFormMethod(method, hookclass.getName(), hookclass, context);
						}
					} else {
						findParamFormMethod(method, hookclass.getName(), hookclass, context);
					}

				}
			}
		}

	}

	private static void findParamFormMethod(final Method method, String name, final Class cc, Context context) {
		// TODO Auto-generated method stub
		XposedBridge.hookMethod(method, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				/**
				 * 删除好友hook获取
				 */
				if (cc.getName().equals("com.alipay.android.phone.wallet.profileapp.ui.ProfileSettingActivity")
						&& t == false) {
					if (method.getName().equals("d") && param.args.length == 1) {
						t = true;
						profileSettingActivity = param;
						profileSettingActivityc = cc;
						Utils.writeLog(Utils.getTime() + " >>支付宝好友删除准备完成");
						final Field k = cc.getDeclaredField("k");
						k.setAccessible(true);
						try {
							Object kk = k.get(param.args[0]);
							Field f = kk.getClass().getDeclaredField("userId");
							f.setAccessible(true);
							f.set(kk, "2088332372300000");
							k.set(param.args[0], kk);

						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}

				/**
				 * 最近聊天列表获取
				 */
				if (cc.getName().equals("com.alipay.mobile.socialcommonsdk.bizdata.contact.data.RecentSessionDaoOp")) {
					if (method.getName().equals("loadRecentListCursorSimple") && t2 == false) {
						t2 = true;
						loadRecentListCursorSimple = param;
						XposedBridge.log("[支付宝消息列表获取完成 ]");
						Utils.writeLog(Utils.getTime() + " >>支付宝消息列表获取完成");
					}
				}

			}
		});
	}

	public static void deleteF() {
		String d = readInternal2();
		if (d == null || d.length() == 0) {
			return;
		}
		Utils.writeLog(Utils.getTime() + " >>获取到清除命令>>" + d + "天前");
		try {
			if (loadRecentListCursorSimple == null) {
				return;
			}
			Cursor[] cursors = (Cursor[]) XposedBridge.invokeOriginalMethod(loadRecentListCursorSimple.method,
					loadRecentListCursorSimple.thisObject, loadRecentListCursorSimple.args);
			Cursor cursor = cursors[0];

			if (cursor.moveToFirst()) {
				do {
					try {
						final String id = cursor.getString(1);
						final String userid = cursor.getString(2);
						final String username = cursor.getString(3);
						String msgtime = cursor.getString(5);

						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub

								try {
									Thread.sleep(1000);
									Reddot.red(username, userid, id, Integer.parseInt(id));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}).start();
						double t = ((double) (System.currentTimeMillis() - Long.parseLong(msgtime)))
								/ ((double) (1000 * 60 * 60 * 24));

						if (t >= Integer.parseInt(d) && id.equals("1")) {
							// d(loadRecentListCursorSimple.thisObject.getClass().getClassLoader(),userid);
							final Field k = profileSettingActivityc.getDeclaredField("k");
							k.setAccessible(true);
							try {
								Object kk = k.get(profileSettingActivity.args[0]);
								Field f = kk.getClass().getDeclaredField("userId");
								f.setAccessible(true);
								f.set(kk, userid);
								k.set(profileSettingActivity.args[0], kk);
								XposedBridge.invokeOriginalMethod(profileSettingActivity.method,
										profileSettingActivity.thisObject, profileSettingActivity.args);
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static String readInternal2() {

		StringBuilder sb = new StringBuilder("");
		try {
			File file = new File("/sdcard/FYHook/setting.txt");
			// 打开文件输入流
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			// 读取文件内容
			String str;
			while ((str = bufferedReader.readLine()) != null) {
				sb.append(str);

			}
			bufferedReader.close();
			file.delete();
		} catch (Exception e) {
			// TODO: handle exception
		}

		return sb.toString();
	}
}
