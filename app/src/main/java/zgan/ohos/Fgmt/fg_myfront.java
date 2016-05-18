package zgan.ohos.Fgmt;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

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
import zgan.ohos.Activities.CallOut;
import zgan.ohos.Activities.LeaveMessages;
import zgan.ohos.Activities.Login;
import zgan.ohos.Activities.MessageActivity;
import zgan.ohos.ConstomControls.ScrollViewWithCallBack;
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
 */
public class fg_myfront extends myBaseFragment implements View.OnClickListener {
    View l_shequgonggao, l_yangguangyubei, l_remoteopen, l_call_mall, l_hujiaowuye, l_wuyeliuyan, l_yunyan, l_yunkong;
    LinearLayout ll_shequhuodong;
    IconicsImageView iv_location;
    AlertDialog opendialog, telDialog;
    ScrollViewWithCallBack sscontent;
    ViewPager adv_pager;
    LinearLayout pager_ind;
    static final int ADSINDEX = 0;
    boolean isContinue = true;
    List<ImageView> imageViews = new ArrayList<>();
    private AtomicInteger what = new AtomicInteger(0);
    List<FrontItem> frontItems;
    List<Advertise> advertises;
    AdvertiseDal advertiseDal;
    FrontItemDal frontItemDal;
    Calendar lastOpent;
    Calendar thisOpen;
    Calendar lastCall;
    Calendar thisCall;
    ImageView iv_zhihuishequ, iv_bottom1, iv_bottom2, iv_bottom3, iv_shequfuwu;
    Point p;
    TextView txt_xiaoqu;
    boolean LOAD_SUCCESS = false;
    private Handler handler;
    Timer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_front, container, false);
        frontItems = new ArrayList<>();
        frontItemDal = new FrontItemDal();
        advertiseDal = new AdvertiseDal();
        iniHandler();
        initView(view);
        //initNetData();
        initDialog();
        Log.i(TAG, "fg_myfront view created");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (handler == null)
            iniHandler();
        isStoped = false;
        initNetData();
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

//    @Override
//    public void onDestroyView() {
//        playadsTask.interrupt();
//        super.onDestroyView();
//    }

    private void initNetData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!LOAD_SUCCESS && !isStoped) {
                    while (!SystemUtils.getIsCommunityLogin()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (SystemUtils.getIsCommunityLogin()) {
                        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1020, String.format("@id=22,@account=%s", PreferenceUtil.getUserName()), "22"), handler);
                        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1002, "@id=22", "22"), handler);
                        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1001, "@id=22", "22"), handler);
                    }
                }

            }
        }).start();
    }

    public void ViewClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.l_shequgonggao:
                intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("msgtype", 0);
                //startActivityWithAnim(getActivity(), intent);
                startActivityIfLogin(intent, resultCodes.SOCIALPOST);
                break;
            case R.id.l_yangguangyubei:
                intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("msgtype", 3);
                //startActivityWithAnim(getActivity(), intent);
                startActivityIfLogin(intent, resultCodes.COUNCILPOST);
                break;
            case R.id.l_remoteopen:
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
                break;
            case R.id.l_call_mall:
                //intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:02367176359"));
                //communityOpt(37, String.format("%s\t%s", PreferenceUtil.getUserName(), 3));
                if (SystemUtils.getIsLogin())
                    //第一次开门
                    if (lastCall == null) {
                        lastCall = Calendar.getInstance();
                        intent = new Intent(getActivity(), CallOut.class);
                        startActivityWithAnim(getActivity(), intent);
                    } else {
                        thisCall = Calendar.getInstance();
                        long span = thisCall.getTimeInMillis() - lastCall.getTimeInMillis();
                        //判断上次点击开门和本次点击开门时间间隔是否大于5秒钟
                        if (span > 5000) {
                            lastCall = Calendar.getInstance();
                            //communityOpt(37, String.format("%s\t%s", PreferenceUtil.getUserName(), PreferenceUtil.getSID()));
                            intent = new Intent(getActivity(), CallOut.class);
                            startActivityWithAnim(getActivity(), intent);
                        } else {
                            generalhelper.ToastShow(getActivity(), "请在" + ((5000 - span) / 1000 + 1) + "秒后操作");
                        }
                    }
                else {
                    startActivityIfLogin(null, resultCodes.REMOTEOPEN);
                }
                break;
            case R.id.l_hujiaowuye:
                //intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:02367288312"));
                intent = new Intent(getActivity(), CallOut.class);
                startActivityIfLogin(intent, resultCodes.DIRECTCALL);
                break;
            case R.id.l_wuyeliuyan:
                intent = new Intent(getActivity(), LeaveMessages.class);
                startActivityIfLogin(intent, resultCodes.HOUSEHOLDING_LEAVEMSG);
                break;
            case R.id.l_yunkong:
//                intent=new Intent(getActivity(),ServeTrace.class);
//                startActivityIfLogin(intent,resultCodes.YUNKONG);
                generalhelper.ToastShow(getActivity(), "即将上线~");
                break;
            case R.id.l_yunyan:
//                intent=new Intent(getActivity(),ServeTrace.class);
//                startActivityIfLogin(intent,resultCodes.YUNYAN);
                generalhelper.ToastShow(getActivity(), "即将上线~");
                break;
        }
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
//        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
//        toolbar.setTitle(getResources().getString(R.string.app_name));
//        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        txt_xiaoqu = (TextView) v.findViewById(R.id.txt_xiaoqu);
        sscontent = (ScrollViewWithCallBack) v.findViewById(R.id.ll_content);
        l_shequgonggao = v.findViewById(R.id.l_shequgonggao);
        l_yangguangyubei = v.findViewById(R.id.l_yangguangyubei);
        iv_zhihuishequ = (ImageView) v.findViewById(R.id.iv_zhihuishequ);
        l_remoteopen = v.findViewById(R.id.l_remoteopen);
        l_call_mall = v.findViewById(R.id.l_call_mall);
        l_hujiaowuye = v.findViewById(R.id.l_hujiaowuye);
        l_wuyeliuyan = v.findViewById(R.id.l_wuyeliuyan);
        ll_shequhuodong = (LinearLayout) v.findViewById(R.id.ll_shequhuodong);
        l_yunyan = v.findViewById(R.id.l_yunyan);
        l_yunkong = v.findViewById(R.id.l_yunkong);

        l_call_mall.setOnClickListener(this);
        l_remoteopen.setOnClickListener(this);
        l_shequgonggao.setOnClickListener(this);
        l_yangguangyubei.setOnClickListener(this);
        l_hujiaowuye.setOnClickListener(this);
        l_wuyeliuyan.setOnClickListener(this);
        l_yunyan.setOnClickListener(this);
        l_yunkong.setOnClickListener(this);
        //iv_shequhuodong = (ImageView) v.findViewById(R.id.iv_shequhuodong);
        adv_pager = (ViewPager) v.findViewById(R.id.adv_pager);
        pager_ind = (LinearLayout) v.findViewById(R.id.pager_ind);
        iv_bottom1 = (ImageView) v.findViewById(R.id.iv_bottom1);
        iv_bottom2 = (ImageView) v.findViewById(R.id.iv_bottom2);
        iv_bottom3 = (ImageView) v.findViewById(R.id.iv_bottom3);
        iv_shequfuwu = (ImageView) v.findViewById(R.id.iv_shequfuwu);

//        sscontent.setScrollViewListener(new ScrollViewWithCallBack.OnScrollListener() {
//            @Override
//            public void OnScroll(ScrollViewWithCallBack scrollView, int x, int y, int oldx, int oldy) {
//                if (y > 20 && y > oldy) {
//                    iv_zhihuishequ.setAlpha(1 / y);
//                }
//                if (y < 20 && y < oldy) {
//                    iv_zhihuishequ.setAlpha(0xff);
//                }
//            }
//        });
    }

    private void loadSqhdData() {
        final List<FrontItem> bottoms = new ArrayList<>();
        int count = frontItems.size();
        if (count > 3) {
            bottoms.add(frontItems.get(count - 1));
            bottoms.add(frontItems.get(count - 2));
            bottoms.add(frontItems.get(count - 3));
            frontItems.remove(count - 1);
            frontItems.remove(count - 2);
            frontItems.remove(count - 3);
            ImageLoader.bindBitmap(bottoms.get(2).getpic_url(), iv_bottom1, 200, 200);
            iv_bottom1.setOnClickListener(new goodsClick(bottoms.get(2)));
            ImageLoader.bindBitmap(bottoms.get(1).getpic_url(), iv_bottom2, 200, 200);
            iv_bottom2.setOnClickListener(new goodsClick(bottoms.get(1)));
            ImageLoader.bindBitmap(bottoms.get(0).getpic_url(), iv_bottom3, 200, 200);
            iv_bottom3.setOnClickListener(new goodsClick(bottoms.get(0)));
        }
        int llheight = (int) (frontItems.size() * 160 * AppUtils.getDensity(getActivity()));
        ll_shequhuodong.setMinimumHeight(llheight);

        int height = (int) (160 * AppUtils.getDensity(getActivity()));

        for (final FrontItem item : frontItems) {
            ImageView iv = new ImageView(getActivity());

            iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            iv.setPadding(0, 0, 0, 20);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageLoader.bindBitmap(item.getpic_url(), iv, p.x, height);
            iv.setOnClickListener(new goodsClick(item));
            ll_shequhuodong.addView(iv);
            iv_shequfuwu.setVisibility(View.VISIBLE);
        }
    }

    class goodsClick implements View.OnClickListener {
        FrontItem item;

        public goodsClick(FrontItem _item) {
            item = _item;
        }

        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent();
                intent.setAction("Page." + item.getview_id());
                intent.putExtra("pageid", item.getview_id());
                if (isActionInstalled(intent))
                    startActivityIfLogin(intent, 0);
                else
                    generalhelper.ToastShow(getActivity(), "即将上线~");
            } catch (ActivityNotFoundException anfe) {
                generalhelper.ToastShow(getActivity(), "即将上线~");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadGuanggaoData() {
        if (advertises != null) {
            List<View> advPics = new ArrayList<>();
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            for (int i = 0; i < advertises.size(); i++) {
                ImageView img = new ImageView(getActivity());
                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setOnClickListener(new adverClick(advertises.get(i)));
                ImageLoader.bindBitmap(advertises.get(i).getpic_url(), img, 500, 200);
                advPics.add(img);
                ImageView simg = new ImageView(getActivity());
                simg.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
                simg.setPadding(5, 5, 5, 5);
                if (i == 0)
                    simg.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.RED).sizeDp(30));
                else
                    simg.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_brightness_1).color(Color.WHITE).sizeDp(30));
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


    class adverClick implements View.OnClickListener {
        Advertise advertise;

        public adverClick(Advertise _adv) {
            advertise = _adv;
        }

        @Override
        public void onClick(View v) {
            Intent intent;
            if (advertise.getad_type() == 6) {
                intent = new Intent("url:" + advertise.getweb_url());
            } else {
                intent = new Intent(getActivity(), AdvertiseDetail.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("advertise", advertise);
                intent.putExtras(bundle);
            }
            startActivityWithAnim(getActivity(), intent);
        }
    }

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
        if (!SystemUtils.getIsCommunityLogin()) {
            opendialog.show();
        } else {
            ViewClick(view);
        }
    }


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
                            if (results[1].equals("1001")) {
                                if (datastr.length() > 0) {
                                    frontItems = frontItemDal.getList(datastr);
                                    loadSqhdData();
                                }
                            } else if (results[1].equals("1002")) {
                                if (datastr.length() > 0) {
                                    advertises = advertiseDal.getList(results[2]);
                                    loadGuanggaoData();
                                }
                            } else if (results[1].equals("1020")) {
                                if (datastr.length() > 0) {
                                    try {
                                        JSONArray jsonArray = new JSONObject(datastr)
                                                .getJSONArray("data");
                                        Log.i("suntest", datastr);
                                        JSONObject obj = (JSONObject) jsonArray.opt(0);
                                        String address = obj.get("address").toString();
                                        String village = obj.get("village").toString();
                                        SystemUtils.setAddress(address);
                                        SystemUtils.setVillage(village);
                                        txt_xiaoqu.setText(village);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                } else if (msg.what == ADSINDEX) {
                    adv_pager.setCurrentItem(msg.arg1);
                }
            }

        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent;
        if (resultCode == resultCodes.LOGIN)
            if (SystemUtils.getIsLogin())
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
//                    case resultCodes.YUNYAN:
//                        intent = new Intent(getActivity(), ServeTrace.class);
//                        startActivityIfLogin(intent, resultCodes.YUNYAN);
//                        break;
//                    case resultCodes.YUNKONG:
//                        intent = new Intent(getActivity(), ServeTrace.class);
//                        startActivityIfLogin(intent, resultCodes.YUNKONG);
//                        break;
                    default:
                        generalhelper.ToastShow(getActivity(), "暂未开通");
                        break;
                }
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

    public boolean isActionInstalled(Intent intent) {
        final PackageManager packageManager = getActivity().getPackageManager();
        //final Intent intent = new Intent(action);
        //检索所有可用于给定的意图进行的活动。如果没有匹配的活动，则返回一个空列表。
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

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
                what.getAndSet(arg0);
            }

        }

    }

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
}
