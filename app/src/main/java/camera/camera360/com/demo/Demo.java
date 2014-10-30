package camera.camera360.com.demo;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;


public class Demo extends Activity implements  CameraFragment.OnFragmentInteractionListener {

    CameraFragment cameraFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        设置全屏显示，隐藏窗口所有装饰
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //setContentView(mySurfaceView);
        setContentView(R.layout.layout_activity);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        cameraFragment = CameraFragment.newInstance("a","nb");
        transaction.add(R.id.fragment_container,cameraFragment);
        transaction.commit();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            //按下减音量键时的判断语句
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraFragment.onVolumeDownKey();
                return true;
            //按下加音量键时的判断语句
            case KeyEvent.KEYCODE_VOLUME_UP:

                cameraFragment.onVolumeUpKey();


                return true;
            case KeyEvent.KEYCODE_BACK:

                cameraFragment.onBackKey();


                return true;

            case KeyEvent.KEYCODE_MENU:
               cameraFragment.onMenuKey();
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
