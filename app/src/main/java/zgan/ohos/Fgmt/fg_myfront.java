package zgan.ohos.Fgmt;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import zgan.ohos.Activities.AdvertiseDetail;
import zgan.ohos.Activities.BindDevice;
import zgan.ohos.Activities.Express_in;
import zgan.ohos.Activities.Express_out;
import zgan.ohos.Activities.Login;
import zgan.ohos.Activities.MessageActivity;
import zgan.ohos.Activities.SMSearchResult;
import zgan.ohos.Activities.SuperMarket;
import zgan.ohos.ConstomControls.ScrollViewWithCallBack;
import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.Dals.AdvertiseDal;
import zgan.ohos.Dals.FrontItemDal;
import zgan.ohos.Models.Advertise;
import zgan.ohos.Models.FrontItem;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * Created by yajunsun on 16-2-24.
 * 首页fragment
 */
public class fg_myfront extends myBaseFragment implements View.OnClickListener {
    LinearLayout ll_shequgrid;
    //RecyclerView rv_grid;
    LinearLayout ll_shequhuodong;
    AlertDialog opendialog;
    ScrollViewWithCallBack sscontent;
    ViewPager adv_pager, func_pager;
    LinearLayout pager_ind, func_ind;
    LinearLayoutManager gridItemLayoutmanger;
    static final int ADSINDEX = 0;
    static final int TOSTMSG = 10;
    boolean isContinue = true;
    List<ImageView> imageViews = new ArrayList<>();
    List<ImageView> funcimageViews = new ArrayList<>();
    private AtomicInteger what = new AtomicInteger(0);
    List<FrontItem> funcPages;
    ImageView iv_gridtitle;
    List<FrontItem> frontItems1;
    List<FrontItem> frontItems2;
    List<Advertise> advertises;
    //FuncPageDal funcPageDal;
    AdvertiseDal advertiseDal;
    FrontItemDal frontItemDal;
    Calendar lastOpent;
    Calendar thisOpen;
    Calendar lastCall;
    Calendar thisCall;
    ImageView iv_bottom1, iv_bottom2, iv_bottom3;
    Point p;
    TextView txt_xiaoqu;
    IconicsImageView ivsearchicon;
    boolean LOAD_SUCCESS = false;
    private Handler handler;
    Timer timer;
    LayoutInflater mLayoutInflater;
    int mTimeOut = 5000;
    int mItemHeight = 0;
    View code2d;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_my_front, container, false);
        frontItems1 = new ArrayList<>();
        frontItems2 = new ArrayList<>();
        //funcPageDal = new FuncPageDal();
        frontItemDal = new FrontItemDal();
        advertiseDal = new AdvertiseDal();
        iniHandler();
        initView(view);
        initNetData();
        initDialog();
        Log.i(TAG, "fg_myfront view created");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (handler == null) {
            iniHandler();
        }
        isStoped = false;
        //initNetData();
    }

    boolean isStoped = false;

    @Override
    public void onStop() {
        super.onStop();
        isStoped = true;
//        if (handler != null) {
//            handler.removeCallbacks(null);
//            handler = null;
//        }

    }

    //获取页面数据
    private void initNetData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, String.format("LOAD_SUCCESS=%s,isStoped=%s", LOAD_SUCCESS, isStoped));
                if (!LOAD_SUCCESS && !isStoped) {
                    while (!SystemUtils.getIsCommunityLogin()) {
                        try {
                            if (mTimeOut <= 0) {
                                break;
                            }
                            Thread.sleep(100);
                            mTimeOut -= 100;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (SystemUtils.getIsCommunityLogin()) {
                        Log.i(TAG, "小区云登陆成功开始拉取数据！!");
                        //用户信息（地址、积分等）
                        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_USERINFO, String.format("@id=22,@account=%s", PreferenceUtil.getUserName()), "22"), handler);
                        //ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_USERINFO, "@id=22", "22"), handler);
                        //顶部滚动广告
                        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_ADVER, "@id=22", "22"), handler);
                        //功能区
                        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_FUNCPAGE, "@id=22", "22"), handler);
                        //专题内容1
                        //ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_FRONTITMES1, "@id=22", "22"), handler);
                        //专题内容2
                        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_FRONTITMES2, "@id=22", "22"), handler);
                    } else {
                        Log.i(TAG, "连接网络超时，请退出后重新打开应用~");
                        Message msg = handler.obtainMessage();
                        msg.what = TOSTMSG;
                        msg.obj = "连接网络超时，请退出后重新打开应用~";
                        msg.sendToTarget();
                    }
                }

            }
        }).start();
    }

    private void initDialog() {
        /*****打开单元门时发现未绑定室内机则提示*****/
        final AlertDialog.Builder openbuilder = new AlertDialog.Builder(getActivity());
        openbuilder.setTitle("您的手机号码还没有与室内机绑定，是否现在绑定？");
        openbuilder.setPositiveButton("是的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), BindDevice.class);
                startActivityWithAnim(getActivity(), intent);
                opendialog.dismiss();
            }
        });
        openbuilder.setCancelable(true);
        openbuilder.setNegativeButton("下次绑定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                opendialog.dismiss();
            }
        });
        opendialog = openbuilder.create();
    }

    private void initView(View v) {
        p = AppUtils.getWindowSize(getActivity());
        txt_xiaoqu = (TextView) v.findViewById(R.id.txt_xiaoqu);
        ivsearchicon = (IconicsImageView) v.findViewById(R.id.iv_searchicon);
        ivsearchicon.setOnClickListener(this);
        sscontent = (ScrollViewWithCallBack) v.findViewById(R.id.ll_content);
        ll_shequhuodong = (LinearLayout) v.findViewById(R.id.ll_shequhuodong);
        adv_pager = (ViewPager) v.findViewById(R.id.adv_pager);
        pager_ind = (LinearLayout) v.findViewById(R.id.pager_ind);
        iv_bottom1 = (ImageView) v.findViewById(R.id.iv_bottom1);
        iv_bottom2 = (ImageView) v.findViewById(R.id.iv_bottom2);
        iv_bottom3 = (ImageView) v.findViewById(R.id.iv_bottom3);

        //功能区
        func_pager = (ViewPager) v.findViewById(R.id.func_pager);
        func_ind = (LinearLayout) v.findViewById(R.id.func_ind);
        //3+1表格功能区
//        iv_gridtitle = (ImageView) v.findViewById(R.id.iv_gridtitle);
//        ll_shequgrid = (LinearLayout) v.findViewById(R.id.ll_shequgrid);
        //rv_grid = (RecyclerView) v.findViewById(R.id.rv_grid);

        //二维码扫描
        code2d = v.findViewById(R.id.iv_code2d);
        code2d.setOnClickListener(this);
    }

    //加载功能区
    private void loadFuncData() {
        List<View> views = new ArrayList<>();
        int funcCount = funcPages.size();
        int pageCount = 0;
        if (funcCount % 10 != 0)
            pageCount = funcCount / 10 + 1;
        else pageCount = funcCount / 10;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        for (int i = 0; i < pageCount; i++) {
            List<FrontItem> fps = new ArrayList<>();
            if (funcCount > 10 && i < pageCount - 1) {
                int b = i * 10;
                int e = (i + 1) * 10;
                for (int j = b; j < e; j++) {
                    fps.add(funcPages.get(j));
                }
            } else if (funcCount <= 10) {
                for (int j = 0; j < funcCount; j++) {
                    fps.add(funcPages.get(j));
                }
            } else if (i == pageCount - 1) {
                int l = funcCount % 10;
                for (int j = funcCount - l; j < funcCount; j++) {
                    fps.add(funcPages.get(j));
                }
            }
            RecyclerView funcV = new RecyclerView(getActivity());
            funcV.setLayoutParams(params);
            funcV.setLayoutManager(new GridLayoutManager(getActivity(), 5));
            funcV.setAdapter(new funcAdapter(fps));
            views.add(funcV);
            ImageView simg = new ImageView(getActivity());
            simg.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
            simg.setPadding(5, 5, 5, 5);
            if (i == 0)
                simg.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(30));
            else {
                simg.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.LTGRAY).sizeDp(30));
            }
            funcimageViews.add(simg);
            func_ind.addView(simg);
        }
        func_pager.setAdapter(new

                AdvAdapter(views)

        );
        func_pager.setOnPageChangeListener(new

                funcPageChangeListener()

        );
        adv_pager.setOnTouchListener(new View.OnTouchListener() {
                                         @Override
                                         public boolean onTouch(View v, MotionEvent event) {
                                             switch (event.getAction()) {
                                                 case MotionEvent.ACTION_DOWN:
                                                 case MotionEvent.ACTION_MOVE:
                                                     return true;
                                                 case MotionEvent.ACTION_UP:
                                                     return false;
                                                 default:
                                                     return false;
                                             }
                                         }
                                     }
        );
    }

    //功能区点击处理事件
    class funcClick implements View.OnClickListener {
        FrontItem func;

        public funcClick(FrontItem _func) {
            func = _func;
        }

        @Override
        public void onClick(View v) {
            try {
                if (func.gettype_id().equals("2011")) {
                    if (SystemUtils.getIsLogin())
                        //第一次开门
                        if (lastOpent == null) {
                            communityOpt(20, String.format("%s\t%s", PreferenceUtil.getUserName(), PreferenceUtil.getSID()));
                            lastOpent = Calendar.getInstance();
                        } else {
                            thisOpen = Calendar.getInstance();
                            long span = thisOpen.getTimeInMillis() - lastOpent.getTimeInMillis();
                            //判断上次点击开门和本次点击开门时间间隔是否大于5秒钟
                            if (span > 5000) {
                                communityOpt(20, String.format("%s\t%s", PreferenceUtil.getUserName(), PreferenceUtil.getSID()));
                                lastOpent = Calendar.getInstance();
                            } else {
                                generalhelper.ToastShow(getActivity(), "请在" + ((5000 - span) / 1000 + 1) + "秒后操作");
                            }
                        }
                    else {
                        startActivityIfLogin(null, resultCodes.REMOTEOPEN);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setAction("Page." + func.gettype_id());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("item", func);
                    intent.putExtras(bundle);
                    if (isActionInstalled(intent))
                        startActivityIfLogin(intent, 0);
                    else {
                        //generalhelper.ToastShow(getActivity(), "即将上线~");
                        intent = new Intent(getActivity(), SuperMarket.class);
                        startActivityWithAnim(getActivity(), intent);
                    }
                }
            } catch (ActivityNotFoundException anfe) {
                Intent intent = new Intent(getActivity(), SuperMarket.class);
                startActivityWithAnim(getActivity(), intent);
//                generalhelper.ToastShow(getActivity(), "即将上线~");
//                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //加载活动专区
    /*private void loadGridItems() {
        List<List<FrontItem>> set = new ArrayList<>();
        gridItemLayoutmanger = new LinearLayoutManager(getActivity());
        rv_grid.setLayoutManager(gridItemLayoutmanger);
        int itemcount = frontItems1.size();
        int lines = itemcount / 3;
        if (itemcount == lines * 3 + 1) {
            for (int i = 0; i < lines; i++) {
                List<FrontItem> frontItems = new ArrayList<>();
                frontItems.add(frontItems1.get(i * 3 + 1));
                frontItems.add(frontItems1.get(i * 3 + 2));
                frontItems.add(frontItems1.get(i * 3 + 3));
                set.add(frontItems);
            }
            ImageLoader.bindBitmap(frontItems1.get(0).getimage_url(), iv_gridtitle);
        }
        rv_grid.setAdapter(new gridItemAdapter(set));
//        int defaultheight = (int) (AppUtils.getDensity(getActivity()) * 100);
//        int height=Math.max(defaultheight,frontItems1.get(1).getheight())*lines;
        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        //params.addRule(RelativeLayout.BELOW,R.id.ll_messages);
        ll_shequgrid.setMinimumHeight(120);
        rv_grid.setMinimumHeight(100);
    }*/
//加载超市购首页项目
    private void loadSqhdData() {
        int int_marginTop = getResources().getInteger(R.integer.front_item_marginTop);
        int marginTop = (int) (AppUtils.getDensity(getActivity()) * int_marginTop);
        int height = 5 * p.x;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (final FrontItem item : frontItems2) {
            ImageView iv = new ImageView(getActivity());
            iv.setLayoutParams(params);
            iv.setPadding(0, 0, 0, marginTop);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setAdjustViewBounds(true);
            iv.setMaxWidth(p.x);
            iv.setMaxHeight(height);
            ImageLoader.bindBitmap(item.getimage_url(), iv, p.x, p.x);
            iv.setOnClickListener(new goodsClick(item));
            ll_shequhuodong.addView(iv);
        }
        ll_shequhuodong.setMinimumHeight(0);
    }

    //超市购项点击处理事件
    class goodsClick implements View.OnClickListener {
        FrontItem item;

        public goodsClick(FrontItem _item) {
            item = _item;
        }

        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent();
                intent.setAction("Page." + item.gettype_id());
                Bundle bundle = new Bundle();
                bundle.putSerializable("item", item);
                intent.putExtras(bundle);
                if (isActionInstalled(intent))
                    startActivityIfLogin(intent, 0);
                else {
                    //generalhelper.ToastShow(getActivity(), "即将上线~");
//                    intent=new Intent(getActivity(), SuperMarket.class);
//                    startActivityWithAnim(getActivity(), intent);
                }
            } catch (ActivityNotFoundException anfe) {
                Intent intent = new Intent(getActivity(), SuperMarket.class);
                startActivityWithAnim(getActivity(), intent);
//                generalhelper.ToastShow(getActivity(), "即将上线~");
//                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //加载顶部广告
    private void loadGuanggaoData() {
        if (advertises != null) {
            List<View> advPics = new ArrayList<>();
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            for (int i = 0; i < advertises.size(); i++) {
                ImageView img = new ImageView(getActivity());
                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                //取消点击功能
                //img.setOnClickListener(new adverClick(advertises.get(i)));
                ImageLoader.bindBitmap(advertises.get(i).getpic_url(), img, 500, 200);
                advPics.add(img);
                ImageView simg = new ImageView(getActivity());
                simg.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
                simg.setPadding(5, 5, 5, 5);
                if (i == 0)
                    simg.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(30));
                else
                    simg.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.LTGRAY).sizeDp(30));
                imageViews.add(simg);
                pager_ind.addView(simg);
            }
            adv_pager.setAdapter(new AdvAdapter(advPics));
            adv_pager.setOnPageChangeListener(new GuidePageChangeListener());
            adv_pager.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                            isContinue = false;
                            break;
                        case MotionEvent.ACTION_UP:
                            isContinue = true;
                            break;
                        default:
                            isContinue = true;
                            break;
                    }
                    return false;
                }
            });
            timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isContinue) {
                        Message msg = new Message();
                        msg.what = ADSINDEX;
                        msg.arg1 = what.get();
                        handler.sendMessage(msg);
                        whatOption();
                    }
                }
            }, 0, 2000);
        }
    }

    //广告点击处理事件
    class adverClick implements View.OnClickListener {
        Advertise advertise;

        public adverClick(Advertise _adv) {
            advertise = _adv;
        }

        @Override
        public void onClick(View v) {
            Intent intent;
            intent = new Intent(getActivity(), AdvertiseDetail.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("advertise", advertise);
            intent.putExtras(bundle);
            startActivityWithAnim(getActivity(), intent);
        }
    }

    //提示对话框弹出
    private void communityOpt(int cmd, String data) {
        //if (SystemUtils.getSID().equals("")) {
        //if (PreferenceUtil.getSID().equals("")) {
        if (!SystemUtils.getIsCommunityLogin()) {
            opendialog.show();
        } else {
//            ZganLoginService.toGetServerData(
//                    20, 0,
//                    String.format("%s\t%s", PreferenceUtil.getUserName(), PreferenceUtil.getSID()), handler);//A0000003
            ZganCommunityService.toGetServerData(
                    cmd, 0,
                    data, handler);//A0000003
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_searchicon) {
            Intent intent = new Intent(getActivity(), SMSearchResult.class);
            startActivity(intent);
        } else if (view.getId() == R.id.iv_code2d) {
            Intent intent = new Intent(getActivity(), CaptureActivity.class);
            startActivityForResult(intent, resultCodes.CODE2DSCAN);
        }
/*        if (!SystemUtils.getIsCommunityLogin()) {
            opendialog.show();
        } else {
            ViewClick(view);
        }
    }*/
    }

    //数据处理handler
    private void iniHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Frame frame = (Frame) msg.obj;
                    String ret = generalhelper.getSocketeStringResult(frame.strData);
                    Log.i(TAG, frame.subCmd + "  " + ret);
                    if (frame.subCmd == 20) {
                        if (ret.equals("0"))
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "门开了", Snackbar.LENGTH_LONG).show();
                        else
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "门没开", Snackbar.LENGTH_LONG).show();
                    } else if (frame.subCmd == 40) {
                        String[] results = frame.strData.split("\t");
                        if (results[0].equals("0")) {
                            LOAD_SUCCESS = true;
                            String datastr = results[2];
                            if (results[1].equals(AppUtils.P_FUNCPAGE)) {
                                if (datastr.length() > 0) {
                                    funcPages = frontItemDal.getList(datastr);
                                    loadFuncData();
                                    if (frame.platform != 0) {
                                        addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_FUNCPAGE, "@id=22", "22"), frame.strData);
                                    }
                                }
                            } else if (results[1].equals(AppUtils.P_FRONTITMES1)) {
                                if (datastr.length() > 0) {
                                    frontItems1 = frontItemDal.getList(datastr);
                                    //loadGridItems();
                                    if (frame.platform != 0) {
                                        addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_FRONTITMES1, "@id=22", "22"), frame.strData);
                                    }
                                }
                            } else if (results[1].equals(AppUtils.P_FRONTITMES2)) {
                                if (datastr.length() > 0) {
                                    frontItems2 = frontItemDal.getList(datastr);
                                    loadSqhdData();
                                    if (frame.platform != 0) {
                                        addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_FRONTITMES2, "@id=22", "22"), frame.strData);
                                    }
                                }
                            } else if (results[1].equals(AppUtils.P_ADVER)) {
                                if (datastr.length() > 0) {
                                    advertises = advertiseDal.getList(results[2]);
                                    loadGuanggaoData();
                                    if (frame.platform != 0) {
                                        addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_ADVER, "@id=22", "22"), frame.strData);
                                    }
                                }
                            } else if (results[1].equals(AppUtils.P_USERINFO)) {
                                if (datastr.length() > 0) {
                                    try {
                                        JSONArray jsonArray = new JSONObject(datastr)
                                                .getJSONArray("data");
                                        JSONObject obj = (JSONObject) jsonArray.opt(0);
                                        String address = obj.get("address").toString();
                                        String village = obj.get("village").toString();
                                        String shop = obj.get("shop").toString();
                                        String property = obj.get("property").toString();
                                        String Fname = obj.get("Fname").toString();
                                        String appurl = obj.get("Appurl").toString();

                                        SystemUtils.setAddress(address);
                                        SystemUtils.setVillage(village);
                                        SystemUtils.setShop(shop);
                                        SystemUtils.setProperty(property);
                                        SystemUtils.setFname(Fname);
                                        txt_xiaoqu.setText(village);
                                        String ALIPAYurl = obj.get("ALIPAYurl").toString();
                                        String WPAYurl = obj.get("WPAYurl").toString();
                                        SystemUtils.setALIPAYurl(ALIPAYurl);
                                        SystemUtils.setWPAYurl(WPAYurl);
                                        SystemUtils.setAppurl(appurl);
                                        if (frame.platform != 0) {
                                            addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), AppUtils.P_USERINFO, String.format("@id=22,@account=%s", PreferenceUtil.getUserName()), "22"), frame.strData);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                            if (frame.platform != 0 && datastr != null && datastr.length() > 0) {
                                addCache(results[1], datastr);
                            }
                        }
                    }
                } else if (msg.what == ADSINDEX) {
                    adv_pager.setCurrentItem(msg.arg1);
                } else if (msg.what == TOSTMSG) {
                    Log.i(TAG, "I received you msg:" + msg.obj.toString());
                    Toast.makeText(getActivity(), msg.obj.toString(), Toast.LENGTH_LONG);
                }
            }

        };
    }

    //带数据返回处理
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent;
        if (resultCode == resultCodes.LOGIN) {
            if (SystemUtils.getIsLogin()) {
                switch (requestCode) {
                    case resultCodes.SOCIALPOST://阳光渝北
                        intent = new Intent(getActivity(), MessageActivity.class);
                        intent.putExtra("msgtype", 1);
                        //startActivityIfLogin(intent, resultCodes.SOCIALPOST);
                        startActivityWithAnim(getActivity(), intent);
                        break;
                    case resultCodes.COUNCILPOST://小区公告
                        intent = new Intent(getActivity(), MessageActivity.class);
                        intent.putExtra("msgtype", 3);
                        //startActivityIfLogin(intent, resultCodes.SOCIALPOST);
                        startActivityWithAnim(getActivity(), intent);
                        break;
                    case resultCodes.HOUSEHOLDING_CALL://呼叫物业
                        if (SystemUtils.getIsLogin()) {
                            //
                        } else
                            //startActivityIfLogin(null, resultCodes.HOUSEHOLDING);
                            generalhelper.ToastShow(getActivity(), "未登录");
                        break;
                    case resultCodes.HOUSEHOLDING_LEAVEMSG://物业留言
                        intent = new Intent(getActivity(), BindDevice.class);
                        startActivityIfLogin(intent, resultCodes.HOUSEHOLDING_LEAVEMSG);
                        break;
                    case resultCodes.REMOTEOPEN://远程开门
                        if (SystemUtils.getIsLogin())
                            communityOpt(20, String.format("%s\t%s", PreferenceUtil.getUserName(), PreferenceUtil.getSID()));
                        else {
                            generalhelper.ToastShow(getActivity(), "未登录");
                            //startActivityIfLogin(null, resultCodes.REMOTEOPEN);
                        }
                        break;
                    case resultCodes.DIRECTCALL://一键呼叫
                        intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:02367176359"));
                        try {
                            //startActivityIfLogin(intent, resultCodes.DIRECTCALL);
                            startActivityWithAnim(getActivity(), intent);
                        } catch (Exception e) {
                            generalhelper.ToastShow(getActivity(), "呼叫失败" + e.getMessage());
                        }
                        break;
                    case resultCodes.SHINEIJI:
                        intent = new Intent(getActivity(), BindDevice.class);
                        startActivityIfLogin(intent, resultCodes.SHINEIJI);
                        break;
//                    case resultCodes.YIKATONG:
//                        intent = new Intent(getActivity(), ServeTrace.class);
//                        startActivityIfLogin(intent, resultCodes.YIKATONG);
//                        break;
                    case resultCodes.EXPRESSIN:
                        intent = new Intent(getActivity(), Express_in.class);
                        startActivityIfLogin(intent, resultCodes.EXPRESSIN);
                        break;
                    case resultCodes.EXPRESSOUT:
                        intent = new Intent(getActivity(), Express_out.class);
                        startActivityIfLogin(intent, resultCodes.EXPRESSOUT);
                        break;
                    default:
                        generalhelper.ToastShow(getActivity(), "暂未开通");
                        break;
                }
            }
        } else if (requestCode == resultCodes.CODE2DSCAN) {

            Bundle bundle = data.getExtras();
            String result = bundle.getString(CodeUtils.RESULT_STRING);
            generalhelper.ToastShow(getActivity(), result);
            
            /*Intent intent = new Intent();
                    intent.setAction("Page." + func.gettype_id());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("item", func);
                    intent.putExtras(bundle);
                    if (isActionInstalled(intent))
                        startActivityIfLogin(intent, 0);
                    else {
                        //generalhelper.ToastShow(getActivity(), "即将上线~");
                        intent = new Intent(getActivity(), SuperMarket.class);
                        startActivityWithAnim(getActivity(), intent);
                    }
*/
        }
        //generalhelper.ToastShow(getActivity(), requestCode);
    }

    /***
     * 用户处于登录状态intent执行，否则让用户登录
     *
     * @param intent
     * @param requstCode
     */
    public void startActivityIfLogin(Intent intent, int requstCode) {
        if (SystemUtils.getIsLogin()) {
            Log.i(TAG, "已登录");
            //判断是否绑定室内机
//            if (SystemUtils.getSID().equals("")) {
//                opendialog.show();
//            } else {
//                startActivityWithAnim(getActivity(), intent);
//            }
            startActivityWithAnim(getActivity(), intent);
        } else {
            Log.i(TAG, "未登录");
            Intent loginIntent = new Intent(getActivity(), Login.class);
            loginIntent.putExtra(SystemUtils.FORRESULT, true);
            startActivityWithAnimForResult(getActivity(), loginIntent, requstCode);
        }
    }

    //隐式Intent判断目标是否存在
    public boolean isActionInstalled(Intent intent) {
        final PackageManager packageManager = getActivity().getPackageManager();
        //final Intent intent = new Intent(action);
        //检索所有可用于给定的意图进行的活动。如果没有匹配的活动，则返回一个空列表。
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    //广告循环驱动
    private void whatOption() {
        what.incrementAndGet();
        if (what.get() > imageViews.size() - 1) {
            what.getAndAdd(0 - imageViews.size());
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
    }

    //广告滑动监听
    private final class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            if (!isStoped) {
                for (int i = 0; i < imageViews.size(); i++) {
                    imageViews.get(arg0).setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(20));
                    if (arg0 != i) {
                        imageViews.get(i)
                                .setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.LTGRAY).sizeDp(20));
                    }
                }
                // what.getAndSet(arg0);
            }

        }

    }

    //功能区滑动监听
    private final class funcPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < funcimageViews.size(); i++) {
                funcimageViews.get(arg0).setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(20));
                if (arg0 != i) {
                    funcimageViews.get(i)
                            .setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.LTGRAY).sizeDp(20));
                }
            }
            what.getAndSet(arg0);
        }

    }

    //广告适配器
    private final class AdvAdapter extends PagerAdapter {
        private List<View> views = null;

        public AdvAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(views.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {

        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(views.get(arg1), 0);
            return views.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }
    }

    //功能区适配器
    private class funcAdapter extends RecyclerView.Adapter<funcAdapter.ViewHolder> {

        List<FrontItem> list;

        public funcAdapter(List<FrontItem> _lst) {
            list = _lst;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mLayoutInflater.inflate(R.layout.front_func_view, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            FrontItem funcPage = list.get(position);
            ImageLoader.bindBitmap(funcPage.geticon_url(), holder.funcicon);
            holder.functxt.setText(funcPage.getview_title());
            //holder.itemView.setOnClickListener(new funcClick(funcPage));
            holder.funcicon.setOnClickListener(new funcClick(funcPage));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView funcicon;
            TextView functxt;

            public ViewHolder(View itemView) {
                super(itemView);
                funcicon = (ImageView) itemView.findViewById(R.id.func_icon);
                functxt = (TextView) itemView.findViewById(R.id.func_txt);
            }
        }

    }

    //活动专区适配器
    private class gridItemAdapter extends RecyclerView.Adapter<gridItemAdapter.ViewHolder> {
        List<List<FrontItem>> set;

        public gridItemAdapter(List<List<FrontItem>> _set) {
            set = _set;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mLayoutInflater.inflate(R.layout.lo_front_items1, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            List<FrontItem> frontItems = set.get(position);
            ImageLoader.bindBitmap(frontItems.get(0).getimage_url(), holder.iv_bottom1, frontItems.get(0).getwidth(), frontItems.get(0).getheight(), new IImageloader() {
                @Override
                public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView, int w, int h) {
                    mItemHeight += imageView.getHeight();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("suntest", String.format("ll_shequgrid.setLayoutParams(%s)", mItemHeight));
                            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mItemHeight + iv_gridtitle.getHeight());
                            params1.addRule(RelativeLayout.BELOW, R.id.ll_messages);
                            ll_shequgrid.setLayoutParams(params1);
                        }
                    });
                }
            });
            ImageLoader.bindBitmap(frontItems.get(1).getimage_url(), holder.iv_bottom2, frontItems.get(1).getwidth(), frontItems.get(1).getheight());
            ImageLoader.bindBitmap(frontItems.get(2).getimage_url(), holder.iv_bottom3, frontItems.get(2).getwidth(), frontItems.get(2).getheight());
            holder.iv_bottom1.setOnClickListener(new goodsClick(frontItems.get(0)));
            holder.iv_bottom2.setOnClickListener(new goodsClick(frontItems.get(1)));
            holder.iv_bottom3.setOnClickListener(new goodsClick(frontItems.get(2)));
        }

        @Override
        public int getItemCount() {
            return set.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_bottom1, iv_bottom2, iv_bottom3;

            public ViewHolder(View itemView) {
                super(itemView);
                iv_bottom1 = (ImageView) itemView.findViewById(R.id.iv_bottom1);
                iv_bottom2 = (ImageView) itemView.findViewById(R.id.iv_bottom2);
                iv_bottom3 = (ImageView) itemView.findViewById(R.id.iv_bottom3);
            }
        }
    }
}
