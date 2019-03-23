package com.zxl.alipay.utils;

import android.os.Handler;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.zxl.alipay.common.Common;
import com.zxl.alipay.hook.AliapyWealth;
import com.zxl.alipay.hook.Personalreceipt;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;



/**
 * @author 清风 QQ:274838061
 * @name QFZZ
 * @class name：MWebSocket
 * @time 2019/1/10 20:01
 * @desc websocket管理
 **/
public class MWebSocket {
    private static MWebSocket mInstance;

    private static final int FRAME_QUEUE_SIZE = 5; //帧队列
    private static final int CONNECT_TIMEOUT = 5000; //连接超时时间 毫秒


    private WebSocket ws;
    private WsListener mListener;

    /**
     * 获取唯一的实例
     * @return
     */
    public static MWebSocket getInstance(){
        if(mInstance == null){
            synchronized (MWebSocket.class){
                if(mInstance == null){
                    mInstance = new MWebSocket();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化并连接到服务器
     */
    public void init(){
        try {
            //连接 的websocket网址在 Common.WebSocketUrl 配置
            ws = new WebSocketFactory().createSocket(Common.WebSocketUrl, CONNECT_TIMEOUT)
                    .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(mListener = new WsListener())//添加回调监听
                    .addHeader("Origin","app:com.taobao.taobao")
                    .connectAsynchronously();//异步连接
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  @author 清风
     *  @desc 发送信息到服务器端
     *  @time 2019/1/10  19:19
     *  @params [msg]
     *  @return boolean
     */
    public boolean sendmsg(String msg)
    {
        boolean sendok = false;
        try
        {
                if (ws.isOpen()) {
                    ws.sendText(msg);
                    Utils.writeLog(Utils.getTime()+">>发送信息【" + msg + "】成功");
                    sendok = true;
                } else {
                    Utils.writeLog(Utils.getTime()+">>未连接到服务器");
                }



        }
        catch (Exception e)
        {

        }

        return sendok;
    }


    /**
     * 缁ф壙榛樿鐨勭洃鍚┖瀹炵幇WebSocketAdapter,閲嶅啓鎴戜滑闇�瑕佺殑鏂规硶
     * onTextMessage 鏀跺埌鏂囧瓧淇℃伅
     * onConnected 杩炴帴鎴愬姛
     * onConnectError 杩炴帴澶辫触
     * onDisconnected 杩炴帴鍏抽棴
     */
    class WsListener extends WebSocketAdapter {
        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            super.onTextMessage(websocket, text);
            Utils.writeLog(Utils.getTime()+">>接受："+text);
            try
            {
                //瑙ｆ瀽鏈嶅姟绔彂杩囨潵鐨勪俊鎭�
                JSONObject jsonObject = new JSONObject(text);
                String cmd = jsonObject.getString("cmd");



                if (cmd.equalsIgnoreCase("order")) //提交订单返回状态
                {
                    if (!jsonObject.getString("orderId").isEmpty()) {
                        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                            Utils.writeLog(Utils.getTime()+">>订单【" + jsonObject.getString("orderId") + "】提交成功");

                        } else {
                            Utils.writeLog(Utils.getTime()+">>订单【" + jsonObject.getString("orderId") + "】提交失败:" + jsonObject.getString("err"));
                        }
                    }
                    else
                    {
                        Utils.writeLog(Utils.getTime()+">>订单状态没有返回订单ID");
                    }
                }else if (cmd.equalsIgnoreCase("getwealth")) //
                {
                    if (jsonObject.getString("type").equalsIgnoreCase("alipay")) {
                        //获取支付余额
                    	AliapyWealth.SubmitAliapyWealth(Utils.cl);
                    }
                }else if(cmd.equalsIgnoreCase("personal")) {//个人收款
                	for(int i = 0 ; i < 20 ; i++ ) {
                		String status = Personalreceipt.social(jsonObject.getString("touserid"), Utils.cl,jsonObject.getString("money"),jsonObject.getString("memo"));
                    	if(status.length()>10 || status.equals("-1")) {
                    		break;
                    	}
                	}

                }else if(cmd.equalsIgnoreCase("aaqrcode")) {//个人收款
                    String data = zfbH5.getAAQrCodeAndSubmit(jsonObject.getString("money"),jsonObject.getString("mark"), Integer.parseInt(jsonObject.getString("renshu")));
                    JSONObject jsonObject1 = new JSONObject(data);
                    if (jsonObject1.has("success")) {
                        if (jsonObject1.getBoolean("success")) {
                            String batchNo = jsonObject1.getString("batchNo");
                            String token = jsonObject1.getString("token");
                            JSONObject jsonObject2 = new JSONObject();
                            jsonObject2.put("cmd", "aaqrcode");
                            jsonObject2.put("imei", Utils.getimei(Utils.context));
                            jsonObject2.put("type", "alipay");
                            jsonObject2.put("status", "success");
                            jsonObject2.put("userid", Common.AlipayUserId);
                            jsonObject2.put("batchno", batchNo);
                            jsonObject2.put("token", token);
                            jsonObject2.put("renshu", Integer.parseInt(jsonObject.getString("renshu")));
                            jsonObject2.put("money", jsonObject.getString("money"));
                            jsonObject2.put("mark", jsonObject.getString("mark"));
                            JSONObject j = new JSONObject();
                            j.put("type", "2");
                            j.put("data", jsonObject2);
                            sendmsg(j.toString());
                        }
                    }
                }
            }
            catch (Exception e)
            {

            }
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
                throws Exception {
            super.onConnected(websocket, headers);
            cancelReconnect();//连接成功的时候取消重连,初始化连接次数
            JSONObject jo = new JSONObject();
            jo.put("type", "1");
            JSONObject jo2 = new JSONObject();
            jo2.put("imei", Utils.getimei(Utils.context));
            jo.put("data", jo2);
            sendmsg(jo.toString());




        }


        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception)
                throws Exception {
            super.onConnectError(websocket, exception);

            reconnect();//连接断开的时候调用重连方法
            //Common.HasSendOrder = false;
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
                throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            reconnect();//连接断开的时候调用重连方法
//            Common.HasSendOrder = false;
        }

    }


    /**
     * 断开连接
     */
    public void disconnect(){
        if(ws != null)
            ws.disconnect();
    }


    private Handler mHandler =  new Handler();

    private int reconnectCount = 0;//重连次数
    private long minInterval = 3000;//重连最小时间间隔
    private long maxInterval = 20000;//重连最大时间间隔

    /**
     * 重连websocket
     */
    public void reconnect() {

        if (ws != null &&
                !ws.isOpen() &&//当前连接断开了
                ws.getState() != WebSocketState.CONNECTING) {//不是正在重连状态

            reconnectCount++;

            long reconnectTime = minInterval;
            if (reconnectCount > 3) {

                long temp = minInterval * (reconnectCount - 2);
                reconnectTime = temp > maxInterval ? maxInterval : temp;
            }

            mHandler.postDelayed(mReconnectTask, reconnectTime);
        }
    }


    private Runnable mReconnectTask = new Runnable() {
        @Override
        public void run() {
            try {
                ws = new WebSocketFactory().createSocket(Common.WebSocketUrl, CONNECT_TIMEOUT)
                        .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                        .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                        .addListener(mListener = new WsListener())//添加回调监听
                        .addHeader("Origin","app:com.taobao.taobao")
                        .connectAsynchronously();//异步连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 取消重连
     */
    private void cancelReconnect() {
        reconnectCount = 0;
        mHandler.removeCallbacks(mReconnectTask);
    }



}
