package com.xiaopo.flying.poicamera.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;

import com.xiaopo.flying.poicamera.R;
import com.xiaopo.flying.poicamera.datatype.Filter;
import com.xiaopo.flying.poicamera.filter.FilterType;
import com.xiaopo.flying.poicamera.ui.adapter.BaseRecyclerAdapter;
import com.xiaopo.flying.poicamera.ui.adapter.FilterPreviewAdapter;
import com.xiaopo.flying.poicamera.ui.custom.TranslateFrameLayout;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class ProcessActivity extends AppCompatActivity {
    public static final String INTENT_KEY_PATH = "path";
    private static final String TAG = "ProcessActivity";

    @BindView(R.id.gpu_image_view)
    GPUImageView mGpuImageView;
    @BindView(R.id.filter_preview_list)
    RecyclerView mFilterPreviewList;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.translate_layout)
    TranslateFrameLayout mTranslateLayout;
    @BindView(R.id.activity_process)
    LinearLayout mActivityProcess;


    private String mPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        ButterKnife.bind(this);

        mPath = getIntent().getStringExtra(INTENT_KEY_PATH);

        init();
    }

    private void init() {
        mGpuImageView.setImage(new File(mPath));

        mTabLayout.addTab(mTabLayout.newTab().setText("滤镜"));

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                if (position == 0) {
                    mTranslateLayout.dismissOrShowBottomView();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                if (position == 0) {
                    mTranslateLayout.dismissOrShowBottomView();
                }
            }
        });

        mFilterPreviewList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        FilterPreviewAdapter adapter = new FilterPreviewAdapter();
        mFilterPreviewList.setAdapter(adapter);


        adapter.refreshData(FilterType.obtainAllFilter(this));

        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener<Filter>() {
            @Override
            public void onItemClick(Filter filter, int position) {
                changeFilter(filter);
            }
        });
    }

    private void changeFilter(Filter filter) {
        if (filter==null){
            Log.e(TAG, "changeFilter: the filter is null");
            return;
        }
        mGpuImageView.setFilter(filter.getImageFilter(this));
        mGpuImageView.requestRender();

    }

}
