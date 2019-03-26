package com.zxl.alipay.common;


public class Common {
    public static String TAG = "QFZZ";

    //public static String WebSocketUrl = "ws://57552c82f0511154.natapp.cc:5588";

    public static String AlipayUserId = "";

    public static String WXLoginId = "";

    public static final String PREFERENCE_NAME="qfzzcfg";

    //websocket正式地址
    public static String WebSocketUrl = "ws://182.61.178.193:9002";
    //绑定用户提交正式地址
    public static String PostUserKeyUrl = "http://182.61.178.193/app/addevice";

    //websocket测试地址
    public static String WebSocketTestUrl = "ws://57552c82f0511154.natapp.cc:5588";
    //绑定用户提交测试地址
    public static String PostUserKeyTestUrl = "http://xuanlv2.natapp1.cc/app/addevice";

    //aa收款
    public static String DeviceTypeAA = "4";
    //红包
    public static String DeviceTypePacket = "1";
    //个人
    public static String DeviceTypePerson = "2";
    //钉钉
    public static String DeviceTypeDingDing = "3";
    //微信
    public static String DeviceTypeWEChat = "5";
}
