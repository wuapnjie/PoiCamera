package com.xiaopo.flying.poicamera.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaopo.flying.poicamera.R;
import com.xiaopo.flying.poicamera.datatype.Filter;

import butterknife.BindView;

/**
 * Created by snowbean on 16-8-2.
 */

public class FilterPreviewAdapter extends BaseRecyclerAdapter<Filter> {

    @Override
    protected int getLayoutResId() {
        return R.layout.item_filter_preview;
    }

    @Override
    protected BaseViewHolder createViewHolder(View itemView) {
        return new FilterPreViewViewHolder(itemView);
    }

    @Override
    protected void bind(BaseViewHolder<Filter> holder, int position, Filter itemData) {
        holder.loadData(itemData);
        holder.setOnItemClickListener(mOnItemClickListener, itemData);
    }


    public static class FilterPreViewViewHolder extends BaseViewHolder<Filter> {
        @BindView(R.id.iv_filter_preview)
        ImageView mIvFilterPreview;
        @BindView(R.id.tv_filter_name)
        TextView mTvFilterName;
        @BindView(R.id.item_container)
        LinearLayout mItemContainer;

        public FilterPreViewViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void loadData(Filter itemData) {
            mTvFilterName.setText(itemData.getName());
            mIvFilterPreview.setImageResource(itemData.getFilterType().obtainPreviewImage());
        }

        @Override
        protected void setOnItemClickListener(final OnItemClickListener<Filter> onItemClickListener, final Filter itemData) {
            mItemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemData, getAdapterPosition());
                    }
                }
            });
        }
    }
}
