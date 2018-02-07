package com.library.pickerphoto.media;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.library.pickerphoto.R;
import com.library.pickerphoto.R2;
import com.library.pickerphoto.base.activities.BaseActivity;
import com.library.pickerphoto.widgets.AppOperator;

import net.oschina.common.utils.BitmapUtil;
import net.oschina.common.utils.StreamUtil;
import net.oschina.common.widget.Loading;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 大图预览
 * Created by huanghaibin on 2017/9/27.
 */

public class LargeImageActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    @BindView(R2.id.imageView)
    SubsamplingScaleImageView mImageView;

    @SuppressWarnings("unused")
    @BindView(R2.id.iv_save)
    ImageView mImageSave;

    @BindView(R2.id.loading)
    Loading mLoading;

    private String mPath;

    public static void show(Context context, String image) {
        Intent intent = new Intent(context, LargeImageActivity.class);
        intent.putExtra("image", image);
        context.startActivity(intent);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_large_image;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mImageView.setMaxScale(15);
        mImageView.setZoomEnabled(true);
        mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
    }

    @Override
    protected void initData() {
        super.initData();
        mPath = getIntent().getStringExtra("image");
        getImageLoader()
                .load(mPath)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        if (isDestroyed())
                            return;
                        if (!mPath.startsWith("http")){
                            mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                        }
                        mImageView.setImage(ImageSource.uri(Uri.fromFile(resource)), new ImageViewState(1.0f,
                                new PointF(0, 0), 0));
                        mImageSave.setVisibility(View.VISIBLE);
                        mLoading.stop();
                        mLoading.setVisibility(View.GONE);
                    }
                });
    }

    private static final int PERMISSION_ID = 0x0001;

    @SuppressWarnings("unused")
    @AfterPermissionGranted(PERMISSION_ID)
    @OnClick(R2.id.iv_save)
    public void saveToFileByPermission() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, permissions)) {
            saveToFile();
//            new Thread(){
//                @Override
//                public void run() {
//                    super.run();
//                    byte[] buffer = new byte[BitmapUtil.DEFAULT_BUFFER_SIZE];
//                    BitmapFactory.Options options = BitmapUtil.createOptions();
//                    String savePath = Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg";
//                    Log.e("path", mPath + "  --   " +new File(mPath).length());
//                    Log.e("save", savePath);
//                    PicturesCompressor.compressImage(mPath, savePath, 3072 * 1024,
//                            80, 1280, 1280 * 16, null, options, true);
//                }
//            }.start();
        } else {
            EasyPermissions.requestPermissions(this, "请授予保存图片权限", PERMISSION_ID, permissions);
        }
    }

    private void saveToFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.gallery_save_file_not_have_external_storage, Toast.LENGTH_SHORT).show();
            return;
        }

        final Future<File> future = getImageLoader()
                .load(mPath)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

        AppOperator.runOnThread(new Runnable() {
            @Override
            public void run() {
                try {
                    File sourceFile = future.get();
                    if (sourceFile == null || !sourceFile.exists())
                        return;
                    String extension = BitmapUtil.getExtension(sourceFile.getAbsolutePath());
                    String extDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            .getAbsolutePath() + File.separator + "开源中国";
                    File extDirFile = new File(extDir);
                    if (!extDirFile.exists()) {
                        if (!extDirFile.mkdirs()) {
                            // If mk dir error
                            callSaveStatus(false, null);
                            return;
                        }
                    }
                    final File saveFile = new File(extDirFile, String.format("IMG_%s.%s", System.currentTimeMillis(), extension));
                    final boolean isSuccess = StreamUtil.copyFile(sourceFile, saveFile);
                    callSaveStatus(isSuccess, saveFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    callSaveStatus(false, null);
                }
            }
        });
    }

    private void callSaveStatus(final boolean success, final File savePath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    // notify
                    if (isDestroyed())
                        return;
                    Uri uri = Uri.fromFile(savePath);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    Toast.makeText(LargeImageActivity.this, R.string.gallery_save_file_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LargeImageActivity.this, R.string.gallery_save_file_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, R.string.gallery_save_file_not_have_external_storage_permission, Toast.LENGTH_SHORT).show();
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}
