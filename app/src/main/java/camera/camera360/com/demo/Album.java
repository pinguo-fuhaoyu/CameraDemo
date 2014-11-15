package camera.camera360.com.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.io.File;

public class Album extends Activity {
    private GridView mPhotoView;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initOptions();
        initView();
        initData();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mPhotoView = (GridView)findViewById(R.id.photos_gridview);
        mPhotoView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //添加点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startImagePagerActivity(position);
            }
        });
    }

    /**
     * 初始化ImageLoader配置
     */
    private void initOptions() {
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub) //加载时显示的图片
                .showImageForEmptyUri(R.drawable.ic_empty)//Uri为空或错误时显示的图片
                .showImageOnFail(R.drawable.ic_error)//加载错误时显示的图片
                .cacheInMemory(true)//图片缓存到内存中
                .cacheOnDisk(true)//图片缓存到SD卡中
                .considerExifParams(true)//是否考虑JPEG图像EXIF参数
                .bitmapConfig(Bitmap.Config.RGB_565)//图片的解码类型
                .build();//构建完成
    }

    /**
     * 初始化数据
     */
     private void initData() {
        String[] proj = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE};
        String selecttions = MediaStore.Images.Media.DESCRIPTION + " = ?";
        String[] selectionArgs = {CameraFragment.NAME_SIGN};
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, selecttions, selectionArgs, null);
        mPhotoView.setAdapter(new PhotoCursorAdapter(this, cursor));
     }

     class PhotoCursorAdapter extends CursorAdapter {
            public PhotoCursorAdapter(Context context, Cursor cursor) {
                super(context, cursor);
         }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.photos_item, null);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //在对性能要求不高时可以使用此方法
            //如果对性能要求比较高，有很多图片的时候可以复用convertView来进行优化
            //使用ViewHolder模式,减少findview次数，把find出来的view引用保存进ViewhOlder，以便下次复用
            ImageView photoView = (ImageView) view.findViewById(R.id.photos_item_imageview);
            TextView titleView = (TextView) view.findViewById(R.id.photos_item_title);

            String path = cursor.getString(1);
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            ImageLoader.getInstance().displayImage(uri.toString().trim(), photoView, options);
            titleView.setText(cursor.getString(2));
        }
    }

    /**
     * 跳转到大图预览
     */
    protected void startImagePagerActivity(int position) {
        Intent intent = new Intent(this, SimpleImageActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImagePagerFragment.INDEX);
        intent.putExtra(Constants.Extra.IMAGE_POSITION, position);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
