package com.lyl.galleryfinal.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.lyl.galleryfinal.GalleryFinal;
import com.lyl.galleryfinal.R;
import com.lyl.galleryfinal.model.PhotoInfo;
import com.lyl.galleryfinal.widget.zoonview.PhotoView;

import java.util.List;

import cn.finalteam.toolsfinal.DeviceUtils;

/**
 * describe 预览界面适配
 * authors liuyaolin
 * createTime 2017/3/6 10:25
 */
public class PhotoPreviewAdapter extends ViewHolderRecyclingPagerAdapter<PhotoPreviewAdapter.PreviewViewHolder, PhotoInfo> {

    private Activity mActivity;
    private DisplayMetrics mDisplayMetrics;

    public PhotoPreviewAdapter(Activity activity, List<PhotoInfo> list) {
        super(activity, list);
        this.mActivity = activity;
        this.mDisplayMetrics = DeviceUtils.getScreenPix(mActivity);
    }

    @Override
    public PreviewViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = getLayoutInflater().inflate(R.layout.gf_adapter_preview_viewpgaer_item, null);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PreviewViewHolder holder, int position) {
        final PhotoInfo photoInfo = getDatas().get(position);
        String path = "";
        if (photoInfo != null) {
            path = photoInfo.getPhotoPath();
        }
        holder.mImageView.setImageResource(R.drawable.ic_gf_default_photo);
        if(path.contains("http://")){
            Glide.with(getContext())
                    .load(path)
                    .error(R.drawable.ic_gf_default_photo)
                    .placeholder(R.drawable.ic_gf_default_photo)
                    .into(holder.mImageView);
        }else{
            Drawable defaultDrawable = mActivity.getResources().getDrawable(R.drawable.ic_gf_default_photo);
            GalleryFinal.getCoreConfig().getImageLoader().displayImage(mActivity, path, holder.mImageView, defaultDrawable, mDisplayMetrics.widthPixels/2, mDisplayMetrics.heightPixels/2);
        }

    }

    static class PreviewViewHolder extends ViewHolderRecyclingPagerAdapter.ViewHolder{
        PhotoView mImageView;
        public PreviewViewHolder(View view) {
            super(view);
            mImageView = (PhotoView) view.findViewById(R.id.pv);
        }
    }
}
