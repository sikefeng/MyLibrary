package com.library.pickerphoto.media.crop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;

import com.library.pickerphoto.R;
import com.library.pickerphoto.R2;
import com.library.pickerphoto.base.activities.BaseActivity;
import com.library.pickerphoto.media.config.SelectOptions;

import net.oschina.common.utils.StreamUtil;

import java.io.FileOutputStream;

import butterknife.OnClick;


public class CropActivity extends BaseActivity implements View.OnClickListener {
    private CropLayout mCropLayout;
    private static SelectOptions mOption;

    public static void show(Fragment fragment, SelectOptions options) {
        Intent intent = new Intent(fragment.getActivity(), CropActivity.class);
        mOption = options;
        fragment.startActivityForResult(intent, 0x04);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_crop;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        setTitle("");
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mCropLayout = (CropLayout) findViewById(R.id.cropLayout);
    }

    @Override
    protected void initData() {
        super.initData();

        String url = mOption.getSelectedImages().get(0);
        getImageLoader().load(url)
                .fitCenter()
                .into(mCropLayout.getImageView());

        mCropLayout.setCropWidth(mOption.getCropWidth());
        mCropLayout.setCropHeight(mOption.getCropHeight());
        mCropLayout.start();
    }

    @OnClick({R2.id.tv_crop, R2.id.tv_cancel})
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_crop) {
            Bitmap bitmap = null;
            FileOutputStream os = null;
            try {
                bitmap = mCropLayout.cropBitmap();
                String path = getFilesDir() + "/crop.jpg";
                os = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();

                Intent intent = new Intent();
                intent.putExtra("crop_path", path);
                setResult(RESULT_OK, intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bitmap != null) bitmap.recycle();
                StreamUtil.close(os);
            }
        } else if (i == R.id.tv_cancel) {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        mOption = null;
        super.onDestroy();
    }
}
