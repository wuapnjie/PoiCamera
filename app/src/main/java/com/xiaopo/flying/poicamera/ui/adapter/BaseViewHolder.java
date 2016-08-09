package com.xiaopo.flying.poicamera.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by snowbean on 16-8-2.
 */

public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    protected abstract void loadData(T itemData);

    protected abstract void setOnItemClickListener(BaseRecyclerAdapter.OnItemClickListener<T> onItemClickListener,T itemData);

    protected Context getContext() {
        return itemView.getContext();
    }

}
