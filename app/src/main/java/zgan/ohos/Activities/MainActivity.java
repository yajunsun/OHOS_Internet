package zgan.ohos.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import zgan.ohos.Dals.FrontItemDal;
import zgan.ohos.Dals.ZGbaseDal;
import zgan.ohos.Fgmt.fg_myaccount;
import zgan.ohos.Fgmt.fg_myfront;
import zgan.ohos.Fgmt.fg_myorder;
import zgan.ohos.Fgmt.fg_shoppingcart;
import zgan.ohos.Models.BigAdvertise;
import zgan.ohos.Models.FrontItem;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

/**
 * create by yajunsun
 * <p/>
 * 首页activity
 */
public class MainActivity extends myBaseActivity {
    /****
     * local variables
     *****/
    final static int CURRENT_OPTION_MAIN = 1;
    final static int CURRENT_OPTION_CATEGORY = 2;
    final static int CURRENT_OPTION_SC = 3;
    final static int CURRENT_OPTION_MINE = 4;
    int current_option_index = 0;
    boolean LOADADV = true;
    BigAdvertise frontItem;
//    static int badgeCount = 6;
//    private BadgeStyle style = ActionItemBadge.BadgeStyles.RED.getStyle();
    /*********/
    /*********
     * user control
     ********/
    IconicsImageView ivfront;
    IconicsImageView ivorder;
    IconicsImageView ivaccount;
    IconicsImageView ivshopcart;
    TextView txtfront;
    TextView txtorder;
    TextView txtaccount;
    TextView txtshopcart;


    FrameLayout fl_badgeCount;
    /************************/
    /*****
     * fragment
     ****/
    fg_myfront myfront;
    fg_myorder myorder;
    fg_myaccount myaccount;
    fg_shoppingcart mycart;
    FragmentManager fgManager;

    /**
     * 广告
     **/
    ImageView iv_adv;
    View fladv;
    Timer timer;
    int time = 0;
    Button btnbreak;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case resultCodes.HOUSEHOLDER:
//                if (SystemUtils.getIsCommunityLogin()) {
//                    setTabSelection(CURRENT_OPTION_MAIN);
//                }
//                else
//                {
//                    setTabSelection(CURRENT_OPTION_CATEGORY);
//                }
//                break;
//            case resultCodes.MYACCOUNT:
//                if (SystemUtils.getIsLogin()) {
//                    setTabSelection(CURRENT_OPTION_MINE);
//                }
//                break;
//        }
    }

    /*****************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Log.v(TAG, "created");
        AppUtils.iniMainActivity(MainActivity.this);

        //极光推送初始化
        JPushInterface.init(getApplicationContext());
        //极光推送的注册ID，需要注册到开发者服务器
        //String rid = JPushInterface.getRegistrationID(getApplicationContext());
        setStyleCustom();
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1030, "@id=22", "22"), handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FragmentTransaction transaction = fgManager.beginTransaction();
        transaction.remove(myfront);
        transaction.remove(myorder);
        transaction.remove(myaccount);
        transaction.remove(mycart);
        myfront = null;
        myorder = null;
        myaccount = null;
        mycart = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "started");
    }

    @Override
    public void ViewClick(View v) {
        switch (v.getId()) {
            case R.id.ll_front:
                setTabSelection(CURRENT_OPTION_MAIN);
                break;
            case R.id.ll_order:
                if (SystemUtils.getIsCommunityLogin()) {
                    setTabSelection(CURRENT_OPTION_CATEGORY);
                } else {
                    Intent intent = new Intent(this, Login.class);
                    //intent.putExtra(SystemUtils.FORRESULT, true);
                    //startActivityWithAnimForResult(intent, resultCodes.HOUSEHOLDER);
                    startActivityWithAnim(intent);
                    finish();
                }
                break;
            case R.id.ll_shopcart:
                if (SystemUtils.getIsLogin()) {
                    setTabSelection(CURRENT_OPTION_SC);
                } else {
                    Intent intent = new Intent(this, Login.class);
                    //intent.putExtra(SystemUtils.FORRESULT, true);
                    //startActivityWithAnimForResult(intent, resultCodes.MYACCOUNT);
                    startActivityWithAnim(intent);
                    finish();
                }
                break;
            case R.id.ll_account:
                if (SystemUtils.getIsLogin()) {
                    setTabSelection(CURRENT_OPTION_MINE);
                } else {
                    Intent intent = new Intent(this, Login.class);
                    //intent.putExtra(SystemUtils.FORRESULT, true);
                    //startActivityWithAnimForResult(intent, resultCodes.MYACCOUNT);
                    startActivityWithAnim(intent);
                    finish();
                }
                break;
            case R.id.btn_break:
                timer.cancel();
                fladv.setVisibility(View.GONE);
                break;
        }
    }

    public void initView() {
        setContentView(R.layout.activity_main2);
        fgManager = getFragmentManager();
        ivfront = (IconicsImageView) findViewById(R.id.iv_front);
        ivorder = (IconicsImageView) findViewById(R.id.iv_order);
        //fl_badgeCount = (FrameLayout) findViewById(R.id.badge_count);
        ivshopcart = (IconicsImageView) findViewById(R.id.iv_shopcart);
        ivaccount = (IconicsImageView) findViewById(R.id.iv_account);
        txtfront = (TextView) findViewById(R.id.txt_front);
        txtorder = (TextView) findViewById(R.id.txt_order);
        txtaccount = (TextView) findViewById(R.id.txt_account);
        txtshopcart = (TextView) findViewById(R.id.txt_shopcart);
        current_option_index = 0;
        setTabSelection(CURRENT_OPTION_MAIN);
        Log.v(TAG, "main activity initialed");
    }

    /**
     * 设置通知栏样式 - 定义通知栏Layout
     */
    private void setStyleCustom() {
        CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(MainActivity.this, R.layout.customer_notitfication_layout, R.id.icon, R.id.title, R.id.text);
        builder.layoutIconDrawable = R.drawable.launcher;
        builder.developerArg0 = "developerArg2";
        JPushInterface.setPushNotificationBuilder(2, builder);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        current_option_index = savedInstanceState.getInt("currentoptionindex");
        setTabSelection(current_option_index);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentoptionindex", current_option_index);
        //super.onSaveInstanceState(outState);
    }

    void initialOptions() {
//        ivfront.setColor(getResources().getColor(R.color.navigation_txt_color));
//        ivorder.setColor(getResources().getColor(R.color.navigation_txt_color));
//        ivaccount.setColor(getResources().getColor(R.color.navigation_txt_color));
        ivfront.setImageResource(R.drawable.shouye1);
        ivorder.setImageResource(R.drawable.guajia1);
        ivaccount.setImageResource(R.drawable.wo1);
        ivshopcart.setImageResource(R.drawable.shopcarticon_g);
        txtfront.setTextColor(getResources().getColor(R.color.navigation_txt_color));
        txtorder.setTextColor(getResources().getColor(R.color.navigation_txt_color));
        txtaccount.setTextColor(getResources().getColor(R.color.navigation_txt_color));
        txtshopcart.setTextColor(getResources().getColor(R.color.navigation_txt_color));
    }


    /**
     * 根据传入的index参数来设置选中的tab页。
     *
     * @param _index 每个tab页对应的下标。0表示消息，1表示联系人，2表示动态，3表示设置。
     */

    private void setTabSelection(int _index) {
        Log.v(TAG, String.valueOf(_index));
        switch (_index) {
            case CURRENT_OPTION_MAIN:
                Log.v(TAG, "current_option_index:" + current_option_index + " CURRENT_OPTION_MAIN:" + CURRENT_OPTION_MAIN);
                if (current_option_index != CURRENT_OPTION_MAIN) {
                    Log.v(TAG, "main front selected");
                    // 每次选中之前先清楚掉上次的选中状态
                    initialOptions();
                    // 开启一个Fragment事务
                    FragmentTransaction transaction = fgManager.beginTransaction();
                    // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
                    hideFragments(transaction);
                    Bundle bundle = new Bundle();
                    //ivfront.setColorRes(R.color.colorPrimary);
                    ivfront.setImageResource(R.drawable.shouye);
                    txtfront.setTextColor(getResources().getColor(R.color.colorPrimary));
                    if (myfront == null) {
                        // 如果Fragment为空，则创建一个并添加到界面上
                        myfront = new fg_myfront();
                        Log.v(TAG, "new front fragment");
                        transaction.add(R.id.content, myfront);
                    } else {
                        // // 如果Fragment不为空，则直接将它显示出来
                        transaction.show(myfront);
                        Log.v(TAG, "show old front fragment");
                    }
                    transaction.commit();
                }
                break;
            case CURRENT_OPTION_SC:
                if (current_option_index != CURRENT_OPTION_SC) {
                    // 每次选中之前先清楚掉上次的选中状态
                    initialOptions();
                    // 开启一个Fragment事务
                    FragmentTransaction transaction = fgManager.beginTransaction();
                    // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
                    hideFragments(transaction);
                    Bundle bundle = new Bundle();
                    //ivorder.setColorRes(R.color.colorPrimary);
                    ivshopcart.setImageResource(R.drawable.shopcarticon_p);
                    txtshopcart.setTextColor(getResources().getColor(R.color.colorPrimary));
                    if (mycart == null) {
                        // 如果Fragment为空，则创建一个并添加到界面上
                        mycart = new fg_shoppingcart();
                        transaction.add(R.id.content, mycart);
                    } else {
                        // // 如果Fragment不为空，则直接将它显示出来
                        transaction.show(mycart);
                        mycart.onStart();
                    }
                    transaction.commit();
                }
                break;
            case CURRENT_OPTION_CATEGORY:
                if (current_option_index != CURRENT_OPTION_CATEGORY) {
                    // 每次选中之前先清楚掉上次的选中状态
                    initialOptions();
                    // 开启一个Fragment事务
                    FragmentTransaction transaction = fgManager.beginTransaction();
                    // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
                    hideFragments(transaction);
                    Bundle bundle = new Bundle();
                    //ivorder.setColorRes(R.color.colorPrimary);
                    ivorder.setImageResource(R.drawable.guajia);
                    txtorder.setTextColor(getResources().getColor(R.color.colorPrimary));
                    if (myorder == null) {
                        // 如果Fragment为空，则创建一个并添加到界面上
                        myorder = new fg_myorder();
                        transaction.add(R.id.content, myorder);
                    } else {
                        // // 如果Fragment不为空，则直接将它显示出来
                        transaction.show(myorder);
                        myorder.onStart();
                    }
                    transaction.commit();
                }
                break;
            case CURRENT_OPTION_MINE:
                if (current_option_index != CURRENT_OPTION_MINE) {
                    // 每次选中之前先清楚掉上次的选中状态
                    initialOptions();
                    // 开启一个Fragment事务
                    FragmentTransaction transaction = fgManager.beginTransaction();
                    // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
                    hideFragments(transaction);
                    Bundle bundle = new Bundle();
                    //ivaccount.setColorRes(R.color.colorPrimary);
                    ivaccount.setImageResource(R.drawable.wo);
                    txtaccount.setTextColor(getResources().getColor(R.color.colorPrimary));
                    if (myaccount == null) {
                        // 如果Fragment为空，则创建一个并添加到界面上
                        myaccount = new fg_myaccount();
                        transaction.add(R.id.content, myaccount);
                    } else {
                        // 如果Fragment不为空，则直接将它显示出来
                        transaction.show(myaccount);
                    }
                    transaction.commit();
                }
                break;
        }
        current_option_index = _index;

    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (myfront != null) {
            transaction.hide(myfront);
        }
        if (myorder != null) {
            transaction.hide(myorder);
        }
        if (myaccount != null) {
            transaction.hide(myaccount);
        }
        if (mycart != null) {
            transaction.hide(mycart);
        }
    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intentLogin = new Intent(MainActivity.this, ZganLoginService.class);
            stopService(intentLogin);
            Intent intentCommunity = new Intent(MainActivity.this, ZganCommunityService.class);
            stopService(intentCommunity);
            Log.v(TAG, "service stoped,activity destroied");
            AppUtils.exits();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Frame frame = (Frame) msg.obj;
                String ret = generalhelper.getSocketeStringResult(frame.strData);
                Log.i(TAG, frame.subCmd + "  " + ret);
                {
                    String[] results = frame.strData.split("\t");
                    if (results[0].equals("0")) {
                        String datastr = results[2];
                        if (results[1].equals(AppUtils.P_BIGADVE)) {
                            if (datastr.length() > 0) {
                                frontItem = new ZGbaseDal<BigAdvertise>().GetSingleModel(datastr, new BigAdvertise());
                                if (frontItem != null && !frontItem.getimage_url().isEmpty()) {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            fladv = findViewById(R.id.fladv);
                                            iv_adv = (ImageView) findViewById(R.id.iv_adv);
                                            btnbreak = (Button) findViewById(R.id.btn_break);
                                            ImageLoader.bindBitmap(frontItem.getimage_url(), iv_adv);
                                            fladv.setVisibility(View.VISIBLE);
                                            timer = new Timer(true);
                                            time = frontItem.gettimer() == 0 ? 3 : frontItem.gettimer();
                                            timer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    if (time == 0) {
                                                        sendEmptyMessage(2);
                                                    }
                                                    time--;
                                                }
                                            },0, 1000);
                                        }
                                    });

                                }
                            }
                        }
                    }
                }
            } else if (msg.what == 2) {
                timer.cancel();
                fladv.setVisibility(View.GONE);
            }
//            else if(msg.what==3)
//            {
//                btnbreak.setText(time+"秒 ");
//            }

        }
    };

}
