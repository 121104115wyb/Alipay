package com.zxl.alipay.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.alibaba.fastjson.JSONObject;
import com.zxl.alipay.hook.Deleterecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class Utils {
	public static Context context = null;
	public static Map m = new HashMap();
	public static Map m2 = new HashMap();
	public static Map m3 = new HashMap();
	public static Map m4 = new HashMap();
	public static ClassLoader cl = null;
	public static Timer timer = new Timer();;

	public static TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			try {
				cl.loadClass("com.alipay.mobile.quinox.LauncherActivity");
				if (HookMain.userId.length() > 5) {
					for (int i = 0; i < 10; i++) {
						
						boolean str = MWebSocket.getInstance().sendmsg("{\"type\":9}");
						if (str) {
							Utils.writeLog(Utils.getTime() + " >>[用户 " + HookMain.userId + " ]>>心跳>>提交成功");
							break;
						} else {
							Utils.writeLog(Utils.getTime() + " >>[用户 " + HookMain.userId + " ]>>心跳>>提交失败");
						}
						Thread.sleep(1000);
					}
					Deleterecord.deleteF();
				} else {

					Utils.writeLog(Utils.getTime() + " >>" + HookMain.userId + ">>心跳>>未登录");
				}

			} catch (Exception e) {

			}
		}
	};;
	/**
	 * 
	 * @param type 类型
	 * @param orderId 订单ID
	 * @param money 金额
	 * @param memo 备注
	 * @param ordertype 类型   红包: redenvelopes  二维码收款：orcode  转账收款：transferaccounts
	 */
	public static boolean subOrder(String type, String orderId, String money, String memo, String ordertype) {
		JSONObject j = new JSONObject();
		j.put("type", "2");
		
		JSONObject jso = new JSONObject();
		jso.put("cmd", "order");
		jso.put("type", type);
		jso.put("ordertype", ordertype);
		jso.put("money", money);
		jso.put("orderId", orderId);
		jso.put("imei", Utils.getimei(context));
		jso.put("memo", memo);
		jso.put("time", System.currentTimeMillis());
		
		
		j.put("data", jso);
		return MWebSocket.getInstance().sendmsg(j.toString());
	}
	
	
	public static boolean subaaOrder(String cmd, String type, String orderId, String money, String memo, String ordertype) {
		JSONObject j = new JSONObject();
		j.put("type", "2");
		
		JSONObject jso = new JSONObject();
		jso.put("cmd", "aaqrcodeorder");
		jso.put("type", type);
		jso.put("imei", Utils.getimei(context));
		jso.put("status", "success");
		jso.put("orderId", orderId);
		jso.put("money", money);
		jso.put("memo", memo);
		jso.put("time", System.currentTimeMillis());
		jso.put("ordertype", ordertype);
		
		j.put("data", jso);
		return MWebSocket.getInstance().sendmsg(j.toString());
	}
	public static synchronized boolean i(String cno) {

		if (m.get(cno) == null) {
			if (m.size() >= 1000) {
				m.clear();
			}
			m.put(cno, "1");
			return false;
		}
		return true;

	}

	public static synchronized boolean i2(String cno) {
		if (m2.get(cno) == null) {
			if (m2.size() >= 1000) {
				m2.clear();
			}
			m2.put(cno, "2");
			return false;
		}
		return true;

	}

	public static synchronized boolean i4(String cno) {
		if (m4.get(cno) == null) {
			if (m4.size() >= 1000) {
				m4.clear();
			}
			m4.put(cno, "2");
			return false;
		}
		return true;

	}

	public static synchronized boolean i3(String cno, String userid) {
		if (m3.get(cno) == null) {
			if (m3.size() >= 5000) {
				m3.clear();
			}
			if (userid.equals("1") != true) {
				m3.put(cno, userid);
			}

			return false;
		}
		return true;

	}

	public static String getTime() {
		return new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date());
	}

	/**
	 * @see //保存日志到目录
	 * @param str
	 */
	public synchronized static void writeLog(String str) {
		try {
			String txt = readInternal();
			File file1 = new File("/sdcard/FYHook/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt");
			File file2 = file1.getParentFile();
			if (!file1.exists()) {
				file2.mkdir();
				file1.createNewFile();

			}
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file1));

			bufferedWriter.append(str);
			if (txt.length() > 1) {
				bufferedWriter.newLine();
				bufferedWriter.append(txt);

			}
			bufferedWriter.flush();
			bufferedWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String readInternal() throws IOException {

		StringBuilder sb = new StringBuilder("");
		try {
			File file = new File("/sdcard/FYHook/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt");
			// 打开文件输入流
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			// 读取文件内容
			String str;
			while ((str = bufferedReader.readLine()) != null) {
				sb.append(str + "\n");
			}
			bufferedReader.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

		return sb.toString();
	}
	public static String readInternal2(){
		
	    StringBuilder sb = new StringBuilder("");
	    try {
			File file = new File("/sdcard/FYHook/setting.txt");
		    //打开文件输入流
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		    //读取文件内容
			String str ;
		    while((str = bufferedReader.readLine()) !=null){
		        sb.append(str);

		    }
		    bufferedReader.close();
		    file.delete();
		} catch (Exception e) {
			// TODO: handle exception
		}
	    
	    return sb.toString();
	}
	/**
	 * @see //过滤无关方法
	 * 
	 */
	public static boolean isClassNameValid(LoadPackageParam loadPackageParam, String className) {
		return !className.contains("$") && !className.contains("BuildConfig") && !className.contains(".R");
	}

	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			XposedBridge.log(urlNameString);
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

	public static String getTextCenter(String text, String begin, String end) {
		try {
			int b = text.indexOf(begin) + begin.length();
			int e = text.indexOf(end, b);
			return text.substring(b, e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}

	public static String timeStamp2Date(String time) {
		Long timeLong = Long.parseLong(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 要转换的时间格式
		Date date;
		try {
			date = sdf.parse(sdf.format(timeLong));
			return sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
     * 获取设备IMEI
     * @return
     */
	public static String getimei(Context context){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		String imei=tm.getDeviceId();
		if(imei==null){
			return "00000000000000000";
		}else{
			return tm.getDeviceId();
		}
	}
    
    
    
}
