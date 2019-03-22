package com.zxl.alipay.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zxl.alipay.R;

/**
 * @author wyb
 * @date 2019/03/22
 * @introduce TestActivity 这个活动的出现只是为了解决manifest 文件出现的警告，并没有什么业务逻辑
 * 如果你不加也没关系
 */
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }
}
