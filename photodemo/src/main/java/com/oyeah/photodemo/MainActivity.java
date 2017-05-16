package com.oyeah.photodemo;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.lyl.galleryfinal.FunctionConfig;
import com.lyl.galleryfinal.GalleryFinal;
import com.lyl.galleryfinal.model.PhotoInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.gv_tupian)
    GridView gvTupian;
    private FunctionConfig functionConfig;
    private int maxSize = 8;//多选的张数
    private List<PhotoInfo> mPhotoList;//照片的数据源
    private FaBuGridView gv_fabu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
         /*
         * 照片
         */
        mPhotoList = new ArrayList<>();
        gv_fabu = new FaBuGridView();
        gvTupian.setAdapter(gv_fabu);
    }

    @OnClick(R.id.iv_add)
    public void onClick() {
        functionConfig = ImageInitUtls.imageInit(maxSize, mPhotoList, this);
        GalleryFinal.openGalleryMuti(1001, functionConfig, mOnHanlderResultCallback);
    }

    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                mPhotoList.clear();
                mPhotoList.addAll(resultList);
            }
            gv_fabu.notifyDataSetChanged();
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 照片网格布局adapter
     */
    class FaBuGridView extends BaseAdapter {

        @Override
        public int getCount() {
            return mPhotoList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {//说明是第一次，需要绘制
                convertView = getLayoutInflater().inflate(R.layout.item, null);
            }
            ImageView fabu_tupian = ViewHolder1.get(convertView, R.id.iv);
            if (mPhotoList.size() > 0) {
                try {
                    if (mPhotoList.get(position).getPhotoPath().contains("/")) {
                        fabu_tupian.setImageBitmap(Bimp.revitionImageSize(mPhotoList.get(position).getPhotoPath()));
                    } else {
                        fabu_tupian.setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.parseInt(mPhotoList.get(position).getPhotoPath())));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return convertView;
        }
    }
}
