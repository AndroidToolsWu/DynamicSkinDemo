package com.example.dynamicskindemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;

import com.example.dynamicskindemo.recycler.RecyclerAdapter;
import com.example.dynamicskindemo.skin.SkinChangeListener;
import com.example.dynamicskindemo.skin.SkinEngine;
import com.example.dynamicskindemo.skin.SkinFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements  SkinChangeListener{

    public static String TAG = "MainActivity";
    private Button mButton,mButton2;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mRecyclerAdapter;

    private List<String> mListData = new ArrayList<>();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        initData();
        initView();
    }

    private void initData() {
        for (int i=0; i<20; i++){
            mListData.add("Hello Ninebot"+i);
        }
    }

    private void initView() {
        mButton = findViewById(R.id.btn);
        mButton.setOnClickListener( v ->{
            changeSkin();
        });

        mButton2 = findViewById(R.id.btn2);
        mButton2.setOnClickListener(v->{
            handleButton2();
        });

        SkinFactory.addSkinChangeListener(MainActivity.this);
        mRecyclerView = findViewById(R.id.recycler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerAdapter = new RecyclerAdapter(mListData);
        mRecyclerView.setAdapter(mRecyclerAdapter);

    }

    @Override
    protected void onDestroy() {
        SkinFactory.removeSkinChangeListener(MainActivity.this);
        super.onDestroy();
    }

    @Override
    public void onSkinChange() {
    }

    protected void changeSkin(){
        File skinFile = new File(Environment.getExternalStorageDirectory(), "SkinDemo/skin.apk");
        Log.d(TAG, "changeSkin:" + skinFile.getAbsolutePath());
        SkinEngine.getInstance().load(skinFile.getAbsolutePath()); //加载外部资源包
        SkinFactory.applyAllSkinViews(); //执行换肤操作
        SkinFactory.notifySkinListeners();
    }

    private void handleButton2(){
        Intent intent = new Intent(MainActivity.this,DisplayActivity.class);
        startActivity(intent);
    }


}
