package com.lyl.galleryfinal;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lyl.galleryfinal.adapter.PhotoPreviewAdapter;
import com.lyl.galleryfinal.model.PhotoInfo;
import com.lyl.galleryfinal.widget.GFViewPager;

import java.io.Serializable;
import java.util.List;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2015/12/29 0029 14:43
 */
public class PhotoPreviewActivity extends PhotoBaseActivity implements ViewPager.OnPageChangeListener {

    public static final String PHOTO_LIST = "photo_list";
    static final String PHOTO_INDEX = "index";

    private RelativeLayout mTitleBar;
    private ImageView mIvBack;
    private ImageView mIvDelete;
    private TextView mTvTitle;
    private TextView mTvIndicator;

    private GFViewPager mVpPager;
    private List<PhotoInfo> mPhotoList;
    private PhotoPreviewAdapter mPhotoPreviewAdapter;

    private ThemeConfig mThemeConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemeConfig = GalleryFinal.getGalleryTheme();

        if (mThemeConfig == null) {
            resultFailureDelayed(getString(R.string.please_reopen_gf), true);
        } else {
            setContentView(R.layout.gf_activity_photo_preview);
            findViews();
            setListener();
            setTheme();

            mPhotoList = (List<PhotoInfo>) getIntent().getSerializableExtra(PHOTO_LIST);
            mPhotoPreviewAdapter = new PhotoPreviewAdapter(this, mPhotoList);
            mVpPager.setAdapter(mPhotoPreviewAdapter);
            mVpPager.setCurrentItem(getIntent().getIntExtra(PHOTO_INDEX, 0));
        }
    }

    private void findViews() {
        mTitleBar = (RelativeLayout) findViewById(R.id.titlebar);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvDelete = (ImageView) findViewById(R.id.iv_delete);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvIndicator = (TextView) findViewById(R.id.tv_indicator);

        mVpPager = (GFViewPager) findViewById(R.id.vp_pager);
    }

    private void setListener() {
        mVpPager.addOnPageChangeListener(this);
        mIvBack.setOnClickListener(mBackListener);
        mIvDelete.setOnClickListener(mBackListener);
    }

    private void setTheme() {
        mIvBack.setImageResource(R.drawable.runter);
        if (mThemeConfig.getIconBack() == R.drawable.runter) {
            mIvBack.setColorFilter(mThemeConfig.getTitleBarIconColor());
        }

        mTitleBar.setBackgroundColor(0xff4dd0c8);
        mTvTitle.setTextColor(mThemeConfig.getTitleBarTextColor());
        if (mThemeConfig.getPreviewBg() != null) {
            mVpPager.setBackgroundDrawable(mThemeConfig.getPreviewBg());
        }
    }

    @Override
    protected void takeResult(PhotoInfo info) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mTvIndicator.setText((position + 1) + "/" + mPhotoList.size());
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private View.OnClickListener mBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_back) {
                onBackPressed();
            } else if (v.getId() == R.id.iv_delete) {
                mPhotoList.remove(mVpPager.getCurrentItem());
                if (mPhotoList.size() > 0) {
                    mPhotoPreviewAdapter = new PhotoPreviewAdapter(PhotoPreviewActivity.this, mPhotoList);
                    mVpPager.setAdapter(mPhotoPreviewAdapter);
                } else {
                    onBackPressed();
                }

            }
        }
    };

    @Override
    public void onBackPressed() {
        setResult(GalleryFinal.PERMISSIONS_CODE_GALLERY, getIntent().putExtra(PHOTO_LIST, (Serializable) mPhotoList));
        super.onBackPressed();
    }
}
