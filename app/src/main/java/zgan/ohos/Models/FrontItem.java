package zgan.ohos.Models;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by Administrator on 16-4-21.
 */
public class FrontItem extends FuncBase implements Serializable {
    private String image_url;
    private int width = 0;
    @Override
    public String getview_title() {
        return view_title;
    }
    @Override
    public void setview_title(String view_title) {
        this.view_title = view_title;
    }

    public String getimage_url() {
        return image_url;
    }

    public void setimage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getwidth() {
        return width;
    }

    public void setwidth(String width) {
        try {
            this.width = Integer.parseInt(width);
        }
        catch (Exception e){
            Log.i("suntest","Invalid width:"+width);
        }
    }

    public int getheight() {
        return height;
    }

    public void setheight(String height) {
        try {
            this.height =Integer.valueOf( height);
        }
        catch (Exception e){Log.i("suntest","Invalid height:"+height);}
    }

    @Override
    public String gettype_id() {
        return type_id;
    }
    @Override
    public void settype_id(String type_id) {
        this.type_id = type_id;
    }
    @Override
    public String getpage_id() {
        return page_id;
    }
    @Override
    public void setpage_id(String page_id) {
        this.page_id = page_id;
    }

    private int height = 0;


    @Override
    public FrontItem getnewinstance() {
        return new FrontItem();
    }
}
