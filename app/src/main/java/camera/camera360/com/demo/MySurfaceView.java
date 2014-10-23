package camera.camera360.com.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gwfuhaoyu on 14-10-11.
 */
@SuppressLint("NewApi")
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback,OnTouchListener {

    private static final  String TAG = "MySurfaceView";
    private SurfaceHolder holder;
    private Camera myCamera;
    private String filePath = "/sdcard/";
    private Boolean mIsSupportZoom;
    private int MAX = 0;
    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;
    protected Camera.Size mPreviewSize;
    protected Camera.Size mPictureSize;
    protected boolean mSurfaceConfiguring = false;
    private OrientationEventListener mOrientationListener;
    private LayoutMode mLayoutMode;
    private static boolean DEBUGGING = true;
    private static final String LOG_TAG = "CameraPreviewSample";
    private int mCenterPosX = -1;
    private int mCenterPosY;
    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";

    private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d("ddd", "shutter");
        }
    };
    private Camera.PictureCallback raw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("ddd", "raw");
        }
    };
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
                //
                myCamera.startPreview();
            }catch (Exception e){

            }
        }
    };

    /**
     * 摄像头打开，可以进行预览的回调接口
     */
    public interface PreviewReady{
        /**
         * 进行预览的回调方法
         */
        public void onPreviewReady();
    }

    private PreviewReady mPreviewReady;

    public void setPreviewReady(PreviewReady previewReady){
        this.mPreviewReady = previewReady;
    }

    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public int maxZoom(){
        return MAX;
    }

    private void init() {
        holder = getHolder();//获得surfaceHolder引用
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型
        this.setOnTouchListener(this);


    }

    public void takePicture() {
        myCamera.takePicture(null,null,jpeg);

    }
    public void voerTack() {
        myCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        myCamera = Camera.open();
        try {
            myCamera.setPreviewDisplay(holder);
            mIsSupportZoom = isSupportZoom();
            Camera.Parameters params = myCamera.getParameters();
            MAX = params.getMaxZoom();
            Log.i("qqq", "MAX = " + MAX);

            Camera.Parameters parameters = myCamera.getParameters();//获得相机参数

            mPreviewSizeList = parameters.getSupportedPreviewSizes();
            mPictureSizeList = parameters.getSupportedPictureSizes();

            mPreviewReady.onPreviewReady();


            //设置预览窗口
            Camera.Size cs = mPreviewSizeList.get(0);
            parameters.setPreviewSize(cs.width,cs.height);
            parameters.setPictureSize(cs.width,cs.height);
            myCamera.setParameters(parameters);
            //预览窗口旋转90度
            myCamera.setDisplayOrientation(90);

            myCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Camera.Size> getPreviewSizeList(){
        return mPreviewSizeList;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


//        Camera.Parameters parameters = myCamera.getParameters();//获得相机参数
//        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();//获取预览的各种分辨率
//        //设置预览窗口
//        Camera.Size cs = sizes.get(0);
//        parameters.setPreviewSize(cs.width,cs.height);
//        parameters.setPictureSize(cs.width,cs.height);
//        myCamera.setParameters(parameters);
//        //预览窗口旋转90度
//        myCamera.setDisplayOrientation(90);
//
//        //开启预览
//        myCamera.startPreview();

        stopPreview();
        Camera.Parameters cameraParams = myCamera.getParameters();
        boolean portrait = isPortrait();

        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            Log.d(TAG, "宽高 w: " + width     + ", h: " + height);
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            if (mSurfaceConfiguring) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

        try {
            startPreView();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected Camera.Size determinePreviewSize(boolean portrait, int reqWidth, int reqHeight) {

        int reqPreviewWidth;
        int reqPreviewHeight;
        if (portrait) {
            reqPreviewWidth = reqHeight;
            reqPreviewHeight = reqWidth;
        } else {
            reqPreviewWidth = reqWidth;
            reqPreviewHeight = reqHeight;
        }

        // Adjust surface size with the closest aspect-ratio
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        return super.onTouchEvent(event);
    }

    public boolean onTouch(android.view.View view, android.view.MotionEvent motionEvent){
        Log.i("123","onTouch() motionEvent.getAction() = " + motionEvent.getAction());
        //当按键抬起时，进行手动聚焦
        if(motionEvent.getAction()==MotionEvent.ACTION_UP){
            Log.i("1111","ACTION_UP");
            touchToFouce(motionEvent);
        }
        return false;
    }


    public void centerScreenFouce(){
        //屏幕中心自动对焦
        myCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                //自动对焦提示
                Toast.makeText(getContext(),b?R.string.autofocus:R.string.autofocus_fail,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void touchToFouce(MotionEvent event) {
        if (myCamera != null) {
            //取消自动对焦
            myCamera.cancelAutoFocus();
            Rect focusRect = calculateTapArea(this, event.getX(), event.getY(), 1f);

            Camera.Parameters parameters = myCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            ArrayList<Camera.Area> list = new ArrayList<Camera.Area>();
            list.add(new Camera.Area(focusRect, 1000));
            //触发对焦
            parameters.setFocusAreas(list);

            ArrayList<Camera.Area> meteringList = new ArrayList<Camera.Area>();
            list.add(new Camera.Area(focusRect, 1000));
               parameters.setMeteringAreas(meteringList);

          //  myCamera.setParameters(parameters);
            myCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {

                }
            });
        }

    }

    public Rect calculateTapArea(SurfaceView view,float x, float y, float coefficient) {
        int areaSize = Float.valueOf(50 * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, view.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, view.getHeight() - areaSize);

        android.graphics.RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }


    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }


    //判断是否是支持的参数
    public boolean isSupportZoom()
    {
        boolean isSuppport = true;
        if (myCamera.getParameters().isSmoothZoomSupported())
        {
            isSuppport = false;
        }
        return isSuppport;
    }

    /**
     * 设置变焦的大小
     * @param zoomValue 当前需要变焦的值（1-99）
     */
    public void setZoom(int zoomValue){
        //变换
        zoomValue = MAX * zoomValue / 99;
        if (mIsSupportZoom && MAX!=0 ){
            try{
                Camera.Parameters params = myCamera.getParameters();
                if(zoomValue > MAX)
                     return;

                params.setZoom(zoomValue);
                myCamera.setParameters(params);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Log.i(TAG, "--------the phone not support zoom");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        myCamera.stopPreview();
        myCamera.release();
        myCamera = null;
    }

    public void setPreviewSize(int index, int width, int height) {
        stopPreview();

        Camera.Parameters cameraParams = myCamera.getParameters();
        boolean portrait = isPortrait();

        Camera.Size previewSize = mPreviewSizeList.get(index);
        Camera.Size pictureSize = determinePictureSize(previewSize);

        Log.d(TAG, "preview - w: " + previewSize.width + ", h: " + previewSize.height);

        mPreviewSize = previewSize;
        mPictureSize = pictureSize;
        boolean layoutChanged = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
        if (layoutChanged) {
            mSurfaceConfiguring = true;
            return;
        }

        configureCameraParameters(cameraParams, portrait);
        try {
            startPreView();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        mSurfaceConfiguring = false;
    }

    public void stopPreview() {
        if (null == myCamera) {
            return;
        }
        myCamera.stopPreview();
    }
    public boolean isPortrait() {
        return (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
        Camera.Size retSize = null;
        for (Camera.Size size : mPictureSizeList) {
            if (size.equals(previewSize)) {
                return size;
            }
        }


        // if the preview size is not supported as a picture size
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

    protected boolean adjustSurfaceLayoutSize(Camera.Size previewSize, boolean portrait,
                                              int availableWidth, int availableHeight) {
        float tmpLayoutHeight, tmpLayoutWidth;
        if (portrait) {
            tmpLayoutHeight = previewSize.width;
            tmpLayoutWidth = previewSize.height;
        } else {
            tmpLayoutHeight = previewSize.height;
            tmpLayoutWidth = previewSize.width;
        }

        float factH, factW, fact;
        factH = availableHeight / tmpLayoutHeight;
        factW = availableWidth / tmpLayoutWidth;
        if (mLayoutMode == LayoutMode.FitToParent) {

            if (factH < factW) {
                fact = factH;
            } else {
                fact = factW;
            }
        } else {
            if (factH < factW) {
                fact = factW;
            } else {
                fact = factH;
            }
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);
        if (DEBUGGING) {
            Log.v(LOG_TAG, "预览 Size - w: " + layoutWidth + ", h: " + layoutHeight);
            Log.v(LOG_TAG, "缩放: " + fact);
        }

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

    protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
            if (portrait) {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
            } else {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
            }
        } else { //  2.2
            int angle;
            Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
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
            myCamera.setDisplayOrientation(angle);
        }

        cameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        cameraParams.setPictureSize(mPictureSize.width, mPictureSize.height);
        if (DEBUGGING) {
            Log.v(LOG_TAG, "Preview Actual Size - w: " + mPreviewSize.width + ", h: " + mPreviewSize.height);
            Log.v(LOG_TAG, "Picture Actual Size - w: " + mPictureSize.width + ", h: " + mPictureSize.height);
        }


        myCamera.setParameters(cameraParams);
    }


    public void startPreView() {
        if (null != myCamera) {
            myCamera.startPreview();
        }
    }
    public static enum LayoutMode {
        FitToParent,
        NoBlank
    };
}
