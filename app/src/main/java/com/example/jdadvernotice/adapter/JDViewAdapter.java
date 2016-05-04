package com.example.jdadvernotice.adapter;


import android.view.View;
import com.example.jdadvernotice.view.JDAdverView;

/**
 * <ul>
 * 京东广告栏数据适配器
 * <ul>
 * 改版履历：
 * <ul>
 * <li>1.0.0 Created by Administrator on 2016/3/20.
 * <li>1.1.0 zhouronghua 2016/4/12 作为基类扩展用
 *
 */

public abstract class JDViewAdapter {

    /**
     * 获取数据的条数
     * @return
     */
    public abstract int getCount();

    /**
     * 获取摸个数据
     * @param position
     * @return
     */
    public abstract Object getItem(int position);

    /**
     * 获取条目布局
     * @param parent: 父控件
     * @param contentView: 缓存的视图项
     * 
     * @return
     */
    public abstract View getView(JDAdverView parent, View contentView);

    /**
     * 条目数据适配
     * @param view: 视图
     * @param data: 对应的数据项
     */
    public abstract void setItem(final View view, final Object data);
}
