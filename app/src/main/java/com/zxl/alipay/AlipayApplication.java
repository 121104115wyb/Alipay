package com.zxl.alipay;

import android.app.Application;

/**
 * @author wyb
 * @date 2019/03/22
 */
public class AlipayApplication extends Application {

    private static AlipayApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


    void init(){
        //do what you want

    }


    static AlipayApplication getInstance() {

        return instance;
    }


}
