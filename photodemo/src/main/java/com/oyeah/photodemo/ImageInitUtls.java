package com.oyeah.photodemo;

import android.content.Context;

import com.lyl.galleryfinal.CoreConfig;
import com.lyl.galleryfinal.FunctionConfig;
import com.lyl.galleryfinal.GalleryFinal;
import com.lyl.galleryfinal.PauseOnScrollListener;
import com.lyl.galleryfinal.ThemeConfig;
import com.lyl.galleryfinal.model.PhotoInfo;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

/**
 * describe
 * authors liuyaolin
 * createTime 2017/5/4 10:01
 */

public class ImageInitUtls {

    //不带编辑界面，多选
    public static FunctionConfig imageInit(int maxSize, List<PhotoInfo> mPhotoList, Context context) {
        //主题（使用自定义）
        ThemeConfig theme = new ThemeConfig.Builder().build();
        FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
        //图片ImageLoader(使用Glide)
        com.lyl.galleryfinal.ImageLoader imageLoader = new GlideImageLoaderZhaoPian();
        PauseOnScrollListener pauseOnScrollListener = new GlidePauseOnScrollListener(false, true);

        functionConfigBuilder.setMutiSelectMaxSize(maxSize);
        //显示相机
        functionConfigBuilder.setEnableCamera(true);
        //显示预览
        functionConfigBuilder.setEnablePreview(true);
        functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        FunctionConfig functionConfig = functionConfigBuilder.build();


        CoreConfig coreConfig = new CoreConfig.Builder(context, imageLoader, theme)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .setNoAnimcation(false)//不关闭动画
                .build();
        GalleryFinal.init(coreConfig);
        initImageLoader(context);
        return functionConfig;
    }

    //预览图片
    public static void yulan(Context context, List<PhotoInfo> image_data, int index) {
        //主题（使用自定义）
        ThemeConfig theme = new ThemeConfig.Builder().build();
        FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
        //图片ImageLoader(使用Glide)
        com.lyl.galleryfinal.ImageLoader imageLoader = new GlideImageLoaderZhaoPian();
        PauseOnScrollListener pauseOnScrollListener = new GlidePauseOnScrollListener(false, true);

        //显示预览
        functionConfigBuilder.setEnablePreview(true);
        FunctionConfig functionConfig = functionConfigBuilder.build();
        CoreConfig coreConfig = new CoreConfig.Builder(context, imageLoader, theme)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .setNoAnimcation(false)//不关闭动画
                .build();
        GalleryFinal.init(coreConfig);
        GalleryFinal.openPreview(image_data, index);
    }

    //带编辑界面，单选
    public static FunctionConfig imageOneInit(List<PhotoInfo> mPhotoList, Context context) {
        //主题（使用自定义）
        ThemeConfig theme = new ThemeConfig.Builder().build();
        FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
        //图片ImageLoader(使用Glide)
        com.lyl.galleryfinal.ImageLoader imageLoader = new GlideImageLoaderZhaoPian();
        PauseOnScrollListener pauseOnScrollListener = new GlidePauseOnScrollListener(false, true);
        functionConfigBuilder.setEnableEdit(true);
        functionConfigBuilder.setEnableCrop(true);
        functionConfigBuilder.setEnableRotate(true);
        functionConfigBuilder.setCropSquare(true);
        functionConfigBuilder.setForceCrop(true);
        //显示相机
        functionConfigBuilder.setEnableCamera(true);
        //显示预览
        functionConfigBuilder.setEnablePreview(true);


        functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        FunctionConfig functionConfig = functionConfigBuilder.build();


        CoreConfig coreConfig = new CoreConfig.Builder(context, imageLoader, theme)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .setNoAnimcation(false)//不关闭动画
                .build();
        GalleryFinal.init(coreConfig);

        return functionConfig;
    }

    private static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

}
