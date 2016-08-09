package com.xiaopo.flying.poicamera.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by snowbean on 16-8-2.
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter {
    protected static final String TAG = BaseRecyclerAdapter.class.getSimpleName();

    protected List<T> mData;

    protected OnItemClickListener<T> mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public List<T> getData() {
        return mData;
    }

    public void refreshData(List<T> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void addData(List<T> data) {
        if (data == null) return;
        if (mData == null) {
            refreshData(data);
            return;
        }
        final int oldCount = getItemCount();
        mData.addAll(data);

        notifyItemInserted(oldCount);
    }

    protected abstract int getLayoutResId();

    protected abstract RecyclerView.ViewHolder createViewHolder(View itemView);

    protected abstract void bind(BaseViewHolder<T> holder, int position, T itemData);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(getLayoutResId(), parent, false);
        return createViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= mData.size()) return;
        T t = mData.get(position);
        if (t == null) return;
        //noinspection unchecked
        bind((BaseViewHolder<T>) holder, position, t);
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T t, int position);
    }
}