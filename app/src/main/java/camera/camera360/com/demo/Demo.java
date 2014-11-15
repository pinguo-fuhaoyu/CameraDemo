package camera.camera360.com.demo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;


public class Demo extends Activity implements  CameraFragment.OnFragmentInteractionListener {

    private CameraFragment mCameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏显示，隐藏窗口所有装饰
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //setContentView(mySurfaceView);
        setContentView(R.layout.layout_activity);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mCameraFragment = CameraFragment.newInstance();
        transaction.add(R.id.fragment_container, mCameraFragment);
        transaction.commit();
        initImageLoader(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            //按下减音量键时的判断语句
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mCameraFragment.onVolumeDownKey();
                return true;
            //按下加音量键时的判断语句
            case KeyEvent.KEYCODE_VOLUME_UP:
                mCameraFragment.onVolumeUpKey();
                return true;
            //按下后退键时的判断语句
            case KeyEvent.KEYCODE_BACK:
                mCameraFragment.onBackKey();
                return true;
            //按下菜单键时的判断语句
            case KeyEvent.KEYCODE_MENU:
               mCameraFragment.onMenuKey();
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)//设置线程的优先级
                .denyCacheImageMultipleSizesInMemory()//不同大小图片只有一个缓存，默认是多个
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())//设置缓存文件的名字
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)//后进先出
                .writeDebugLogs()
                .build();//开始构建

        ImageLoader.getInstance().init(config);//全局初始化此配置
    }
}
