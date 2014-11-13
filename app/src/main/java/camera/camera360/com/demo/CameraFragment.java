package camera.camera360.com.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;


public class CameraFragment extends Fragment implements  View.OnClickListener,AdapterView.OnItemClickListener  {
    private OnFragmentInteractionListener mListener;
    private boolean mIsClicked = false;
    private MySurfaceView mSfv;
    private static final int MSG_HIDE_SEEKBAR = 100;
    private SeekBar mZoomBar = null;
    private int mCurZoomValue = 1;//设置变焦的值
    private ListView mListView;
    private View mainView;
    private ImageButton mImageButton;
    private ImageView mAlbum;
    public static final String NAME_SIGN = "mydemo";
    /**
     * 使用Handle携带信息控制变焦条的隐藏和显示
     */
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_HIDE_SEEKBAR:
                    mZoomBar.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    public CameraFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demo, container, false);
        initView(view);
        initListener();
    return view;
    }

    private void initView(View view) {

        mainView = view.findViewById(R.id.mainview);
        mImageButton = (ImageButton)view.findViewById(R.id.shutter);
        mListView = (ListView)view.findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);
//        findViewById(R.id.shutter).setOnClickListener(this);
        mImageButton.setOnClickListener(this);
        mSfv = (MySurfaceView)view.findViewById(R.id.sfv);
        mSfv.setClickable(true);
        mSfv.setOnClickListener(this);
        mZoomBar = (SeekBar)view.findViewById(R.id.seekbar_zoom);
        ResolutionAdapter adapter = new ResolutionAdapter(getActivity());
        adapter.setData(mSfv.getSupportedPreivewSizes());
        mListView.setAdapter(adapter);

        mAlbum = (ImageView)view.findViewById(R.id.photo_imageview);
    }

    private void initListener() {
        //让mHandler携带消息
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_SEEKBAR,4 * 1000);//开始，计时多少毫秒
        //为zoomBar绑定OnSeekBarChangeListener方法
        mZoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_SEEKBAR, 2 * 1000);//开始
            }
        });
        //为相册绑定事件监听
        mAlbum.setOnClickListener(this);

        //为快门按钮绑定长按事件
        mImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //长按对焦
                mSfv.centerScreenFouce();
                return false;
            }
        });

        //为快门绑定OnTouchListener事件
        mImageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    //按下时无反应
                    case MotionEvent.ACTION_DOWN:
                        //android framework
                        //onLongClick()
                        break;
                    //抬起时进行拍照
                    case MotionEvent.ACTION_UP:
                        mSfv.takePicture();
                        mIsClicked = true;
                        Toast.makeText(getActivity(), R.string.succeed, Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        //根据ID判断触发事件
        switch (v.getId()){
            //当id为sfv时，显示zoomBar
            case R.id.sfv:
                mZoomBar.setVisibility(View.VISIBLE);
                resetTimer();
                break;
            case R.id.photo_imageview:
                toAlbum();
        }
    }

    //跳转到相册
    public void toAlbum() {
        Intent intent = new Intent();
        intent.setClass(getActivity(),Album.class);
        startActivity(intent);
    }

    private void resetTimer(){
        //显示zoomBar之后，清空message
        mHandler.removeMessages(MSG_HIDE_SEEKBAR);
        //重新计时
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_SEEKBAR,2 * 1000);//开始计时
    }

    //下面为分辨率变更
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Rect rect = new Rect();
        mainView.getDrawingRect(rect);
        mSfv.setPreviewSize(i, rect.width(), rect.height());
    }

    /**
     * 按下音量减时减少变焦
     */
    public void onVolumeDownKey(){
        //如果zoomBar此时是隐蔽的，则显示
        if(mZoomBar.getVisibility() != View.VISIBLE){
            mZoomBar.setVisibility(View.VISIBLE);
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
        mZoomBar.setProgress(mCurZoomValue);

        resetTimer();
    }

    /**
     * 按下音量加时增加变焦
     */
    public void onVolumeUpKey(){
        if(mZoomBar.getVisibility() != View.VISIBLE){
            mZoomBar.setVisibility(View.VISIBLE);
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
        mZoomBar.setProgress(mCurZoomValue);

        resetTimer();
    }

    /**
     * 按下返回键时的处理
     */
    public void onBackKey(){
        //点击返回键finish
        if(mListView.getVisibility() == View.VISIBLE){
            mListView.setVisibility(View.GONE);
        }else{
            getActivity().finish();
        }

    }

    /**
     * 按下菜单键时控制菜单的显示与隐藏
     */
    public void onMenuKey(){
        //显示分辨率更改界面
//                initPopWindow();
        if(mListView.getVisibility() == View.VISIBLE){
            mListView.setVisibility(View.GONE);
        }else{
            mListView.setVisibility(View.VISIBLE);
        }
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //清理Handler信息
        mHandler.removeMessages(MSG_HIDE_SEEKBAR);
        mHandler = null;
    }
}
