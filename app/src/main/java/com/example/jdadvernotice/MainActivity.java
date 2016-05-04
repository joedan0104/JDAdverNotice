package com.example.jdadvernotice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.jdadvernotice.entity.AdverNotice;
import com.example.jdadvernotice.view.JDAdverView;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private  List<AdverNotice> datas = new ArrayList<AdverNotice>();
    private JDAdverView adverView;
    private NoticeAdapter adapter;
    private Button refresh;
    private Button change;
    private int changeTotal;
    private static final int MAX  = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        adapter = new NoticeAdapter(datas);
        adverView = (JDAdverView) findViewById(R.id.jdadver);
        adverView.setAdapter(adapter);
        refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        change = (Button) findViewById(R.id.change);
        change.setOnClickListener(this);
    }

    private void initData() {
        datas.add(new AdverNotice("瑞士维氏军刀 新品满200-50","最新"));
        datas.add(new AdverNotice("家居家装焕新季，讲199减100！","最火爆"));
        datas.add(new AdverNotice("带上相机去春游，尼康低至477","HOT"));
        datas.add(new AdverNotice("价格惊呆！电信千兆光纤上市","new"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        adverView.stop();
        Log.d("zsz", "JDAdverView onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        adverView.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                refreshAdver();
                break;
            case R.id.change:
                changeAdver();
                break;
        }
    }

    private void changeAdver() {
        if(datas.size() < MAX && changeTotal < MAX) {
            datas.add(new AdverNotice("孩子 " + changeTotal, "最新"));
            adapter.setList(datas);
            adverView.refresh();
            changeTotal = datas.size();
            if(changeTotal == MAX) {
                Toast.makeText(this, "已经达到最大值", Toast.LENGTH_SHORT).show();
            }
        } else if(changeTotal == MAX && datas.size() > 0) {
            datas.remove(datas.size() - 1);
            adapter.setList(datas);
            adverView.refresh();
            if(0 == datas.size()) {
                changeTotal = 0;
            }
        }
    }

    private void refreshAdver() {
        adverView.refresh();
    }
}
