package camera.camera360.com.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


@SuppressLint("NewApi")
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String LOG_TAG = "CameraPreviewSample";
    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";
    private int maxZoomValue = 0;
    private SurfaceHolder mHolder;
    protected Camera mCamera;
    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;
    protected Camera.Size mPreviewSize;
    protected Camera.Size mPictureSize;
    private int mCameraId = 0;
    private int mCenterPosX = -1;
    private int mCenterPosY;
    private String filePath = "/sdcard/";

    protected boolean mSurfaceConfiguring = false;
    private boolean mIsSupportZoom;

    public MySurfaceView(Context context) {
        this(context,null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initPreview();
    }

    private void initPreview() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        openCamera();

    }

    private void openCamera() {
        //判断当前手机API是不是支持的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(mCameraId);
        } else {
            mCamera = Camera.open();
        }
        //获取相机参数
        Camera.Parameters cameraParams = mCamera.getParameters();
        //获得支持的预览尺寸
        mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
        //获得支持的照片尺寸
        mPictureSizeList = cameraParams.getSupportedPictureSizes();
        mIsSupportZoom = isSupportZoom();

        Camera.Parameters parameters = mCamera.getParameters();
        //相机旋转90度
        parameters.setRotation(90);
        //设置参数
        mCamera.setParameters(parameters);
    }

    /**
     * 切换摄像头，暂时不使用
     * @param cameraId 0,后置；1，前置
     */
    public void changeCamera(int cameraId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (Camera.getNumberOfCameras() > cameraId) {
                mCameraId = cameraId;
            } else {
                mCameraId = 0;
            }
        } else {
            mCameraId = 0;
        }
        openCamera();
    }

    //预览窗口设置
    public void setPreviewSize(int index, int width, int height) {
        //停止预览之后才能设置
        stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        Camera.Size previewSize = mPreviewSizeList.get(index);
        Camera.Size pictureSize = determinePictureSize(previewSize);

        mPreviewSize = previewSize;
        mPictureSize = pictureSize;
        boolean layoutChanged = adjustSurfaceLayoutSize(previewSize, width, height);
        if (layoutChanged) {
            mSurfaceConfiguring = true;
            return;
        }
        //分辨率变换
        configureCameraParameters(cameraParams);
        try {
            startPreView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSurfaceConfiguring = false;
    }

    public List<Camera.Size> getSupportedPreivewSizes() {
        return mPreviewSizeList;
    }

    public void centerScreenFouce(){
        //屏幕中心自动对焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                //自动对焦提示
                Toast.makeText(getContext(),b?R.string.autofocus:R.string.autofocus_fail,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            //surface已经创建，可以用来呈现预览了
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //预览界面变换，先停止预览
        stopPreview();
        //获取参数
        Camera.Parameters cameraParams = mCamera.getParameters();
        //变换判断
        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            Log.d(LOG_TAG, "宽高 w: " + width     + ", h: " + height);
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, width, height);
            if (mSurfaceConfiguring) {
                return;
            }
        }

        configureCameraParameters(cameraParams);
        mSurfaceConfiguring = false;

        try {
            startPreView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Camera.Size determinePreviewSize(int reqWidth, int reqHeight) {
        int reqPreviewWidth = reqHeight;
        int reqPreviewHeight = reqWidth;


        float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : mPreviewSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);

            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
                Log.d(LOG_TAG, " reqRatio: " + reqRatio + ", curRatio: " + curRatio + ", deltaRatio: " + deltaRatio);
            }
        }
        return retSize;
    }

    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
        Camera.Size retSize = null;
        for (Camera.Size size : mPictureSizeList) {
            if (size.equals(previewSize)) {
                return size;
            }
        }


        float reqRatio = ((float) previewSize.width) / previewSize.height;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        for (Camera.Size size : mPictureSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }
    //布局尺寸变换
    private boolean adjustSurfaceLayoutSize(Camera.Size previewSize,int availableWidth, int availableHeight) {
        float tmpLayoutHeight = previewSize.width;
        float tmpLayoutWidth = previewSize.height;

        float factH, factW, fact;
        factH = availableHeight / tmpLayoutHeight;
        factW = availableWidth / tmpLayoutWidth;
        if (factH < factW) {
            fact = factH;
        } else {
            fact = factW;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);

        boolean layoutChanged;
        if ((layoutWidth != this.getWidth()) || (layoutHeight != this.getHeight())) {
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            if (mCenterPosX >= 0) {
                layoutParams.topMargin = mCenterPosY - (layoutHeight / 2);
                layoutParams.leftMargin = mCenterPosX - (layoutWidth / 2);
            }
            this.setLayoutParams(layoutParams);
            layoutChanged = true;
        } else {
            layoutChanged = false;
        }

        return layoutChanged;
    }


    public void setCenterPosition(int x, int y) {
        mCenterPosX = x;
        mCenterPosY = y;
    }
    //设置角度
    protected void configureCameraParameters(Camera.Parameters cameraParams) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
        } else {
            int angle;
            if(getContext() instanceof Activity){
                Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        angle = 90;
                        break;
                    case Surface.ROTATION_90:
                        angle = 0;
                        break;
                    case Surface.ROTATION_180:
                        angle = 270;
                        break;
                    case Surface.ROTATION_270:
                        angle = 180;
                        break;
                    default:
                        angle = 90;
                        break;
                }
                Log.v(LOG_TAG, "angle: " + angle);
                mCamera.setDisplayOrientation(angle);
            }
        }

        cameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        cameraParams.setPictureSize(mPictureSize.width, mPictureSize.height);

        mCamera.setParameters(cameraParams);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }


    /**
     * 释放相机
     */
    public void stop() {
        if (null == mCamera) {
            return;
        }
        //释放相机前先停止预览
        stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (null == mCamera) {
            return;
        }
        mCamera.stopPreview();
    }

    public void setOneShotPreviewCallback(PreviewCallback callback) {
        if (null == mCamera) {
            return;
        }
        mCamera.setOneShotPreviewCallback(callback);
    }

    public void setPreviewCallback(PreviewCallback callback) {
        if (null == mCamera) {
            return;
        }
        mCamera.setPreviewCallback(callback);
    }

    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    /**
     * 自动聚焦
     */
    public void autoFocus() {
        if (null != mCamera) {
            mCamera.autoFocus(null);
        }
    }

    public void takePicture() {
        mCamera.takePicture(null,null,jpeg);

    }


    public void startPreView() {
        if (null != mCamera) {
            mCamera.startPreview();
        }
    }


    /**
     * 得到相机的缩放程度
     *
     * @return
     */
    public int getMaxZoom() {
        int maxzoom = 0;
        if (null != mCamera) {
            Camera.Parameters parameters = mCamera.getParameters();
            maxzoom = parameters.getMaxZoom();
        }
        return maxzoom;
    }

    /**
     * 得到当前缩放
     *
     * @return
     */
    public int getCurrentZoom() {
        int zoom = 0;
        if (null != mCamera) {
            Camera.Parameters parameters = mCamera.getParameters();
            zoom = parameters.getZoom();
        }
        return zoom;
    }

    //判断是否是支持的参数
    public boolean isSupportZoom()
    {
        if (mCamera.getParameters().isSmoothZoomSupported()){
            return false;
        }else{
            maxZoomValue = mCamera.getParameters().getMaxZoom();
            return true;
        }
    }

    /**
     * 设置变焦的大小
     * @param zoomValue 当前需要变焦的值（1-99）
     */
    public void setZoom(int zoomValue){
        //变换
        zoomValue = maxZoomValue * zoomValue / 99;
        if (mIsSupportZoom && maxZoomValue!=0 ){
            try{
                Camera.Parameters params = mCamera.getParameters();
                if(zoomValue > maxZoomValue)
                    return;

                params.setZoom(zoomValue);
                mCamera.setParameters(params);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Log.i(LOG_TAG, "相机不支持变焦");
        }
    }

    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("ddd", "jpeg");
            final  Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        //存储的路径
                        String path  = filePath + "demo_" + System.currentTimeMillis() + ".jpg";
                        //输出流
                        File file = new File(path);
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        bos.flush();
                        //关闭输出流
                        bos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();


            try {
                mCamera.startPreview();
            }catch (Exception e){

            }
        }
    };
}
