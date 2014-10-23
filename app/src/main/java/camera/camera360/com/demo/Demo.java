package camera.camera360.com.demo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;


public class Demo extends Activity implements  View.OnClickListener,MySurfaceView.PreviewReady,AdapterView.OnItemClickListener {
    boolean isClicked = false;
    MySurfaceView mSfv;
//    protected static final int MEMU_Reso = Menu.FIRST;
//    protected static final int MENU_Quit = Menu.FIRST+1;
    private static final int MSG_HIDE_SEEKBAR = 100;
    private SeekBar zoomBar = null;
    private int mCurZoomValue = 1;//设置变焦的值
    ListView mListView;
    LayoutInflater inflater;
    View mainView;

    //使用Handler携带信息，来设置zoomBar的隐藏与否
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_HIDE_SEEKBAR:
                    zoomBar.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        设置全屏显示，隐藏窗口所有装饰
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //setContentView(mySurfaceView);
        setContentView(R.layout.fragment_demo);

        //添加菜单按钮

        inflater = LayoutInflater.from(this);

        mainView = findViewById(R.id.mainview);
        ImageButton imageButton = (ImageButton)findViewById(R.id.shutter);
        mListView = (ListView)findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);
//        findViewById(R.id.shutter).setOnClickListener(this);
        imageButton.setOnClickListener(this);

        mSfv = (MySurfaceView)findViewById(R.id.sfv);
        mSfv.setClickable(true);
        mSfv.setOnClickListener(this);
        mSfv.setPreviewReady(this);

        //
        zoomBar = (SeekBar)findViewById(R.id.seekbar_zoom);
        //让mHandler携带消息
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_SEEKBAR,4 * 1000);//开始，计时多少毫秒
        //为zoomBar绑定OnSeekBarChangeListener方法
        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //设置zoom的值
                mCurZoomValue = i;
                mSfv.setZoom(i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //清空handler里携带的信息
                mHandler.removeMessages(MSG_HIDE_SEEKBAR);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //重新计时
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_SEEKBAR,2 * 1000);//开始
            }
        });
            //为快门按钮绑定长按事件
            imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //长按对焦
                mSfv.centerScreenFouce();
                return false;
            }
        });
        //为快门绑定OnTouchListener事件
        imageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    //按下时无反应
                    case MotionEvent.ACTION_DOWN:
                        //android framework
                        //onLongClick()
                        break;
                    //抬起时进行拍照
                    case MotionEvent.ACTION_UP:
                        mSfv.takePicture();
                        isClicked = true;
                        Toast.makeText(Demo.this,R.string.succeed,Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });


//        initRespectList();

    }


    private void initPopWindow(){

        //引入窗口配置文件
        View contentView = inflater.inflate(R.layout.popupwindow,null);
        contentView.setBackgroundColor(Color.WHITE);

        PopupWindow popupWindow = new PopupWindow(200,  WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(contentView);

        ListView listView = (ListView) contentView.findViewById(R.id.popuwindow);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ratios);
        listView.setAdapter(adapter);
        //获取焦点
        popupWindow.setFocusable(true);
        //锚点
        popupWindow.showAsDropDown(zoomBar,0,0);
    }


//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        //添加菜单按钮
//        menu.add(0, MEMU_Reso, 0, R.string.resolution).
//                setIcon(android.R.drawable.ic_menu_view);
//        menu.add(0, MENU_Quit, 0, R.string.quit).
//                setIcon(android.R.drawable.ic_menu_close_clear_cancel);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case MEMU_Reso:
//
//                break;
//            case MENU_Quit:
//                finish();
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        //返回true，自己处理按键事件
//        if(keyCode == KeyEvent.KEYCODE_MENU){
//            return super.onKeyUp(keyCode,event);
//        }
//        return true;
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            //按下减音量键时的判断语句
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //如果zoomBar此时是隐蔽的，则显示
                if(zoomBar.getVisibility() != View.VISIBLE){
                    zoomBar.setVisibility(View.VISIBLE);
                }
                //每按一次减音量键，mCurZoomValue的值-10
                //但是最小值不得小于1
                mCurZoomValue-=10;
                if(mCurZoomValue >=1){
                    //值>=1时，执行setZoom方法
                    mSfv.setZoom(mCurZoomValue);
                }else{
                    //小于1时赋值为1
                    mCurZoomValue = 1;
                }
                //zoomBar的值随之改变
                zoomBar.setProgress(mCurZoomValue);

                resetTimer();

               return true;
            //按下加音量键时的判断语句
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(zoomBar.getVisibility() != View.VISIBLE){
                    zoomBar.setVisibility(View.VISIBLE);
                }
                //每按一次加音量键，mCurZoomValue的值+10
                //但是最大值不得超过99
                mCurZoomValue+=10;
                if(mCurZoomValue <99){
                    //值小于99时，执行setZoom方法
                    mSfv.setZoom(mCurZoomValue);
                }else{
                    //超过99时赋值为99
                    mCurZoomValue = 99;
                }
                //zoomBar的值随之改变
                zoomBar.setProgress(mCurZoomValue);

                resetTimer();

                return true;
            case KeyEvent.KEYCODE_BACK:
                //点击返回键finish
                if(mListView.getVisibility() == View.VISIBLE){
                    mListView.setVisibility(View.GONE);
                }else{
                    this.finish();
                }
                return true;

            case KeyEvent.KEYCODE_MENU:
                //显示分辨率更改界面
//                initPopWindow();
                if(mListView.getVisibility() == View.VISIBLE){
                    mListView.setVisibility(View.GONE);
                }else{
                    mListView.setVisibility(View.VISIBLE);
                }
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    String[] ratios = new String[]{"1","11","111","1111","111111"};

    private void initRespectList() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ratios);

    }

    private void resetTimer(){
        //显示zoomBar之后，清空message
        mHandler.removeMessages(MSG_HIDE_SEEKBAR);
        //重新计时
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_SEEKBAR,2 * 1000);//开始，jishi
    }

    @Override
    public void onClick(View v) {
        Log.i("123", "onClick()");
        //根据ID判断触发事件
        switch (v.getId()){
            //当id为sfv时，显示zoomBar
            case R.id.sfv:
                zoomBar.setVisibility(View.VISIBLE);
                resetTimer();
                break;
//            case R.id.shutter:
//                //当id为shutter时,拍照
//                if(!isClicked) {
//                    mSfv.takePicture();
//                    isClicked = true;
//                    Toast.makeText(Demo.this,R.string.succeed,Toast.LENGTH_SHORT).show();
//                } else {
//                    mSfv.voerTack();
//                    isClicked = false;
//                }
//                break;
        }
    }

    @Override
    public void onPreviewReady() {
        //
        RepectAdapter adapter = new RepectAdapter(this);
        adapter.setData(mSfv.getPreviewSizeList());
        mListView.setAdapter(adapter);
    }

    //下面为分辨率变更

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.i("ooo ","onItemClick()");
        //TODO
        Rect rect = new Rect();
        mainView.getDrawingRect(rect);

//        if (0 == i) {
//            this.surfaceChanged(null, 0, rect.width(), rect.height());
//        } else {
        mSfv.setPreviewSize(i - 1, rect.width(), rect.height());
//        }
    }

}
