package zgan.ohos.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import zgan.ohos.Fgmt.fg_myaccount;
import zgan.ohos.Fgmt.fg_myfront;
import zgan.ohos.Fgmt.fg_myorder;
import zgan.ohos.Models.MyOrder;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.resultCodes;

public class MainActivity extends myBaseActivity {
    /****
     * local variables
     *****/
    final static int CURRENT_OPTION_MAIN = 1;
    final static int CURRENT_OPTION_CATEGORY = 2;
    final static int CURRENT_OPTION_SC = 3;
    final static int CURRENT_OPTION_MINE = 4;
    int current_option_index = 0;
//    static int badgeCount = 6;
//    private BadgeStyle style = ActionItemBadge.BadgeStyles.RED.getStyle();
    /*********/
    /*********
     * user control
     ********/
    IconicsImageView ivfront;
    IconicsImageView ivorder;
    IconicsImageView ivaccount;
    TextView txtfront;
    TextView txtorder;
    TextView txtaccount;
    FrameLayout fl_badgeCount;
    /************************/
    /*****
     * fragment
     ****/
    fg_myfront myfront;
    fg_myorder myorder;
    fg_myaccount myaccount;
    FragmentManager fgManager;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        }
    }

    public void initView() {
        setContentView(R.layout.activity_main2);
        fgManager = getFragmentManager();
        ivfront = (IconicsImageView) findViewById(R.id.iv_front);
        ivorder = (IconicsImageView) findViewById(R.id.iv_order);
        //fl_badgeCount = (FrameLayout) findViewById(R.id.badge_count);

        ivaccount = (IconicsImageView) findViewById(R.id.iv_account);
        txtfront = (TextView) findViewById(R.id.txt_front);
        txtorder = (TextView) findViewById(R.id.txt_order);
        txtaccount = (TextView) findViewById(R.id.txt_account);
        current_option_index = 0;
        setTabSelection(CURRENT_OPTION_MAIN);
        Log.v(TAG, "main activity initialed");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        current_option_index=savedInstanceState.getInt("currentoptionindex");
        setTabSelection(current_option_index);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentoptionindex",current_option_index);
        //super.onSaveInstanceState(outState);
    }

    void initialOptions() {
//        ivfront.setColor(getResources().getColor(R.color.navigation_txt_color));
//        ivorder.setColor(getResources().getColor(R.color.navigation_txt_color));
//        ivaccount.setColor(getResources().getColor(R.color.navigation_txt_color));
        ivfront.setImageResource(R.drawable.shouye1);
        ivorder.setImageResource(R.drawable.guajia1);
        ivaccount.setImageResource(R.drawable.wo1);
        txtfront.setTextColor(getResources().getColor(R.color.navigation_txt_color));
        txtorder.setTextColor(getResources().getColor(R.color.navigation_txt_color));
        txtaccount.setTextColor(getResources().getColor(R.color.navigation_txt_color));
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
            Intent intentCommunity=new Intent(MainActivity.this, ZganCommunityService.class);
            stopService(intentCommunity);
            Log.v(TAG, "service stoped,activity destroied");
            AppUtils.exits();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
