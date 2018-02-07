package com.library.pickerphoto.application;

import android.content.Context;
import android.support.multidex.MultiDex;

import net.oschina.common.helper.ReadStateHelper;


/**
 * Created by qiujuer
 * on 2016/10/27.
 */
public class OSCApplication extends AppContext {
    private static final String CONFIG_READ_STATE_PRE = "CONFIG_READ_STATE_PRE_";

    @Override
    public void onCreate() {
        super.onCreate();

    }




    /**
     * 获取已读状态管理器
     *
     * @param mark 传入标示，如：博客：blog; 新闻：news
     * @return 已读状态管理器
     */
    public static ReadState getReadState(String mark) {
        ReadStateHelper helper = ReadStateHelper.create(getInstance(),
                CONFIG_READ_STATE_PRE + mark, 100);
        return new ReadState(helper);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * 一个已读状态管理器
     */
    public static class ReadState {
        private ReadStateHelper helper;

        ReadState(ReadStateHelper helper) {
            this.helper = helper;
        }

        /**
         * 添加已读状态
         *
         * @param key 一般为资讯等Id
         */
        public void put(long key) {
            helper.put(key);
        }

        /**
         * 添加已读状态
         *
         * @param key 一般为资讯等Id
         */
        public void put(String key) {
            helper.put(key);
        }

        /**
         * 获取是否为已读
         *
         * @param key 一般为资讯等Id
         * @return True 已读
         */
        public boolean already(long key) {
            return helper.already(key);
        }

        /**
         * 获取是否为已读
         *
         * @param key 一般为资讯等Id
         * @return True 已读
         */
        public boolean already(String key) {
            return helper.already(key);
        }
    }
}
