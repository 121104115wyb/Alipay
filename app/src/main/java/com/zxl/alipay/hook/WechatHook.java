package com.zxl.alipay.hook;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import com.zxl.alipay.common.Common;
import com.zxl.alipay.utils.HookMain;
import com.zxl.alipay.utils.Utils;
import com.zxl.alipay.utils.XmlToJson;

import org.json.JSONObject;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * @author 清风 QQ:274838061
 * @name QFZZ
 * @class name：WechatHook
 * @time 2019/1/10 22:28
 * @desc 微信HOOk主类
 **/
public class WechatHook {

    public Class<?> CollectCreateQRCodeUI = null;

    private static WechatHook mInstance;

    /**
     * 当前类的唯一实例
     * @return
     */
    public static WechatHook getInstance(){
        if(mInstance == null){
            synchronized (WechatHook.class){
                if(mInstance == null){
                    mInstance = new WechatHook();
                }
            }
        }
        return mInstance;
    }

    public void StartGenQrCode(String mark, String money)
    {
        if (CollectCreateQRCodeUI == null)
        {
            Utils.writeLog("微信没有初始化成功");
            return;
        }
        Intent intent2=new Intent(Utils.context, CollectCreateQRCodeUI);
        intent2.putExtra("mark", mark);
        intent2.putExtra("money", money);
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.context.startActivity(intent2);

    }

    public void hook(final ClassLoader appClassLoader, final Context context) {
        try {
            XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase",appClassLoader, "insert", String.class, String.class, ContentValues.class,
                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param)
                                throws Throwable {
                            try {
                                ContentValues contentValues = (ContentValues) param.args[2];
                                String tableName = (String) param.args[0];
                                if (TextUtils.isEmpty(tableName) || !tableName.equals("message")) {
                                    return;
                                }
                                Integer type = contentValues.getAsInteger("type");
                                if (null == type) {
                                    return;
                                }
                                if(type==318767153){
                                    JSONObject msg=new XmlToJson.Builder(contentValues.getAsString("content")).build().getJSONObject("msg");
                                    XposedBridge.log(msg.toString());
                                    if(!msg.toString().contains("零钱提现")){
                                        Utils.writeLog("=========微信收到订单start========");
                                        String money=msg.getJSONObject("appmsg").getJSONObject("mmreader").getJSONObject("template_detail").getJSONObject("line_content").getJSONObject("topline").getJSONObject("value").getString("word");
                                        money=money.replace("￥", "");
                                        String mark=msg.getJSONObject("appmsg").getJSONObject("mmreader").getJSONObject("template_detail").getJSONObject("line_content").getJSONObject("lines").getJSONArray("line").getJSONObject(0).getJSONObject("value").getString("word");
                                        String pay_outtradeno="";
                                        try {
                                            pay_outtradeno=msg.getJSONObject("appmsg").getJSONObject("ext_pay_info").getString("pay_outtradeno");
                                        } catch (Exception e) {
                                            pay_outtradeno=msg.getJSONObject("appmsg").getString("template_id");
                                        }
                                        Utils.writeLog("收到微信支付订单："+pay_outtradeno+"=="+money+"=="+mark);

                                        Utils.subOrder("wechat",pay_outtradeno,money,mark,"orcode");

                                        Utils.writeLog("=========微信收到订单end========");
                                    }
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e.getMessage());
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                        }
                    });
            Class<?> clazz= XposedHelpers.findClass("com.tencent.mm.plugin.collect.b.s", appClassLoader);
            XposedBridge.hookAllMethods(clazz, "a", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    //更新余额操作
//					Double balance=PayHelperUtils.getWechatBalance(appClassLoader);
//					PayHelperUtils.sendBalance("wechat", balance+"", context);

                    Utils.writeLog("=========微信生成完成start========");
                    if(Utils.getVerName(context).equals("6.6.7")){
                        Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "hUL");
                        double money = (double) moneyField.get(param.thisObject);

                        Field markField = XposedHelpers.findField(param.thisObject.getClass(), "desc");
                        String mark = (String) markField.get(param.thisObject);

                        Field payurlField = XposedHelpers.findField(param.thisObject.getClass(), "hUK");
                        String payurl = (String) payurlField.get(param.thisObject);

                        if(payurl!=null){
                            XposedBridge.log("调用增加数据方法==>微信");
                            Utils.writeLog("微信生成成功,金额:"+money+"备注:"+mark+"二维码:"+payurl);
                            Utils.SubmiWXSolidcode(payurl, String.valueOf( money),mark);
                        }

                    }else if(Utils.getVerName(context).equals("6.6.6")){
                        Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "llG");
                        double money = (double) moneyField.get(param.thisObject);

                        Field markField = XposedHelpers.findField(param.thisObject.getClass(), "desc");
                        String mark = (String) markField.get(param.thisObject);

                        Field payurlField = XposedHelpers.findField(param.thisObject.getClass(), "llF");
                        String payurl = (String) payurlField.get(param.thisObject);

                        if(payurl!=null){
                            XposedBridge.log("调用增加数据方法==>微信");
                            Utils.writeLog("微信生成成功,金额:"+money+"备注:"+mark+"二维码:"+payurl);
                            Utils.SubmiWXSolidcode(payurl, String.valueOf( money),mark);
                        }

                    }else if(Utils.getVerName(context).equals("6.7.3")){

                        Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "iHP");
                        double money = (double) moneyField.get(param.thisObject);

                        Field markField = XposedHelpers.findField(param.thisObject.getClass(), "desc");
                        String mark = (String) markField.get(param.thisObject);

                        Field payurlField = XposedHelpers.findField(param.thisObject.getClass(), "iHO");
                        String payurl = (String) payurlField.get(param.thisObject);

                        if(payurl!=null){
                            XposedBridge.log("调用增加数据方法==>微信");
                            Utils.writeLog("微信生成成功,金额:"+money+"备注:"+mark+"二维码:"+payurl);
                            Utils.SubmiWXSolidcode(payurl, String.valueOf( money),mark);

                        }

                    }

                    Utils.writeLog("=========微信生成完成end========");
                }
            });

            XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI",appClassLoader, "initView",
                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param)
                                throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                            //微信6.6.7新版修复
                            Utils.writeLog("=========微信设置金额start========");
                            try {
                                Utils.writeLog("版本："+Utils.getVerName(context));
                                if (Utils.getVerName(context).equals("6.6.7")) {
                                    Intent intent = ((Activity) param.thisObject).getIntent();
                                    String mark = intent.getStringExtra("mark");
                                    String money = intent.getStringExtra("money");
                                    //获取WalletFormView控件
                                    Field WalletFormViewField = XposedHelpers.findField(param.thisObject.getClass(), "hXD");
                                    Object WalletFormView = WalletFormViewField.get(param.thisObject);
                                    Class<?> WalletFormViewClass = XposedHelpers.findClass("com.tencent.mm.wallet_core.ui.formview.WalletFormView", appClassLoader);
                                    //获取金额控件
                                    Field AefField = XposedHelpers.findField(WalletFormViewClass, "uZy");
                                    Object AefView = AefField.get(WalletFormView);
                                    //call设置金额方法
                                    XposedHelpers.callMethod(AefView, "setText", money);
                                    //call设置备注方法
                                    Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
                                    XposedHelpers.callStaticMethod(clazz, "a", param.thisObject, mark);
                                    XposedHelpers.callStaticMethod(clazz, "c", param.thisObject);
                                    //点击确定
                                    Button click = (Button) XposedHelpers.callMethod(param.thisObject, "findViewById", 2131756838);
                                    click.performClick();
                                } else if (Utils.getVerName(context).equals("6.6.6")) {
                                    Intent intent = ((Activity) param.thisObject).getIntent();
                                    String mark = intent.getStringExtra("mark");
                                    String money = intent.getStringExtra("money");
                                    //获取WalletFormView控件
                                    Field WalletFormViewField = XposedHelpers.findField(param.thisObject.getClass(), "loz");
                                    Object WalletFormView = WalletFormViewField.get(param.thisObject);
                                    Class<?> WalletFormViewClass = XposedHelpers.findClass("com.tencent.mm.wallet_core.ui.formview.WalletFormView", appClassLoader);
                                    //获取金额控件
                                    Field AefField = XposedHelpers.findField(WalletFormViewClass, "Aef");
                                    Object AefView = AefField.get(WalletFormView);
                                    //call设置金额方法
                                    XposedHelpers.callMethod(AefView, "setText", money);
                                    //call设置备注方法
                                    Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
                                    XposedHelpers.callStaticMethod(clazz, "a", param.thisObject, mark);
                                    XposedHelpers.callStaticMethod(clazz, "c", param.thisObject);
                                    //点击确定
                                    Button click = (Button) XposedHelpers.callMethod(param.thisObject, "findViewById", 2131756780);
                                    click.performClick();
                                } else if (Utils.getVerName(context).equals("6.7.3")) {
                                    try {
                                        Intent intent = ((Activity) param.thisObject).getIntent();
                                        String mark = intent.getStringExtra("mark");
                                        String money = intent.getStringExtra("money");
                                        Utils.writeLog( "mark:"+mark+" money:"+money);
                                        Object iKG = XposedHelpers.getObjectField(param.thisObject, "iKG");
                                        XposedHelpers.callMethod(iKG, "setText", money);
                                        XposedHelpers.setObjectField(param.thisObject, "dkv", mark);
                                        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
                                        XposedHelpers.callStaticMethod(clazz, "c", param.thisObject);
                                        //点击确定
                                        final Button btnnext = (Button) XposedHelpers.callMethod(param.thisObject,"findViewById",2131822647);
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Utils.writeLog( "点击确定");
                                                btnnext.performClick();
                                            }
                                        },1L);


                                    } catch (Exception e) {
                                        Utils.writeLog( Log.getStackTraceString(e));
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                Utils.writeLog( Log.getStackTraceString(e));
                            }
                            Utils.writeLog("=========微信设置金额end========");
                        }
                    });
            // hook获取loginid
            XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", appClassLoader, "onResume",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String loginid=Utils.getWechatLoginId(context);
                            loginid=loginid.replace("+86", "");
                            HookMain.userId = loginid;
                            Common.WXLoginId = loginid;
                            CollectCreateQRCodeUI = XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
                            Utils.SubmitWXUserId();
                        }
                    });

        } catch (Exception e) {
            Utils.writeLog("异常"+e.getMessage());
        }
    }
}
