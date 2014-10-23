package camera.camera360.com.demo;

import android.content.Context;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by gwfuhaoyu on 14-10-21.
 */
public class RepectAdapter extends BaseAdapter {

    private  List<Camera.Size> mDatas;
    LayoutInflater inflater;

    public RepectAdapter(Context ctx){
        inflater = LayoutInflater.from(ctx);
    }

    /**
     * 所有的分辨率
     * @param sizes
     */
    public void setData(List<Camera.Size> sizes){
        this.mDatas = sizes;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = (TextView) inflater.inflate(R.layout.resolution,null);
        Camera.Size size =  mDatas.get(i);
        String repect = size.width +" x " + size.height;
        textView.setText(repect);
        return textView;
    }
}
