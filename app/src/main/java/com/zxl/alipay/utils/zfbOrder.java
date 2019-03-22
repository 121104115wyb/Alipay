package com.zxl.alipay.utils;


import android.content.Context;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class zfbOrder {

	public static String sendGet(String url, String cookie) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url;
			XposedBridge.log(urlNameString);
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("Cookie", cookie);
			connection.setRequestProperty("Referer", "https://render.alipay.com/p/z/merchant-mgnt/simple-order.html");
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Linux; U; Android 7.1.1; zh-CN; 1605-A01 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/11.8.8.968 UWS/2.13.1.39 Mobile Safari/537.36 UCBS/2.13.1.39_180615144818 NebulaSDK/1.8.100112 Nebula AlipayDefined(nt:WIFI,ws:360|0|3.0) AliApp(AP/10.1.22.835) AlipayClient/10.1.22.835 Language/zh-Hans useStatusBar/true isConcaveScreen/false");
			connection.setRequestProperty("X-Alipay-Client-Session", "check");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段

			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取当前支付宝的H5 cookie
	 * 
	 * @param appClassLoader
	 * @return
	 */
	public static String getCookieStr(ClassLoader appClassLoader) {
		String cookieStr = "";
		// 获得cookieStr
		XposedHelpers.callStaticMethod(XposedHelpers.findClass(
				"com.alipay.mobile.common.transportext.biz.appevent.AmnetUserInfo", appClassLoader), "getSessionid");
		Context context = (Context) XposedHelpers.callStaticMethod(XposedHelpers.findClass(
				"com.alipay.mobile.common.transportext.biz.shared.ExtTransportEnv", appClassLoader), "getAppContext");
		if (context != null) {
			Object readSettingServerUrl = XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("com.alipay.mobile.common.helper.ReadSettingServerUrl", appClassLoader),
					"getInstance");
			if (readSettingServerUrl != null) {
				// String gWFURL = (String)
				// XposedHelpers.callMethod(readSettingServerUrl, "getGWFURL",
				// context);
				String gWFURL = ".alipay.com";
				cookieStr = (String) XposedHelpers.callStaticMethod(XposedHelpers
						.findClass("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", appClassLoader),
						"getCookie", gWFURL);
			} else {
				Utils.writeLog("异常readSettingServerUrl为空");
			}
		} else {
			Utils.writeLog("异常context为空");
		}
		return cookieStr;
	}

	public static void getTradeInfoFromAPP(final Context context, final String cookie) {
		String url = "https://mbillexprod.alipay.com/enterprise/walletTradeList.json?lastTradeNo=&lastDate=&pageSize=10&shopId=&_inputcharset=gbk&ctoken&source=&_ksTS="
				+ System.currentTimeMillis() + "_49&_callback=&_input_charset=utf-8";
		// HttpUtils httpUtils = new HttpUtils();
		// httpUtils.configResponseTextCharset("GBK");
		String result = "";
		result = sendGet(url, cookie);

		result = result.replace("/**/(", "").replace("})", "}");
		try {
			JSONObject jsonObject = new JSONObject(result);

			XposedBridge.log("getTradeInfoFromAPP获取数据完整，返回数据" + result);
			if (jsonObject.has("status")) {
				String status = jsonObject.getString("status");
				if (status.equals("succeed")) {
					JSONObject res = jsonObject.getJSONObject("result");
					JSONArray jsonArray = res.getJSONArray("list");
					if (jsonArray != null && jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i++) {

							JSONObject object = jsonArray.getJSONObject(i);
							final String tradeNo = object.getString("tradeNo");
							String urls = "https://tradeeportlet.alipay.com/wireless/tradeDetail.htm?tradeNo=" + tradeNo
									+ "&source=channel&_from_url=https%3A%2F%2Frender.alipay.com%2Fp%2Fz%2Fmerchant-mgnt%2Fsimple-order._h_t_m_l_%3Fsource%3Dmdb_card";
							result = sendGet(urls, cookie);
							org.jsoup.nodes.Document document = Jsoup.parse(result);
							Elements elements = document.getElementsByClass("trade-info-value");
							if (elements.size() >= 5) {
								XposedBridge.log(elements.toString());
								String money = document.getElementsByClass("amount").get(0).ownText().replace("+", "")
										.replace("-", "");
								String mark = elements.get(3).ownText();
								String dt = System.currentTimeMillis() + "";
								XposedBridge.log(money + "--" + mark);

							}

						}
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public static String getTs(ClassLoader appClassLoader) {
		String value = "";
		try {

			value = (String) XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("com.alipay.mobile.common.transport.utils.GtsUtils", appClassLoader),
					"get64HexCurrentTimeMillis");
		} catch (Exception e) {

		}
		return value;
	}

	public static String getpackage_nick(ClassLoader appClassLoader) {
		String package_nick = "";
		try {
			// 获得package_nick
			package_nick = (String) XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("com.alipay.mobile.nebula.appcenter.util.H5AppUtil", appClassLoader),
					"getPackageNick", "66666676");
		} catch (Exception e) {

		}
		return package_nick;
	}

	public static String getminiwua(ClassLoader appClassLoader, final Context context) {
		String value = "";
		try {

			value = (String) XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("com.alipay.rdssecuritysdk.v2.face.RDSClient", appClassLoader),
					"getMiniWuaData", context);
		} catch (Exception e) {

		}
		return value;
	}

	public static String getDid(ClassLoader appClassLoader) {
		String value = "";
		try {

			value = (String) XposedHelpers.callStaticMethod(XposedHelpers
					.findClass("com.alipay.mobile.common.netsdkextdependapi.deviceinfo.DeviceInfoUtil", appClassLoader),
					"getDeviceId");
		} catch (Exception e) {

		}
		return value;
	}

	public static String getclientVersion(ClassLoader appClassLoader) {
		String value = "";
		try {

			value = (String) XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("com.alipay.mobile.nebula.util.H5Utils", appClassLoader), "getVersion");
		} catch (Exception e) {

		}
		return value;
	}

	public static String getsign(ClassLoader appClassLoader, String content, String Ts) {
		String value = "";
		try {
			Class<?> SignRequest = XposedHelpers
					.findClass("com.alipay.mobile.common.netsdkextdependapi.security.SignRequest", appClassLoader);
			Object req = XposedHelpers.newInstance(SignRequest);
			XposedHelpers.setObjectField(req, "appkey", "rpc-sdk-online");
			// QFLog.e(TAG,"content:"+content+" Ts:"+Ts);
			String strBase64 = Base64.encodeToString(content.getBytes("UTF-8"), Base64.NO_WRAP);
			// QFLog.e(TAG,"Operation-Type=alipay.mobile.bill.QuerySingleBillDetailForH5&Request-Data="+strBase64+"&Ts="+Ts);
			XposedHelpers.setObjectField(req, "content",
					"Operation-Type=alipay.mobile.bill.QuerySingleBillDetailForH5&Request-Data=" + strBase64 + "&Ts="
							+ Ts);
			XposedHelpers.setIntField(req, "signType", 0);

			Object signresult = XposedHelpers.callStaticMethod(XposedHelpers
					.findClass("com.alipay.mobile.common.netsdkextdependapi.security.SecurityUtil", appClassLoader),
					"signature", req);
			if (signresult != null) {
				value = (String) XposedHelpers.getObjectField(signresult, "sign");
			}
		} catch (Exception e) {

		}
		return value;
	}

	public static String getversion(ClassLoader appClassLoader) {
		String version = "";
		try {

			Object AppProvider = XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("com.alipay.mobile.nebula.util.H5Utils", appClassLoader), "getProvider",
					"com.alipay.mobile.nebula.provider.H5AppProvider");
			version = (String) XposedHelpers.callMethod(AppProvider, "getVersion", "66666676");
		} catch (Exception e) {

		}
		return version;
	}
	public static void getorder(final Context context, final String cookie, ClassLoader appClassLoader, String tradeNo) throws JSONException {
		String result = getorder2(context,cookie,appClassLoader,tradeNo);
		JSONObject jsonObject = new JSONObject(result);
        if (jsonObject.has("succ")) {
            if (jsonObject.getBoolean("succ")) {
                String mark = "";
                JSONArray jsonArray = jsonObject.getJSONArray("fields");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    if (item.getString("templateId").equalsIgnoreCase("BLDetailCommon")) {
                        JSONObject tmpjson = new JSONObject(item.getString("value"));
                        mark =tmpjson.getString("content");
                        break;
                    }

                }
                String dt = jsonObject.getJSONObject("extension").getString("gmtBizCreateTime");
            }
        }
	} 
	public static String getorder2(final Context context, final String cookie, ClassLoader appClassLoader, String tradeNo) {
		BufferedReader in = null;
		PrintWriter out = null;
		String result = "";
		try {
			String tinfo = "[{\"bizType\":\"0\",\"tradeNo\":\""+tradeNo+"\"}]";
			String url = "https://mobilegw.alipay.com/mgw.htm";

			// HttpUtils httpUtils = new HttpUtils("Android_Ant_Client");
			String appid = "66666676";
			String Ts = getTs(appClassLoader);

			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性

			connection.setRequestProperty("User-Agent", "Android_Ant_Client");
			connection.setRequestProperty("pagets", "-__MVkKy+5_");
			connection.setRequestProperty("lastclickspm", "");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("package_nick", getpackage_nick(appClassLoader));
			connection.setRequestProperty("x-nb-appid", "AP_" + appid + "_android");
			connection.setRequestProperty("nbversion", getversion(appClassLoader));
			connection.setRequestProperty("nbappid", appid);
			connection.setRequestProperty("visibleflag", "1");
			connection.setRequestProperty("miniwua", getminiwua(appClassLoader, context));
			connection.setRequestProperty("AppId", "Android-container");
			connection.setRequestProperty("Version", "2");
			connection.setRequestProperty("Did", getDid(appClassLoader));
			connection.setRequestProperty("Operation-Type", "alipay.mobile.bill.QuerySingleBillDetailForH5");
			connection.setRequestProperty("Ts", Ts);
			connection.setRequestProperty("Scene", "active");
			connection.setRequestProperty("clientVersion", getclientVersion(appClassLoader));
			connection.setRequestProperty("Cookie", cookie);
			connection.setRequestProperty("Accept-Language", "zh-Hans");
			connection.setRequestProperty("Retryable", "1");
			connection.setRequestProperty("signType", "0");
			String sign = getsign(appClassLoader, tinfo, Ts);
			connection.setRequestProperty("Sign", sign);

			connection.setRequestProperty("X-Alipay-Client-Session", "check");
			// 建立实际的连接
			connection.setDoOutput(true);
			connection.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(connection.getOutputStream());
			// 发送请求参数
			out.print(tinfo);

			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += "/n" + line;
			}
		} catch (Exception e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;

	}
}
