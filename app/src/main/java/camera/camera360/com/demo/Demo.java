package camera.camera360.com.demo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;


public class Demo extends Activity implements  CameraFragment.OnFragmentInteractionListener {

    private CameraFragment mCameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        设置全屏显示，隐藏窗口所有装饰
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //setContentView(mySurfaceView);
        setContentView(R.layout.layout_activity);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mCameraFragment = CameraFragment.newInstance();
        transaction.add(R.id.fragment_container, mCameraFragment);
        transaction.commit();

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
}
