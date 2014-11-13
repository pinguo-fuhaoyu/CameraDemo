package camera.camera360.com.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;


public class SimpleImageActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String tag = ImagePagerFragment.class.getName();//"ImagePagerFragment"
        Fragment fr = getSupportFragmentManager().findFragmentByTag(tag);//复用Fragment
        if (fr == null) {
            fr = new ImagePagerFragment();
            fr.setArguments(getIntent().getExtras());
        }
        int titleRes = R.string.ac_name_image_pager;

        setTitle(titleRes);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, tag).commit();

    }

}
