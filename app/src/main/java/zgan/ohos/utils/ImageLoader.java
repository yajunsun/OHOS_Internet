package zgan.ohos.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import zgan.ohos.Contracts.IImageloader;
import zgan.ohos.MyApplication;
import zgan.ohos.R;

/**
 * Created by yajunsun on 2015/11/12.
 */
public final class ImageLoader {
    private static final String TAG = "sun.ImageLoader";
    private static final int MESSAGE_POST_RESULT = 1;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10000L;
    private static final int DISK_CACHE_INDEX = 0;
    private static final int TAG_KEY_URI = R.id.imageloader_uri;
    private static boolean mIsDiskLruCacheCreated = false;
    private static int IO_BUFFER_SIZE = 8 * 1024;
    private static final long DISK_CACHE_SIZE = 1004 * 1024 * 80;
    private static ImageResizer mImageResizer = new ImageResizer();
    private static int maxMemery = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static LruCache<String, Bitmap> imageCaches = new LruCache<String, Bitmap>(
            maxMemery / 8) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    };
    public static DiskLruCache diskLruCache;

    static {
        try {
            if (!getDiskCacheDir().exists())
                getDiskCacheDir().mkdir();
            if (getUsableSpace(getDiskCacheDir()) > DISK_CACHE_SIZE) {
                diskLruCache = DiskLruCache.open(getDiskCacheDir(), 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            }
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public ImageLoader() {
    }

    /**
     * 加载drawable资源图片
     *
     * @param context
     * @param resId      资源ID
     * @param mImageView imageview控件
     * @param download   回调接口
     * @param reqWidth   imageview控件的宽
     * @param reqHeight  imageview控件的高
     */
    public void loadDrawableRS(Context context, int resId, View mImageView, IImageloader download, int reqWidth, int reqHeight) {
        if (reqHeight == -1)
            reqHeight = mImageView.getHeight();
        if (reqWidth == -1)
            reqWidth = mImageView.getWidth();
        int w = reqWidth, h = reqHeight;
        Bitmap currBitmap
//                = imageCaches.get(String.valueOf(resId));
//        if (currBitmap == null) {
                = decodeSampledBitmapFromResource(context.getResources(), resId, reqWidth, reqHeight, w, h);
        //}
        if (currBitmap != null) {
            //Add2Cache(String.valueOf(resId), currBitmap);
            //mImageView.setImageBitmap(currBitmap);
            if (download != null)
                download.onDownloadSucc(currBitmap, String.valueOf(resId), mImageView, w, h);
        }
    }

    /************
     * local method
     ************/
    public static void Add2Cache(String key, Bitmap bitmap) {
        if (Get4Cache(key) == null)
            imageCaches.put(key, bitmap);
    }

    public static Bitmap Get4Cache(String key) {
        return imageCaches.get(key);
    }

    public static void Remove4Cache(String key) {
        if (Get4Cache(key) != null)
            imageCaches.remove(key);
    }


    public static void bindBitmap(final String uri, final ImageView imageView) {
        bindBitmap(uri, imageView, 0, 0);
    }

    public static void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight) {
        bindBitmap(uri, imageView, 0, 0, null);
    }

    public static void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight, final IImageloader binded) {
        imageView.setTag(TAG_KEY_URI, uri);
        //imageView.setImageDrawable(MyApplication.context.getResources().getDrawable(R.drawable.nullpic));
        final Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTast = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
                if (bitmap != null) {
      //              if (binded != null)
//                        binded.onDownloadSucc(bitmap, uri, imageView, bitmap.getWidth(), bitmap.getHeight());
                    LoaderResult result = new LoaderResult(imageView, uri, bitmap,binded);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTast);
    }


    public static Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            Log.i(TAG, "load BitMap from memory");
            return bitmap;
        }
        try {
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
            if (bitmap != null) {
                Log.i(TAG, "load BitMap from disk");
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
            Log.i(TAG, "load BitMap from Http,url:" + uri);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (bitmap == null && !mIsDiskLruCacheCreated) {
            Log.i(TAG, "encounter error,DiskLruCache is not created.");
            bitmap = downloadBitmapFromUrl(uri);
        }
        return bitmap;
    }

    private static Bitmap loadBitmapFromMemCache(String url) {
        final String key = hashKeyFromUrl(url);
        Bitmap bitmap = Get4Cache(key);
        return bitmap;
    }

    private static synchronized Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper())
            throw new RuntimeException("can not vist network from UI Thread.");
        if (diskLruCache == null)
            return null;
        OutputStream outputStream = null;
        try {
            String key = hashKeyFromUrl(url);
            DiskLruCache.Editor editor = diskLruCache.edit(key);
            if (editor != null) {
                outputStream = editor.newOutputStream(DISK_CACHE_INDEX);

                if (downloadUrlToStream(url, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                diskLruCache.flush();

            }
            try {
                return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (OutOfMemoryError ofMemoryError) {
            ofMemoryError.printStackTrace();
            generalhelper.ToastShow(MyApplication.context, "加载失败");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputStream.close();
        }
        return null;
    }

    private static Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws Exception {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.v(TAG, "load bitmap from UI Thread,it is not recommended!");
            Log.i(TAG, "load bitmap from UI Thread,it is not recommended!");
        }
        if (diskLruCache == null)
            return null;
        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
//            try {
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
            if (bitmap != null)
                Add2Cache(key, bitmap);
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            finally {
            fileInputStream.close();
            //}
        }
        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                  int reqWidth, int reqHeight, int w, int h) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        w = options.outWidth;
        h = options.outHeight;
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public void getBitmapFromFile(String imageName, int viewWidth, int viewHeigth) {
        if (imageName != null) {
            File file;
            String real_path;
            try {
                real_path = picturefile.getappdirpic();
                if (imageName.startsWith(real_path))
                    file = new File(imageName);
                else
                    file = new File(real_path, imageName);
                if (file.exists()) {
                    FileInputStream fs = new FileInputStream(file);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(fs, null, options);
                    int inSampleSize;
                    inSampleSize = calculateInSampleSize(options, viewWidth, viewHeigth);
                    options.inSampleSize = inSampleSize;
                    options.inJustDecodeBounds = false;
                    fs.close();
                    fs = new FileInputStream(file);
                    Bitmap currBitmap = BitmapFactory.decodeStream(fs, null, options);
                    Add2Cache(hashKeyFromUrl(imageName), currBitmap);
                    fs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean downloadUrlToStream(String urlString, OutputStream outputStream) throws Exception {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            in.close();
            out.flush();
            out.close();
        }
        return false;
    }

    private static Bitmap downloadBitmapFromUrl(final String uri) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1)
                sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    static File getDiskCacheDir() {
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = MyApplication.context.getExternalCacheDir().getPath();
        } else {
            cachePath = MyApplication.context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + "imagecachefiles");
    }

    public static long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            return path.getUsableSpace();
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "ImageLoader#" + mCount.getAndIncrement());
        }
    };
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    private static Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                final LoaderResult result = (LoaderResult) msg.obj;
                ImageView imageView = result.imageView;
                imageView.setImageBitmap(result.bitmap);
                String uri = (String) imageView.getTag(TAG_KEY_URI);
                if (uri.equals(result.uri)) {
                    imageView.setImageBitmap(result.bitmap);
                    if (result.callback!=null) {
                        Log.i("suntest", "load bitmap callback");
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                result.callback.onDownloadSucc(result.bitmap, result.uri, result.imageView, result.bitmap.getWidth(), result.bitmap.getHeight());
                            }
                        }, 200);
                    }
                }
                else {
                    Log.i(TAG, "set image bitmap,but url has changed,ignored!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                generalhelper.ToastShow(MyApplication.context, "图片过大，无法加载~");
            }
        }
    };

    private static class LoaderResult {
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;
        public IImageloader callback;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap,IImageloader _call) {
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
            callback=_call;
        }
    }
    /****************************************************/
}
