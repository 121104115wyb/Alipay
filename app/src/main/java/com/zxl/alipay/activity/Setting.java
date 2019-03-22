package com.zxl.alipay.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import com.zxl.alipay.R;
import com.zxl.alipay.common.Common;
import com.zxl.alipay.utils.Utils;

import static com.zxl.alipay.common.Common.PostUserKeyUrl;

public class Setting extends Activity {
    private EditText set, key;
    private TextView uri;
    private Button b, b1;
    private static String RadioBtnChecked = "RadioBtnChecked";

    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            {"stat":false,"message":"imei重复或数据库异常","code":-102,"data":null}
            String code = "", mesage = "", stat = "";
            try {
                Bundle bundle = msg.getData();
                JSONObject data = new JSONObject(bundle.getString("result"));
                code = data.getString("code");
                mesage = data.getString("message");
                stat = data.getString("stat");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (msg.what) {
                case 1:
                    Toast.makeText(Setting.this, "绑定成功\n" + "code:" + code
                            + "\n" + "message:" + mesage + "\n" + "stat:" + stat + "\n" + "绑定user_key地址:" + PostUserKeyUrl, Toast.LENGTH_SHORT).show();

                    break;
                case 2:
                    Toast.makeText(Setting.this, "绑定失败\n" + "code:" + code
                            + "\n" + "message:" + mesage + "\n" + "stat:" + stat + "\n" + "绑定user_key地址:" + PostUserKeyUrl, Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seting);
        b = (Button) findViewById(R.id.button1);
        b1 = (Button) findViewById(R.id.subkey);
        set = (EditText) findViewById(R.id.editText1);
        key = (EditText) findViewById(R.id.key);
        RadioGroup radiogroup = (RadioGroup) findViewById(R.id.radiogroup);
        radiogroup.setOnCheckedChangeListener(new MyRadioGroupCheckListener());
        RadioButton productBtn = (RadioButton) findViewById(R.id.productbtn);
        RadioButton testBtn = (RadioButton) findViewById(R.id.testbtn);
        Boolean checked = getData(RadioBtnChecked);
        changeUrl(checked);
        if (checked) {
            productBtn.setChecked(checked);

        } else {
            testBtn.setChecked(!checked);
        }

        set.setText(readInternal());
        key.setText(readkey());
        uri = (TextView) findViewById(R.id.textView4);
        uri.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(Setting.this, MainActivity.class);
                Setting.this.startActivity(i);
            }
        });
    }

    //radioButton监听
    private class MyRadioGroupCheckListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                //正式地址
                case R.id.productbtn:
                    changeUrl(true);
                    break;
                //测试地址
                case R.id.testbtn:
                    changeUrl(false);
                    break;
                default:
                    break;

            }
        }
    }


    public void c(View v) {
        writeSet(set.getText().toString());
    }

    public void k(View v) {
        final String str = key.getText().toString();
        writekey(str);
        new Thread(new Runnable() {

            @Override
            public void run() {

                //http://xuanlv2.natapp1.cc/app/addevice
                // TODO Auto-generated method stub
                String result = sendPost(PostUserKeyUrl, "imei=" + Utils.getimei(Setting.this) + "&user_key=" + str);
                if (result.indexOf("true") != -1) {
                    Message msg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", result);
                    msg.setData(bundle);
                    msg.what = 1;
                    h.sendMessage(msg);
                } else {
                    Message msg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", result);
                    msg.setData(bundle);
                    msg.what = 2;
                    h.sendMessage(msg);
                }
            }
        }).start();
    }

    public synchronized static void writekey(String str) {
        try {
            File file1 = new File("/sdcard/FYHook/key.txt");
            File file2 = file1.getParentFile();
            if (!file1.exists()) {
                file2.mkdir();
                file1.createNewFile();

            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file1));

            bufferedWriter.append(str);

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String readkey() {

        StringBuilder sb = new StringBuilder("");
        try {
            File file = new File("/sdcard/FYHook/key.txt");
            //打开文件输入流
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            //读取文件内容
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);

            }
            bufferedReader.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

        return sb.toString();
    }

    public synchronized static void writeSet(String str) {
        try {
            File file1 = new File("/sdcard/FYHook/setting.txt");
            File file2 = file1.getParentFile();
            if (!file1.exists()) {
                file2.mkdir();
                file1.createNewFile();

            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file1));

            bufferedWriter.append(str);

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public static String readInternal() {

        StringBuilder sb = new StringBuilder("");
        try {
            File file = new File("/sdcard/FYHook/setting.txt");
            //打开文件输入流
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            //读取文件内容
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);

            }
            bufferedReader.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

        return sb.toString();
    }


    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //1.获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            //2.中文有乱码的需要将PrintWriter改为如下
            //out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8")
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            result = "false";
        }
        //使用finally块来关闭输出流、输入流
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


    void changeUrl(Boolean chencked) {

        if (chencked) {
            //正式地址
            PostUserKeyUrl = "http://182.61.178.193/app/addevice";
            writeurl(Common.WebSocketUrl);
        } else {
            //测试地址
            PostUserKeyUrl = "http://xuanlv2.natapp1.cc/app/addevice";
            writeurl(Common.WebSocketTestUrl);
        }
        saveData(RadioBtnChecked, chencked);
    }


    //保存按钮的状态
    private void saveData(String key, Boolean isChecked) {
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putBoolean(key, isChecked);
        editor.commit();
    }

    //获取按钮的状态
    private Boolean getData(String key) {
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        return preferences.getBoolean(key, true);

    }


    //xposend框架不能用SharedPreferences缓存机制，static静态变量机制
    //保存websocket地址到文件中
    public synchronized static void writeurl(String url) {
        try {
            File file1 = new File("/sdcard/FYHook/websocket.txt");
            File file2 = file1.getParentFile();
            if (!file1.exists()) {
                file2.mkdir();
                file1.createNewFile();

            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file1));

            bufferedWriter.append(url);

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    //读取文件中的websocket地址
    public static String readUrl() {

        StringBuilder sb = new StringBuilder("");
        try {
            File file = new File("/sdcard/FYHook/websocket.txt");
            //打开文件输入流
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            //读取文件内容
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);

            }
            bufferedReader.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

        return sb.toString();
    }

}
