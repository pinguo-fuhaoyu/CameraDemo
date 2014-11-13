package camera.camera360.com.demo;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;

public class ImagePagerFragment extends Fragment {

    public static final int INDEX = 2;

    private Cursor mCursor;
    private DisplayImageOptions options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)//地址为空时显示的图片
                .showImageOnFail(R.drawable.ic_error)//图片加载/解码过程中错误显示的图片
                .resetViewBeforeLoading(true)//设置图片在下载前复位
                .cacheOnDisk(true)//设置图片缓存在SD卡中
                .imageScaleType(ImageScaleType.EXACTLY)//图片编码方式
                .bitmapConfig(Bitmap.Config.RGB_565)//图片的解码类型
                .considerExifParams(true)//考虑JPEG图像EXIF参数
                .displayer(new FadeInBitmapDisplayer(300))//图片加载好厚渐入的动画时间
                .build();//构建完成

        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {

        String[] proj = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE};
        String selecttions = MediaStore.Images.Media.DESCRIPTION + " = ?";
        String[] selectionArgs = {CameraFragment.NAME_SIGN};

        mCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, selecttions, selectionArgs, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_pager, container, false);
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new ImageAdapter());
        pager.setCurrentItem(getArguments().getInt(Constants.Extra.IMAGE_POSITION, 0));
        return rootView;
    }

    private class ImageAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        ImageAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);


            mCursor.moveToPosition(position);
            String path = mCursor.getString(1);

            File file = new File(path);
            Uri uri = Uri.fromFile(file);

            ImageLoader.getInstance().displayImage(uri.toString().trim(), imageView, options, new SimpleImageLoadingListener() {
                //图片加载时显示圆形进度条
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    spinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) { //获取图片失败类型
                        case IO_ERROR:  //I/O错误
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:    //解码错误
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:    //网络延迟错误
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:     //内存不足
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:   //未知错误
                            message = "Unknown error";
                            break;
                    }
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    //隐藏圆形进度条
                    spinner.setVisibility(View.GONE);
                }
                //隐藏圆形进度条
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    spinner.setVisibility(View.GONE);
                }
            });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
