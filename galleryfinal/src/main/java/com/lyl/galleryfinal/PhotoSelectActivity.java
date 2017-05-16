/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.lyl.galleryfinal;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lyl.galleryfinal.adapter.FolderListAdapter;
import com.lyl.galleryfinal.adapter.PhotoListAdapter;
import com.lyl.galleryfinal.model.PhotoFolderInfo;
import com.lyl.galleryfinal.model.PhotoInfo;
import com.lyl.galleryfinal.permission.AfterPermissionGranted;
import com.lyl.galleryfinal.permission.EasyPermissions;
import com.lyl.galleryfinal.utils.PhotoTools;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.finalteam.toolsfinal.DeviceUtils;
import cn.finalteam.toolsfinal.StringUtils;
import cn.finalteam.toolsfinal.io.FilenameUtils;

/**
 * Desction:图片选择器
 * Author:pengjianbo
 * Date:15/10/10 下午3:54
 */
public class  PhotoSelectActivity extends PhotoBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    private Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    private final int HANLDER_TAKE_PHOTO_EVENT = 1000;
    private final int HANDLER_REFRESH_LIST_EVENT = 1002;

    private GridView mGvPhotoList;
    private ListView mLvFolderList;
    private LinearLayout mLlFolderPanel;
    private ImageView mIvTakePhoto;
    private TextView mIvBack;
    //    private ImageView mIvClear;
    private TextView mIvPreView;
    private TextView mTvChooseCount;
    private TextView mTvSubTitle;
    private LinearLayout mLlTitle;
    private LinearLayout mFabOk;
    private TextView mTvEmptyView;
    private RelativeLayout mTitlebar;
    private TextView mTvTitle;
    private ImageView mIvFolderArrow;

    private List<PhotoFolderInfo> mAllPhotoFolderList;
    private FolderListAdapter mFolderListAdapter;

    private List<PhotoInfo> mCurPhotoList;
    private PhotoListAdapter mPhotoListAdapter;

    //是否需要刷新相册
    private boolean mHasRefreshGallery = false;
    private List<PhotoInfo> mSelectPhotoList = new ArrayList<>();
    private LinearLayout ok_button;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selectPhotoMap", (Serializable) mSelectPhotoList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectPhotoList = (ArrayList<PhotoInfo>) getIntent().getSerializableExtra("selectPhotoMap");
    }

    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANLDER_TAKE_PHOTO_EVENT) {
                PhotoInfo photoInfo = (PhotoInfo) msg.obj;
                takeRefreshGallery(photoInfo);
                refreshSelectCount();
            } else if (msg.what == HANDLER_REFRESH_LIST_EVENT) {
                refreshSelectCount();
                mPhotoListAdapter.notifyDataSetChanged();
                mFolderListAdapter.notifyDataSetChanged();
                if (mAllPhotoFolderList.get(0).getPhotoList() == null ||
                        mAllPhotoFolderList.get(0).getPhotoList().size() == 0) {
                    mTvEmptyView.setText(R.string.no_photo);
                }

                mGvPhotoList.setEnabled(true);
                mLlTitle.setEnabled(true);
                mIvTakePhoto.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GalleryFinal.getFunctionConfig() == null || GalleryFinal.getGalleryTheme() == null) {
            resultFailureDelayed(getString(R.string.please_reopen_gf), true);
        } else {
            setContentView(R.layout.gf_activity_photo_select);
            mPhotoTargetFolder = null;

            findViews();
            setListener();

            mAllPhotoFolderList = new ArrayList<>();
            mFolderListAdapter = new FolderListAdapter(this, mAllPhotoFolderList, GalleryFinal.getFunctionConfig());
            mLvFolderList.setAdapter(mFolderListAdapter);

            mCurPhotoList = new ArrayList<>();
            mPhotoListAdapter = new PhotoListAdapter(this, mCurPhotoList, mSelectPhotoList, mScreenWidth);
            mGvPhotoList.setAdapter(mPhotoListAdapter);

            if (GalleryFinal.getFunctionConfig().isMutiSelect()) {
                mTvChooseCount.setVisibility(View.VISIBLE);
                mFabOk.setVisibility(View.VISIBLE);
            }

            setTheme();
            mGvPhotoList.setEmptyView(mTvEmptyView);

            if (GalleryFinal.getFunctionConfig().isCamera()) {
                mIvTakePhoto.setVisibility(View.VISIBLE);
            } else {
                mIvTakePhoto.setVisibility(View.GONE);
            }

            refreshSelectCount();
            requestGalleryPermission();

            mGvPhotoList.setOnScrollListener(GalleryFinal.getCoreConfig().getPauseOnScrollListener());
        }

        Global.mPhotoSelectActivity = this;
    }

    private void setTheme() {
//        mIvBack.setImageResource(GalleryFinal.getGalleryTheme().getIconBack());
//        if (GalleryFinal.getGalleryTheme().getIconBack() == R.drawable.ic_gf_back) {
//            mIvBack.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
//        }

//        mIvFolderArrow.setImageResource(GalleryFinal.getGalleryTheme().getIconFolderArrow());
//        if (GalleryFinal.getGalleryTheme().getIconFolderArrow() == R.drawable.ic_gf_triangle_arrow) {
//            mIvFolderArrow.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
//        }

//        mIvClear.setImageResource(GalleryFinal.getGalleryTheme().getIconClear());
//        if (GalleryFinal.getGalleryTheme().getIconClear() == R.drawable.ic_gf_clear) {
//            mIvClear.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
//        }

//        mIvPreView.setImageResource(GalleryFinal.getGalleryTheme().getIconPreview());
//        if (GalleryFinal.getGalleryTheme().getIconPreview() == R.drawable.ic_gf_preview) {
//            mIvPreView.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
//        }

//        mIvTakePhoto.setImageResource(GalleryFinal.getGalleryTheme().getIconCamera());
//        if (GalleryFinal.getGalleryTheme().getIconCamera() == R.drawable.ic_gf_camera) {
//            mIvTakePhoto.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
//        }
//        mFabOk.setIcon(GalleryFinal.getGalleryTheme().getIconFab());

//        mTitlebar.setBackgroundColor(GalleryFinal.getGalleryTheme().getTitleBarBgColor());
//        mTvSubTitle.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
//        mTvTitle.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
//        mTvChooseCount.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
//        mFabOk.setColorPressed(GalleryFinal.getGalleryTheme().getFabPressedColor());
//        mFabOk.setColorNormal(GalleryFinal.getGalleryTheme().getFabNornalColor());
    }

    private void findViews() {
        mGvPhotoList = (GridView) findViewById(R.id.gv_photo_list);
        mLvFolderList = (ListView) findViewById(R.id.lv_folder_list);
        mTvSubTitle = (TextView) findViewById(R.id.tv_sub_title);
        mLlFolderPanel = (LinearLayout) findViewById(R.id.ll_folder_panel);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        mTvChooseCount = (TextView) findViewById(R.id.tv_choose_count);
        mIvBack = (TextView) findViewById(R.id.iv_back);
        mFabOk = (LinearLayout) findViewById(R.id.fab_ok);
        mTvEmptyView = (TextView) findViewById(R.id.tv_empty_view);
        mLlTitle = (LinearLayout) findViewById(R.id.ll_title);
//        mIvClear = (ImageView) findViewById(R.id.iv_clear);
        mTitlebar = (RelativeLayout) findViewById(R.id.titlebar);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mIvFolderArrow = (ImageView) findViewById(R.id.iv_folder_arrow);
        mIvPreView = (TextView) findViewById(R.id.iv_preview);
    }

    private void setListener() {
        mLlTitle.setOnClickListener(this);
        mIvTakePhoto.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvFolderArrow.setOnClickListener(this);

        mLvFolderList.setOnItemClickListener(this);
        mGvPhotoList.setOnItemClickListener(this);
        mFabOk.setOnClickListener(this);
//        mIvClear.setOnClickListener(this);
        mIvPreView.setOnClickListener(this);
    }

    protected void deleteSelect(int photoId) {
        try {
            for (Iterator<PhotoInfo> iterator = mSelectPhotoList.iterator(); iterator.hasNext(); ) {
                PhotoInfo info = iterator.next();
                if (info != null && info.getPhotoId() == photoId) {
                    iterator.remove();
                    break;
                }
            }
        } catch (Exception e) {
        }

        refreshAdapter();
    }

    private void refreshAdapter() {
        mHanlder.sendEmptyMessageDelayed(HANDLER_REFRESH_LIST_EVENT, 100);
    }

    protected void takeRefreshGallery(PhotoInfo photoInfo, boolean selected) {
        if (isFinishing() || photoInfo == null) {
            return;
        }

        Message message = mHanlder.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;
        mSelectPhotoList.add(photoInfo);
        mHanlder.sendMessageDelayed(message, 100);
    }

    /**
     * 解决在5.0手机上刷新Gallery问题，从startActivityForResult回到Activity把数据添加到集合中然后理解跳转到下一个页面，
     * adapter的getCount与list.size不一致，所以我这里用了延迟刷新数据
     *
     * @param photoInfo
     */
    private void takeRefreshGallery(PhotoInfo photoInfo) {
        mCurPhotoList.add(0, photoInfo);
        mPhotoListAdapter.notifyDataSetChanged();

        //添加到集合中
        List<PhotoInfo> photoInfoList = mAllPhotoFolderList.get(0).getPhotoList();
        if (photoInfoList == null) {
            photoInfoList = new ArrayList<>();
        }
        photoInfoList.add(0, photoInfo);
        mAllPhotoFolderList.get(0).setPhotoList(photoInfoList);

        if (mFolderListAdapter.getSelectFolder() != null) {
            PhotoFolderInfo photoFolderInfo = mFolderListAdapter.getSelectFolder();
            List<PhotoInfo> list = photoFolderInfo.getPhotoList();
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(0, photoInfo);
            if (list.size() == 1) {
                photoFolderInfo.setCoverPhoto(photoInfo);
            }
            mFolderListAdapter.getSelectFolder().setPhotoList(list);
        } else {
            String folderA = new File(photoInfo.getPhotoPath()).getParent();
            for (int i = 1; i < mAllPhotoFolderList.size(); i++) {
                PhotoFolderInfo folderInfo = mAllPhotoFolderList.get(i);
                String folderB = null;
                if (!StringUtils.isEmpty(photoInfo.getPhotoPath())) {
                    folderB = new File(photoInfo.getPhotoPath()).getParent();
                }
                if (TextUtils.equals(folderA, folderB)) {
                    List<PhotoInfo> list = folderInfo.getPhotoList();
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(0, photoInfo);
                    folderInfo.setPhotoList(list);
                    if (list.size() == 1) {
                        folderInfo.setCoverPhoto(photoInfo);
                    }
                }
            }
        }

        mFolderListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void takeResult(PhotoInfo photoInfo) {

        Message message = mHanlder.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;

        if (!GalleryFinal.getFunctionConfig().isMutiSelect()) { //单选
            mSelectPhotoList.clear();
            mSelectPhotoList.add(photoInfo);

            if (GalleryFinal.getFunctionConfig().isEditPhoto()) {//裁剪
                mHasRefreshGallery = true;
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<>();
                list.add(photoInfo);
                resultData(list);
            }

            mHanlder.sendMessageDelayed(message, 100);
        } else {//多选
            mSelectPhotoList.add(photoInfo);
            mHanlder.sendMessageDelayed(message, 100);
        }
    }

    /**
     * 执行裁剪
     */
    protected void toPhotoEdit() {
        Intent intent = new Intent(this, PhotoEditActivity.class);
        intent.putExtra(PhotoEditActivity.SELECT_MAP, (Serializable) mSelectPhotoList);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_title || id == R.id.iv_folder_arrow) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
                mLlFolderPanel.setVisibility(View.GONE);
                mLlFolderPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.gf_flip_horizontal_out));
            } else {
                mLlFolderPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.gf_flip_horizontal_in));
                mLlFolderPanel.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.iv_take_photo) {
            //判断是否达到多选最大数量
            if (GalleryFinal.getFunctionConfig().isMutiSelect() && mSelectPhotoList.size() == GalleryFinal.getFunctionConfig().getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            }

            if (!DeviceUtils.existSDCard()) {
                toast(getString(R.string.empty_sdcard));
                return;
            }
            requestPermission(1, Manifest.permission.CAMERA, new Runnable() {
                @Override
                public void run() {
                    takePhotoAction();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(PhotoSelectActivity.this)
                            .setTitle("拍照功能未开启")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    if (Build.VERSION.SDK_INT >= 9) {
                                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                                    } else if (Build.VERSION.SDK_INT <= 8) {
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                                        intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
                                    }
                                    startActivity(intent);
                                }
                            }).create().show();

                }
            });

        } else if (id == R.id.iv_back) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
                mLlTitle.performClick();
            } else {
                finish();
            }
        } else if (id == R.id.fab_ok) {
            if (mSelectPhotoList.size() > 0) {
                if (!GalleryFinal.getFunctionConfig().isEditPhoto()) {
                    resultData((ArrayList<PhotoInfo>) mSelectPhotoList);
                } else {
                    toPhotoEdit();
                }
            }
        } else if (id == R.id.iv_preview) {
            Intent intent = new Intent(this, PhotoPreviewActivity.class);
            intent.putExtra(PhotoPreviewActivity.PHOTO_LIST, (Serializable) mSelectPhotoList);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.lv_folder_list) {
            folderItemClick(position);
        } else {
            photoItemClick(view, position);
        }
    }

    private void folderItemClick(int position) {
        mLlFolderPanel.setVisibility(View.GONE);
        mCurPhotoList.clear();
        PhotoFolderInfo photoFolderInfo = mAllPhotoFolderList.get(position);
        if (photoFolderInfo.getPhotoList() != null) {
            mCurPhotoList.addAll(photoFolderInfo.getPhotoList());
        }
        mPhotoListAdapter.notifyDataSetChanged();

        if (position == 0) {
            mPhotoTargetFolder = null;
        } else {
            PhotoInfo photoInfo = photoFolderInfo.getCoverPhoto();
            if (photoInfo != null && !StringUtils.isEmpty(photoInfo.getPhotoPath())) {
                mPhotoTargetFolder = new File(photoInfo.getPhotoPath()).getParent();
            } else {
                mPhotoTargetFolder = null;
            }
        }
        mTvSubTitle.setText(photoFolderInfo.getFolderName());
        mFolderListAdapter.setSelectFolder(photoFolderInfo);
        mFolderListAdapter.notifyDataSetChanged();

        if (mCurPhotoList.size() == 0) {
            mTvEmptyView.setText(R.string.no_photo);
        }
    }

    private void photoItemClick(View view, int position) {
        PhotoInfo info = mCurPhotoList.get(position);
        if (!GalleryFinal.getFunctionConfig().isMutiSelect()) {
            mSelectPhotoList.clear();
            mSelectPhotoList.add(info);
            String ext = FilenameUtils.getExtension(info.getPhotoPath());
            if (GalleryFinal.getFunctionConfig().isEditPhoto() && (ext.equalsIgnoreCase("png")
                    || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))) {
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<>();
                list.add(info);
                resultData(list);
            }
            return;
        }
        boolean checked = false;
        if (!mSelectPhotoList.contains(info)) {
            if (GalleryFinal.getFunctionConfig().isMutiSelect() && mSelectPhotoList.size() == GalleryFinal.getFunctionConfig().getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            } else {
                mSelectPhotoList.add(info);
                checked = true;
            }
        } else {
            try {
                for (Iterator<PhotoInfo> iterator = mSelectPhotoList.iterator(); iterator.hasNext(); ) {
                    PhotoInfo pi = iterator.next();
                    if (pi != null && TextUtils.equals(pi.getPhotoPath(), info.getPhotoPath())) {
                        iterator.remove();
                        break;
                    }
                }
            } catch (Exception e) {
            }
            checked = false;
        }
        refreshSelectCount();

        PhotoListAdapter.PhotoViewHolder holder = (PhotoListAdapter.PhotoViewHolder) view.getTag();
        if (holder != null) {
            if (checked) {
                holder.mIvCheck.setImageResource(R.drawable.yeser);
            } else {
                holder.mIvCheck.setImageResource(Color.alpha(0));
            }
        } else {
            mPhotoListAdapter.notifyDataSetChanged();
        }
    }

    public void refreshSelectCount() {
        mTvChooseCount.setText(mSelectPhotoList.size() + "");
//        if ( mSelectPhotoList.size() > 0 && GalleryFinal.getFunctionConfig().isMutiSelect() ) {
//        mIvClear.setVisibility(View.VISIBLE);
//        } else {
//            mIvClear.setVisibility(View.GONE);
//        }

        if (GalleryFinal.getFunctionConfig().isEnablePreview()) {
            mIvPreView.setVisibility(View.VISIBLE);
        } else {
            mIvPreView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPermissionsGranted(List<String> list) {
        getPhotos();
    }

    @Override
    public void onPermissionsDenied(List<String> list) {
        mTvEmptyView.setText(R.string.permissions_denied_tips);
//        mIvTakePhoto.setVisibility(View.GONE);
    }

    /**
     * 获取所有图片
     */
    @AfterPermissionGranted(GalleryFinal.PERMISSIONS_CODE_GALLERY)
    private void requestGalleryPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            getPhotos();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_tips_gallery),
                    GalleryFinal.PERMISSIONS_CODE_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void getPhotos() {
        mTvEmptyView.setText(R.string.waiting);
        mGvPhotoList.setEnabled(false);
        mLlTitle.setEnabled(false);
        mIvTakePhoto.setEnabled(false);
        new Thread() {
            @Override
            public void run() {
                super.run();

                mAllPhotoFolderList.clear();
                List<PhotoFolderInfo> allFolderList = PhotoTools.getAllPhotoFolder(PhotoSelectActivity.this, mSelectPhotoList);
                mAllPhotoFolderList.addAll(allFolderList);

                mCurPhotoList.clear();
                if (allFolderList.size() > 0) {
                    if (allFolderList.get(0).getPhotoList() != null) {
                        mCurPhotoList.addAll(allFolderList.get(0).getPhotoList());
                    }
                }

                refreshAdapter();
            }
        }.start();
    }

    /**
     * 请求权限
     *
     * @param id                   请求授权的id 唯一标识即可
     * @param permission           请求的权限
     * @param allowableRunnable    同意授权后的操作
     * @param disallowableRunnable 禁止权限后的操作
     */
    protected void requestPermission(int id, String permission, Runnable allowableRunnable, Runnable disallowableRunnable) {
        if (allowableRunnable == null) {
            throw new IllegalArgumentException("allowableRunnable == null");
        }

        allowablePermissionRunnables.put(id, allowableRunnable);
        if (disallowableRunnable != null) {
            disallowablePermissionRunnables.put(id, disallowableRunnable);
        }

        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {
            //减少是否拥有权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //弹出对话框接收权限
                ActivityCompat.requestPermissions(PhotoSelectActivity.this, new String[]{permission}, id);
                return;
            } else {
                allowableRunnable.run();
            }
        } else {
            allowableRunnable.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Runnable allowRun = allowablePermissionRunnables.get(requestCode);
            allowRun.run();
        } else {
            Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
            disallowRun.run();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
                mLlTitle.performClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasRefreshGallery) {
            mHasRefreshGallery = false;
            requestGalleryPermission();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (GalleryFinal.getCoreConfig() != null &&
                GalleryFinal.getCoreConfig().getImageLoader() != null) {
            GalleryFinal.getCoreConfig().getImageLoader().clearMemoryCache();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoTargetFolder = null;
        if(mSelectPhotoList!=null){
            mSelectPhotoList.clear();
        }
        System.gc();
    }
}
