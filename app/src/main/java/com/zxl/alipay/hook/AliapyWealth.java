package com.zxl.alipay.hook;

import com.google.gson.Gson;
import com.zxl.alipay.common.Common;
import com.zxl.alipay.utils.MWebSocket;
import com.zxl.alipay.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;


public class AliapyWealth {

	public static String getAliapyWealth(ClassLoader cl) {
		String ret = "";
		try {
			Class<?> MicroServiceUtil = cl.loadClass("com.alipay.mobile.common.androidannotations.MicroServiceUtil");
			Class<?> WealthHomePBManager = cl
					.loadClass("com.alipay.mobilewealth.biz.service.gw.api.home.pb.WealthHomePBManager");

			Method getRpcMethod = MicroServiceUtil.getMethod("getRpcProxy", Class.class);
			getRpcMethod.setAccessible(true);
			Object wealthHomePBManager = getRpcMethod.invoke(null, WealthHomePBManager);

			if (wealthHomePBManager != null) {

				Class<?> WealthInfoDynamicReqPB = XposedHelpers
						.findClass("com.alipay.mobilewealth.biz.service.gw.request.home.WealthInfoDynamicReqPB", cl);
				Object wealthInfoDynamicReqPB = XposedHelpers.newInstance(WealthInfoDynamicReqPB);
				XposedHelpers.setObjectField(wealthInfoDynamicReqPB, "source", "HOME");

				Object result = XposedHelpers.callMethod(wealthHomePBManager, "queryWealthHomeInfoV996",
						wealthInfoDynamicReqPB);

				if (result != null) {
					ret = new Gson().toJson(result);
					JSONObject jsonObject = new JSONObject(ret);
					if (jsonObject.getString("resultCode").equalsIgnoreCase("100")) {
						JSONArray jsonArray = jsonObject.getJSONArray("moduleInfos");
						if (jsonArray != null) {
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject item = jsonArray.getJSONObject(i);
								if (item.getString("widgetId").equalsIgnoreCase("WEALTH_HOME_ACC_BALANCE")) {
									ret = item.getString("mainInfo").replace("元", "").trim();
								}
							}
						}
					}
				}

			}
		} catch (Exception e) {

		}
		return ret;
	}
	
	
	 /**
     * 提交支付宝总资产金额
     */
    public static void SubmitAliapyWealth(final ClassLoader cl)
    {
        
            new Thread() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("cmd", "wealth");
                        jsonObject.put("imei", Utils.getimei(Utils.context));
                        jsonObject.put("type", "alipay");
                        jsonObject.put("userid", Common.AlipayUserId);
                        jsonObject.put("wealth", getAliapyWealth(cl));
                        if (MWebSocket.getInstance().sendmsg(jsonObject.toString())) {
                           Utils.writeLog(Utils.getTime()+">>提交支付宝总资产成功");
                        } else {
                        	Utils.writeLog(Utils.getTime()+">>提交支付宝总资产失败");
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
            }.start();



    }
}
