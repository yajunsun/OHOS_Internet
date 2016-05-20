package zgan.ohos.Activities;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import zgan.ohos.Dals.SuperMarketDal;
import zgan.ohos.Models.SuperMarketM;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.generalhelper;

public class CreditsDetail extends myBaseActivity {

    List<String> list;
    LinearLayout llcontent;
    int pageindex = 0;
    boolean isLoadingMore = false;
    int width=0;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_credits_detail);
        width= AppUtils.getWindowSize(this).x;
        llcontent = (LinearLayout) findViewById(R.id.ll_content);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toSetProgressText(getResources().getString(R.string.loading));
        toShowProgress();
        loadData();
    }

    protected void loadData() {
        isLoadingMore = false;
        list=new ArrayList<>();
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1023, String.format("@id=22,@account=%s",PreferenceUtil.getUserName()), "@22"), handler);
    }

    void bindData() {
       // ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getInteger(R.integer.supermarket_img_height));
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int maxheight=width*5;
        params.setMargins(0, 0, 0, 0);
        for (int i = 0; i < list.size(); i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setAdjustViewBounds(true);
            iv.setMaxWidth(width);
            iv.setMaxHeight(maxheight);
            ImageLoader.bindBitmap(list.get(i), iv, width, width);
            llcontent.addView(iv);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String[] results = frame.strData.split("\t");
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.v(TAG, frame.subCmd + "  " + ret);
                if (frame.subCmd == 40) {
                    String datastr = results[2];
                    if (results[0].equals("0") && results[1].equals("1023")) {
                        if (datastr.length() > 0) {
                            try {
                                JSONArray jsonArray = new JSONObject(datastr)
                                        .getJSONArray("data");
                                Log.i("suntest", datastr);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = (JSONObject) jsonArray.opt(i);
                                    String url = obj.get("pic_url").toString();
                                    list.add(url);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                bindData();
                            }
                        });
                    }
                }
                toCloseProgress();
            }
        }
    };

    @Override
    public void ViewClick(View v) {

    }
}
