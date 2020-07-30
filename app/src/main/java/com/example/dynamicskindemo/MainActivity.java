package com.example.dynamicskindemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dynamicskindemo.recycler.RecyclerAdapter;
import com.example.dynamicskindemo.skin.SkinEngine;
import com.example.dynamicskindemo.skin.SkinFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private List<String> mListData = new ArrayList<>();

    public static void verifyStoragePermissions(AppCompatActivity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), SkinFactory.getInstance());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        initData();
        initView();
    }

    private void initData() {
        for (int i = 0; i < 20; i++) {
            mListData.add("Hello Ninebot" + i);
        }
    }

    private void initView() {
        Button button = findViewById(R.id.btn);
        button.setOnClickListener(v -> {
            changeSkin();
        });

        Button button2 = findViewById(R.id.btn2);
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recycler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mListData);
        recyclerView.setAdapter(recyclerAdapter);
    }

    protected void changeSkin() {
        File skinFile = new File(Environment.getExternalStorageDirectory(), "SkinDemo/skin.apk");
        SkinEngine.getInstance().load(skinFile.getAbsolutePath()); //加载外部资源包
        SkinFactory.getInstance().applyAllSkinViews(); //执行换肤操作
        SkinFactory.getInstance().notifySkinListeners();
    }
}
