package com.example.jdadvernotice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jdadvernotice.adapter.JDViewAdapter;
import com.example.jdadvernotice.entity.AdverNotice;
import com.example.jdadvernotice.view.JDAdverView;

import java.util.List;

/**
 * Created by Administrator on 2016/3/20.
 * 京东广告栏数据适配器
 *
 */

public class NoticeAdapter extends JDViewAdapter {
    private List<AdverNotice> mDatas;

    public class ViewHolder {
        /** 描述信息 */
        public TextView title;
        /** 时间字段 */
        public TextView url;
    }

    public NoticeAdapter(List<AdverNotice> mDatas) {
        this.mDatas = mDatas;
        if (mDatas == null || mDatas.isEmpty()) {
            throw new RuntimeException("nothing to show");
        }
    }

    public void setList(List<AdverNotice> mDatas) {
//        this.mDatas.clear();
        this.mDatas = mDatas;
//        if (mDatas == null || mDatas.isEmpty()) {
//            throw new RuntimeException("nothing to show");
//        }
    }

    /**
     * 获取数据的条数
     * @return
     */
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    /**
     * 获取摸个数据
     * @param position
     * @return
     */
    public AdverNotice getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public View getView(JDAdverView parent, View convertView) {
        View view = null;
        if(null == convertView) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //比如打开url
                    ViewHolder holder = (ViewHolder) v.getTag();
                    if(null != holder) {
                        Toast.makeText(v.getContext(),holder.url.getText(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.url = (TextView) view.findViewById(R.id.tag);
            view.setTag(holder);
        } else {
            view = convertView;
        }
        return view;
    }

    @Override
    public void setItem(View view, Object data) {
        AdverNotice item = (AdverNotice) data;
        if(null != view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if(null != item && null != holder) {
                holder.title.setText(item.title);
                holder.url.setText(item.url);
            }
        }
    }

//    /**
//     * 获取条目布局
//     * @param parent
//     * @return
//     */
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View view = null;
//        if(null == convertView) {
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, null);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //比如打开url
//                    ViewHolder holder = (ViewHolder) v.getTag();
//                    if(null != holder) {
//                        Toast.makeText(v.getContext(),holder.url.getText(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//            ViewHolder holder = new ViewHolder();
//            holder.title = (TextView) convertView.findViewById(R.id.title);
//            holder.url = (TextView) convertView.findViewById(R.id.tag);
//            view.setTag(holder);
//        } else {
//            view = convertView;
//        }
//        setItem(view, getItem(position));
//        return view;
//    }
}
