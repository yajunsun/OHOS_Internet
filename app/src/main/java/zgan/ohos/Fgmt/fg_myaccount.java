package zgan.ohos.Fgmt;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import zgan.ohos.Activities.BindDevice;
import zgan.ohos.Activities.CreditsDetail;
import zgan.ohos.Activities.CreditsRule;
import zgan.ohos.Activities.Login;
import zgan.ohos.Activities.MyPakages;
import zgan.ohos.Activities.UpdatePassword;
import zgan.ohos.ConstomControls.RoundImageViewByXfermode;
import zgan.ohos.R;
import zgan.ohos.services.community.ZganCommunityService;
import zgan.ohos.services.community.ZganCommunityService_Listen;
import zgan.ohos.services.community.ZganCommunityService_Main;
import zgan.ohos.utils.AppUtils;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.ImageLoader;
import zgan.ohos.utils.PreferenceUtil;
import zgan.ohos.utils.SystemUtils;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.picturefile;

public class fg_myaccount extends myBaseFragment implements View.OnClickListener {

    Toolbar toolbar;
    RoundImageViewByXfermode iv_header;
    //ImageView iv_updateheader, iv_updatepwd,iv_updatepaypwd,iv_pakages, iv_logout, iv_binddevice;
    View ll_header, rl_updateheader, rl_updatepwd, rl_usecredits, rl_logout, rl_binddevice, rl_pakages;
    ImageLoader imageLoader;
    boolean headerchanged = false;
    String LOCALHEADERFILENAME;
    File pictureFile;
    LayoutInflater myInflater;
    Dialog headerSelectDialog, paypwdChangeDialog;
    TextView txt_account, txtcredits;
    ImageView ivcreditsrule;
    String mStandardsUrl;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA: {// 调用Gallery返回的
                if (data != null) {
                    Uri uri;
                    if (data.hasExtra("data")) {
                        Log.v(TAG, "bitmap data");
                        Bitmap photo = data.getParcelableExtra("data");
                        try {
                            /************ 保存图像 ******************/

                            if (pictureFile == null) {

                                Log.d("suntest",
                                        "Error creating media file, check storage permissions: ");
                                return;
                            }
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            photo.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                            headerchanged = true;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        iv_header.setImageBitmap(photo);
                    } else if ((uri = data.getData()) != null) {
                        Log.v(TAG, "uri data");
                        doCropPhoto(uri);
                    }
                    /***********************************/
                    // 下面就是显示照片了

                    headerSelectDialog.dismiss();
                } else {
                    Log.v(TAG, "data is null");
                }
            }
            break;
            case CAMERA_WITH_DATA: {// 照相机程序返回的,再次调用图片剪辑程序去修剪图片
                if (mCurrentPhotoFile.exists())
                    Log.v(TAG, mCurrentPhotoFile.getPath());
                doCropPhoto(mCurrentPhotoFile);
            }
            break;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_fg_myaccount, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ll_header = view.findViewById(R.id.ll_header);
        imageLoader = new ImageLoader();
        //Point p = AppUtils.getWindowSize(getActivity());
//        imageLoader.loadDrawableRS(getActivity(), R.drawable.bg_header, ll_header, new IImageloader() {
//            @Override
//            public void onDownloadSucc(Bitmap bitmap, String c_url, View imageView) {
//                if (Build.VERSION.SDK_INT > 15)
//                    imageView.setBackground(new BitmapDrawable(bitmap));
//            }
//        }, p.x, (int) (Integer.valueOf(getResources().getString(R.string.myaccount_header_Height)) * AppUtils.getDensity(getActivity())));
        iv_header = (RoundImageViewByXfermode) view.findViewById(R.id.iv_header);

//        iv_updateheader = (ImageView) view.findViewById(R.id.iv_updateheader);
//        iv_updatepwd = (ImageView) view.findViewById(R.id.iv_updatepwd);
//        iv_logout = (ImageView) view.findViewById(R.id.iv_logout);
//        iv_binddevice = (ImageView) view.findViewById(R.id.iv_binddevice);
//        iv_updatepaypwd=(ImageView)view.findViewById(R.id.iv_updatepaypwd);
//        iv_pakages=(ImageView)view.findViewById(R.id.iv_pakages);
        rl_updateheader = view.findViewById(R.id.rl_updateheader);
        rl_updatepwd = view.findViewById(R.id.rl_updatepwd);
        rl_usecredits = view.findViewById(R.id.rl_usecredits);
        rl_logout = view.findViewById(R.id.rl_logout);
        rl_binddevice = view.findViewById(R.id.rl_binddevice);
        rl_pakages = view.findViewById(R.id.rl_pakages);

        rl_updateheader.setOnClickListener(this);
        rl_updatepwd.setOnClickListener(this);
        rl_usecredits.setOnClickListener(this);
        rl_logout.setOnClickListener(this);
        rl_binddevice.setOnClickListener(this);
        rl_pakages.setOnClickListener(this);
        txt_account = (TextView) view.findViewById(R.id.txt_account);
        txtcredits = (TextView) view.findViewById(R.id.txtcredits);
        ivcreditsrule = (ImageView) view.findViewById(R.id.ivcreditsrule);
        ivcreditsrule.setOnClickListener(this);
        return view;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Frame frame = (Frame) msg.obj;
                    String[] results = frame.strData.split("\t");
                    String ret = generalhelper.getSocketeStringResult(frame.strData);
                    Log.v("suntest", frame.subCmd + "  " + ret);
                    if (frame.subCmd == 40) {
                        if (results[0].equals("0") && results[1].equals("1022")) {
                            if (results.length<3)
                                return;
                            if (results[2].length() > 0) {
                                try {
                                    JSONArray jsonArray = new JSONObject(results[2])
                                            .getJSONArray("data");
                                    Log.i("suntest", results[2]);
                                    JSONObject obj = (JSONObject) jsonArray.opt(0);
                                    mStandardsUrl = obj.get("standards").toString();
                                    if (frame.platform != 0) {
                                        addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1022, String.format("@id=22,@account=%s", PreferenceUtil.getUserName()), "22"), frame.strData);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } else if (results[0].equals("0") && results[1].equals("1024")) {
                            if (results.length >2) {
                                if (results[2].length() > 0) {
                                    try {
                                        JSONArray jsonArray = new JSONObject(results[2])
                                                .getJSONArray("data");
                                        Log.i("suntest", results[2]);
                                        JSONObject obj = (JSONObject) jsonArray.opt(0);
                                        int integral = obj.getInt("integral");
                                        txtcredits.setText("我的积分 "+integral);
                                        if (frame.platform != 0) {
                                            addCache("40" + String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1024, String.format("@id=22,@Fname=%s", SystemUtils.getFname()), "22"), frame.strData);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                            else
                                txtcredits.setText("我的积分 "+0);
                        }
                    }
                    break;
            }
        }
    };

    protected void loadData() {
        ZganCommunityService.toGetServerData(40, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1022, String.format("@id=22,@account=%s", PreferenceUtil.getUserName()), "22"), handler);
        ZganCommunityService.toGetServerData(40,0,2, String.format("%s\t%s\t%s\t%s", PreferenceUtil.getUserName(), 1024, String.format("@id=22,@Fname=%s", SystemUtils.getFname()), "22"), handler);
    }

    @Override
    public void onStart() {
        super.onStart();
//        LOCALHEADERFILENAME = PreferenceUtil.getUserName() + "_header";
//        pictureFile = picturefile.getdochead(LOCALHEADERFILENAME);
//        if (pictureFile != null && pictureFile.exists())
//            iv_header.setImageBitmap(BitmapFactory.decodeFile(pictureFile.getPath()));
//        else
//            iv_header.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_account_box).colorRes(R.color.md_white_1000));
//        txt_account.setText(PreferenceUtil.getUserName());
        loadData();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.rl_updateheader:
                doPickPhotoAction();
                break;
            case R.id.rl_updatepwd:
                intent = new Intent(getActivity(), UpdatePassword.class);
                startActivityWithAnim(getActivity(), intent);
                break;
            case R.id.rl_updatepaypwd:
                break;
            case R.id.rl_usecredits:
                intent = new Intent(getActivity(), CreditsDetail.class);
                startActivityWithAnim(getActivity(), intent);
                break;
            case R.id.rl_pakages:
                intent = new Intent(getActivity(), MyPakages.class);

                startActivityWithAnim(getActivity(), intent);
                break;
            case R.id.rl_logout:
                PreferenceUtil.setUserName("");
                PreferenceUtil.setPassWord("");
                PreferenceUtil.setCommunityIP("");
                PreferenceUtil.setCommunityPORT(0);
                PreferenceUtil.setSID("0");
                SystemUtils.setIsLogin(false);
                SystemUtils.setIsCommunityLogin(false);
                intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.rl_binddevice:
                intent = new Intent(getActivity(), BindDevice.class);
                startActivityWithAnim(getActivity(), intent);
                break;
            case R.id.btn_fromsd:
                doPickPhotoFromGallery();// 从相册中去获取
                break;
            case R.id.btn_fromcamera:
                String status = Environment
                        .getExternalStorageState();
                if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
                    doTakePhoto();// 用户点击了从照相机获取
                } else {
                    generalhelper.ToastShow(getActivity(),
                            "没有SD卡");
                }
                break;
            case R.id.btn_dismiss:
                headerSelectDialog.dismiss();
                break;
            case R.id.ivcreditsrule:
                intent = new Intent(getActivity(), CreditsRule.class);
                intent.putExtra("creditsrule", mStandardsUrl);
                startActivityWithAnim(getActivity(), intent);
                break;
        }
    }

    /******************
     * 或取头像
     *******************/
    /* 用来标识请求照相功能的activity */
    private static final int CAMERA_WITH_DATA = 3023;

    /* 用来标识请求gallery的activity */
    private static final int PHOTO_PICKED_WITH_DATA = 3021;

    /* 拍照的照片存储位置 */
    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");

    private File mCurrentPhotoFile;// 照相机拍照得到的图片

    private void doPickPhotoAction() {
        Button btn_fromsd, btn_fromcamera, btn_dismiss;
        View view = myInflater.inflate(R.layout.photo_choose_dialog,
                null);

        btn_fromsd = (Button) view.findViewById(R.id.btn_fromsd);
        btn_fromcamera = (Button) view.findViewById(R.id.btn_fromcamera);
        btn_dismiss = (Button) view.findViewById(R.id.btn_dismiss);
        btn_fromsd.setOnClickListener(this);
        btn_fromcamera.setOnClickListener(this);
        btn_dismiss.setOnClickListener(this);
        headerSelectDialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
        headerSelectDialog.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        Window window = headerSelectDialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        headerSelectDialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        headerSelectDialog.setCanceledOnTouchOutside(true);
        headerSelectDialog.show();
    }

    // }

    /**
     * 拍照获取图片
     */
    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();// 创建照片的存储目录
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
            final Intent intent = getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "R.string.photoPickerNotFoundText",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * 用当前时间给取得的图片命名
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date) + ".jpg";
    }

    // 请求Gallery程序
    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            Intent intent = getPhotoPickIntent();
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), " R.string.photoPickerNotFoundText1",
                    Toast.LENGTH_LONG).show();
        }
    }

    // 封装请求Gallery的intent
    public static Intent getPhotoPickIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.setClassName("com.android.camera",
        // "com.android.camera.CropImage");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        // intent.putExtra("crop", "true");
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        intent.putExtra("outputX", 150);
//        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        return intent;
    }

    protected void doCropPhoto(File of) {
        try {
            // 启动gallery去剪辑这个照片
            Intent intent = getCropImageIntent(Uri.fromFile(of));
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "R.string.photoPickerNotFoundText",
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void doCropPhoto(Uri uri) {
        try {
            // 启动gallery去剪辑这个照片
            Intent intent = getCropImageIntent(uri);
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "R.string.photoPickerNotFoundText",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Constructs an intent for image cropping. 调用图片剪辑程序
     */
    public static Intent getCropImageIntent(Uri photoUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        // intent.addCategory(Intent.CATEGORY_OPENABLE);
        // intent.setClassName("com.android.camera",
        // "com.android.camera.CropImage");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        return intent;
    }
}
