package zgan.ohos.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import zgan.ohos.Fgmt.myBaseFragment;
import zgan.ohos.MyApplication;

public class picturefile {

    public static final int MEDIA_TYPE_IMAGE = 1;

    public static final int MEDIA_TYPE_PDF = 2;

    /******
     * 获取临时图像文件地址
     *******/
    public static String getTempFilestr() {
        return getappdirpic() + File.separator + "IMG_temppic.jpg";
    }

    /******
     * 获取应用程序使用的图片存放目录
     *******/
    public static String getappdirpic() {
        // 安全起见，在使用前应该

        // 用Environment.getExternalStorageState()检查SD卡是否已装入
        try {
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(

                            Environment.DIRECTORY_PICTURES), MyApplication.context.getPackageName());
            // 如果期望图片在应用程序卸载后还存在、且能被其它应用程序共享，

            // 则此保存位置最合适

            // 如果不存在的话，则创建存储目录

            if (!mediaStorageDir.exists()) {

                if (!mediaStorageDir.mkdirs()) {

                    Log.d(MyApplication.context.getPackageName(), "failed to create directory");

                    return null;

                }
            }
            return mediaStorageDir.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 为保存图片或视频创建文件Uri */
    /*****
     * 获取新建图像文件的Uri
     *******/
    public static Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /******
     * 删除临时图片文件
     *******/
    public static void DeleteTemppic() {
        File file = new File(getTempFilestr());
        if (file.exists())
            file.delete();
    }

    /*****
     * 根据文件路径获取Uri
     *****/
    public static Uri getUriFromPath(String path) {
        return Uri.fromFile(new File(path));
    }

    /**
     * 为保存图片或视频创建File
     */
    public static File getOutputMediaFile(int type) {

        String filedir = getappdirpic();

        // 创建媒体文件名

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(filedir + File.separator + "IMG_" + timeStamp
                    + ".jpg");

        } else if (type == MEDIA_TYPE_PDF) {
            mediaFile = new File(filedir + File.separator + "PDF_" + timeStamp
                    + ".pdf");
        } else {
            return null;
        }
        return mediaFile;
    }

    /******
     * 获取头像
     ********/
    public static File getdochead(String doctorid) {
        String filedir = getappdirpic();
        File mediaFile = new File(filedir + File.separator + doctorid + ".jpg");
        return mediaFile;
    }

    /******************
     * 或取头像
     *******************/

	/* 用来标识请求照相功能的activity */
    public static final int CAMERA_WITH_DATA = 3023;

    /* 用来标识请求gallery的activity */
    public static final int PHOTO_PICKED_WITH_DATA = 3021;

    /* 拍照的照片存储位置 */
    public static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");

    //public static File mCurrentPhotoFile;// 照相机拍照得到的图片
    public static final int action_takephoto = 0;
    public static final int action_pickphoto = 1;
    //public Handler handler;
    public File mCurrentPhotoFile;

    public void doPickPhotoAction(final myBaseFragment f, final Handler _handler) {
        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(f.getActivity(),
                android.R.style.Theme_Light);
        String cancel = "返回";
        String[] choices;
        choices = new String[2];
        choices[0] = "拍照 "; // getString(R.string.take_photo); //拍照
        choices[1] = "从相册中选择";// getString(R.string.pick_photo); //从相册中选择
        final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
                android.R.layout.simple_list_item_1, choices);

        final AlertDialog.Builder builder = new AlertDialog.Builder(
                dialogContext);
        // builder.setTitle(R.string.attachToContact);
        builder.setSingleChoiceItems(adapter, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0: {
                                String status = Environment
                                        .getExternalStorageState();
                                if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
                                    doTakePhoto(f);// 用户点击了从照相机获取
                                    _handler.sendEmptyMessage(action_takephoto);
                                } else {
                                    generalhelper.ToastShow(dialogContext, "没有SD卡");
                                }
                                break;

                            }
                            case 1:
                                _handler.sendEmptyMessage(action_pickphoto);
                                doPickPhotoFromGallery(f);// 从相册中去获取
                                break;
                        }
                    }
                });
        builder.setNegativeButton(cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        builder.create().show();
    }

    /**
     * 拍照获取图片
     */
    protected void doTakePhoto(myBaseFragment f) {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();// 创建照片的存储目录
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
            Intent intent = getTakePickIntent(mCurrentPhotoFile);
            f.startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(f.getActivity(), "R.string.photoPickerNotFoundText",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void doCropPhoto(myBaseFragment fg) {
        try {
            // 启动gallery去剪辑这个照片
            Intent intent = picturefile.getCropImageIntent(Uri.fromFile(mCurrentPhotoFile));
            fg.startActivityForResult(intent, picturefile.PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            Toast.makeText(fg.getActivity(), "R.string.photoPickerNotFoundText",
                    Toast.LENGTH_LONG).show();
        }
    }

    // 请求Gallery程序
    protected void doPickPhotoFromGallery(myBaseFragment f) {
        try {
            // Launch picker to choose photo for selected contact
            Intent intent = getPhotoPickIntent();
            f.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(f.getActivity(), " R.string.photoPickerNotFoundText1",
                    Toast.LENGTH_LONG).show();
        }
    }
    // }

    /**
     * 拍照获取图片
     */

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * 用当前时间给取得的图片命名
     */
    public String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date) + ".jpg";
    }

    // 封装请求Gallery的intent
    public static Intent getPhotoPickIntent() {

        return getPhotoPickIntent(true);
    }

    public static Intent getPhotoPickIntent(Boolean iscorp) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (iscorp) {

            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 80);
            intent.putExtra("outputY", 80);
        }
        intent.putExtra("crop", "true");
        intent.setType("image/*");
        intent.putExtra("return-data", iscorp);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                getUriFromPath(getTempFilestr()));
        intent.putExtra("noFaceDetection", true); // no face detection
        return intent;
    }

    /**
     * Constructs an intent for image cropping. 调用图片剪辑程序
     */
    public static Intent getCropImageIntent(Uri photoUri, int outputX,
                                            int outputY, Boolean returndata) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        // intent.addCategory(Intent.CATEGORY_OPENABLE);
        // intent.setClassName("com.android.camera",
        // "com.android.camera.CropImage");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        // intent.putExtra("aspectX", 1);
        // intent.putExtra("aspectY", 1);
        if (outputX > 0)
            intent.putExtra("outputX", outputX);
        if (outputY > 0)
            intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", returndata);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                getUriFromPath(getTempFilestr()));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        return intent;
    }

    public static Intent getCropImageIntent(Uri photoUri) {
        return getCropImageIntent(photoUri, 0, 0, false);
    }

    public static Bitmap decodeUriAsBitmap(Uri uri, Context context) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /******
     * 缩放图片
     ******/
    public static Bitmap getScaledBitmap(byte[] data, int scale) {
        BitmapFactory.Options opts = new Options();

        // 设置这个，只得到Bitmap的属性信息放入opts，而不把Bitmap加载到内存中
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, opts);// (imageFile.getPath(),

        opts.inSampleSize = scale;
        // 内存不足时可被回收
        opts.inPurgeable = true;
        // 设置为false,表示不仅Bitmap的属性，也要加载bitmap
        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                opts);
        data = null;
        return bitmap;
    }

    /******
     * 缩放图片
     ******/
    public static Bitmap getScaledBitmap(byte[] data, int viewWidth,
                                         int viewHeight) {
        BitmapFactory.Options opts = new Options();

        // 设置这个，只得到Bitmap的属性信息放入opts，而不把Bitmap加载到内存中
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        int bitmapWidth = opts.outWidth;
        int bitmapHeight = opts.outHeight;
        int scale = Math
                .max(bitmapWidth / viewWidth, bitmapHeight / viewHeight);
        opts.inSampleSize = scale;
        // 内存不足时可被回收
        opts.inPurgeable = true;
        // 设置为false,表示不仅Bitmap的属性，也要加载bitmap
        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                opts);
        data = null;
        return bitmap;
    }

    /******
     * 缩放图片
     ******/
    public static Bitmap getScaledBitmap(Bitmap data, int viewWidth,
                                         int viewHeight) {
        int originWidth = data.getWidth();
        int originHeight = data.getHeight();
        if (originWidth < viewWidth && originHeight < viewHeight) {
            return data;
        }

        int width = originWidth;
        int height = originHeight;
        Bitmap bitmap;
        // 若图片过宽, 则保持长宽比缩放图片
        if (originWidth > viewWidth) {
            width = viewWidth;

            double i = originWidth * 1.0 / viewWidth;
            height = (int) Math.floor(originHeight / i);

            bitmap = Bitmap.createScaledBitmap(data, width, height, false);
            data = null;
            return bitmap;
        }

        // 若图片过长, 则从上端截取
        if (height > viewHeight) {
            height = viewHeight;

            bitmap = Bitmap.createBitmap(data, 0, 0, width, height);
            data = null;
            return bitmap;
        }
        return data;
    }

    /******
     * 删除文件片
     ******/
    public static void deletefile(String filepath) {
        File file = new File(filepath);
        if (file.exists())
            file.delete();
    }

    public static void saveBitmapFile(Bitmap bm, String path) throws Exception {
        FileOutputStream fos = null;
        try {
            /************ 保存图像 ******************/

            File pictureFile = new File(path);
            if (pictureFile == null) {
                Log.v("suntest",
                        "Error creating media file, check storage permissions: ");
                return;
            }
            fos = new FileOutputStream(pictureFile);
            // fos.write(data);
            // fos.close();
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);

        } catch (Exception e) {
            // TODO: handle exception
            throw e;

        } finally {
            fos.flush();
            fos.close();
        }
    }
}
