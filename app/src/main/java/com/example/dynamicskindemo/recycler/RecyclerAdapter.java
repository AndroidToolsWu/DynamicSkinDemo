package com.example.dynamicskindemo.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dynamicskindemo.R;
import com.example.dynamicskindemo.skin.SkinFactory;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyHolder>{

    private List<String> mData;

    public RecyclerAdapter(List<String> data){
        mData = data;
    }

    @NonNull
    @Override
    public RecyclerAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        TextView mTextView;
        ImageView mImageView;

        private MyHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv);
            mImageView = itemView.findViewById(R.id.iv);
            SkinFactory.applySkin(mTextView);
            SkinFactory.applySkin(mImageView);
        }

        public void setData(String data){
            SkinFactory.applyRecyclerViewSkin(itemView);
            mTextView.setText(data);
        }

    }
}
