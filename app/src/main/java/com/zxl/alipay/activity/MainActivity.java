package com.zxl.alipay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.zxl.alipay.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
	private TextView log,uri;
	
	public static Timer timer = new Timer();;
	Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1){
            	log.setText(readInternal());
            }
            super.handleMessage(msg);
        }
    };
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		log = (TextView) findViewById(R.id.textView1);
		log.setMovementMethod(ScrollingMovementMethod.getInstance());
		log.setText(readInternal());
		uri = (TextView) findViewById(R.id.textView2);
		uri.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, Setting.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
		timer.schedule(timerTask,1000,500);
		
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public static String readInternal(){
		
	    StringBuilder sb = new StringBuilder("");
	    try {
			File file = new File("/sdcard/FYHook/"+new SimpleDateFormat("yyyyMMdd").format(new Date())+".txt");
		    //打开文件输入流
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		    //读取文件内容
			String str ;
		    while((str = bufferedReader.readLine()) !=null){
		        sb.append(str+"\n");

		    }
		    bufferedReader.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	    
	    return sb.toString();
	}
}
