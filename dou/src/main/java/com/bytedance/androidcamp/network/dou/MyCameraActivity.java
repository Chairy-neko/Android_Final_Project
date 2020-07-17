package com.bytedance.androidcamp.network.dou;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    Camera mCamera;
    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    ImageView mImageView;
    VideoView mVideoView;
    MediaRecorder mMediaRecorder;

    Button btn_update;

    private String mp4Path;
    boolean isRecording = false;
    boolean isImage = false;
    boolean isVideo = false;
    Camera.PictureCallback mPictureCallback;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(IMiniDouyinService.BASE_URL)//设置网络请求的url地址
            .addConverterFactory(GsonConverterFactory.create())//设置数据解析器
            .build();
    private IMiniDouyinService miniDouyinService = retrofit.create(IMiniDouyinService.class);

    private Uri mSelectedVideo;
    public Uri mSelectedImage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_update:
                    startActivity(new Intent(MyCameraActivity.this, MainActivity.class));
                    return true;
                case R.id.item_camera:
                    mCamera.takePicture(null,null,mPictureCallback);
                    isImage = true;
                    if(isImage && isVideo){
                        btn_update.setVisibility(View.VISIBLE);
                    }
                    return true;
                case R.id.item_video:
                    if(isRecording) {
                        item.setIcon(R.mipmap.video);
                        item.setTitle("录制");
                    }
                    else{
                        item.setIcon(R.drawable.pause);
                        item.setTitle("停止");
                    }

                    record();
                    if(isImage && isVideo){
                        btn_update.setVisibility(View.VISIBLE);
                    }
                    return true;
                case R.id.item_back:
                    startActivity(new Intent(MyCameraActivity.this, AddActivity.class));
            }
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_camera);
        btn_update = findViewById(R.id.btn_update);
        mImageView = findViewById(R.id.iv_preview);
        mVideoView = findViewById(R.id.vv_preview);
        mSurfaceView = findViewById(R.id.sv);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        BottomNavigationView navigation = findViewById(R.id.navigation_my_camera);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initCamera();

        mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream fos = null;
                String filepath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+
                        File.separator+"1.jpg";
                File file = new File(filepath);
                try{
                    fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.flush();
                    Bitmap bitmap = BitmapFactory.decodeFile(filepath);
                    mImageView.setVisibility(View.VISIBLE);
                    mVideoView.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                    mSelectedImage = Uri.fromFile(file);//修改
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    mCamera.startPreview();
                    if(fos != null){
                        try{
                            fos.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postVideo();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera == null)
            initCamera();
        mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }

    private void initCamera(){
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.set("orientation","portrait");
        parameters.set("rotation",90);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    private void record(){
        if(isRecording){
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder=null;
            mCamera.lock();

            mVideoView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mVideoView.setVideoPath(mp4Path);
            mVideoView.start();
            isRecording=false;
            isVideo = true;
            File mp4file = new File(mp4Path);
            mSelectedVideo = Uri.fromFile(mp4file);//添加
        }else {
            if(prepareVideoRecoder()){
                isRecording=true;
                mMediaRecorder.start();
            }
        }
    }

    private boolean prepareVideoRecoder(){
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mp4Path = getOutputMediaPath();
        mMediaRecorder.setOutputFile(mp4Path);
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
        mMediaRecorder.setOrientationHint(90);
        try{
            mMediaRecorder.prepare();
        }catch (Exception e){
            mMediaRecorder.release();
            return false;
        }
        return true;
    }

    private String getOutputMediaPath(){
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir,"VIDEO_"+timestamp+".mp4");
        if(!mediaFile.exists()){
            mediaFile.getParentFile().mkdirs();
        }
        return mediaFile.getAbsolutePath();
    }

    private void postVideo() {
        btn_update.setText("上传中...");
        btn_update.setEnabled(false);
        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part videoPart = getMultipartFromUri("video", mSelectedVideo);
        miniDouyinService.postVideo("17396876307", "neko", coverImagePart, videoPart).enqueue(
                new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        if (response.body() != null) {
                            Toast.makeText(MyCameraActivity.this, response.body().toString(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                        btn_update.setText(R.string.update);
                        btn_update.setEnabled(true);
                        btn_update.setVisibility(View.INVISIBLE);
                        isImage = false;
                        isVideo = false;
                    }

                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
                        btn_update.setText(R.string.update);
                        btn_update.setEnabled(true);
                        btn_update.setVisibility(View.INVISIBLE);
                        isImage = false;
                        isVideo = false;
                        Toast.makeText(MyCameraActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        File f = new File(ResourceUtils.getRealPath(MyCameraActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(holder.getSurface() == null){
            return ;
        }
        //停止预览效果
        mCamera.stopPreview();
        //重新设置预览效果
        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
