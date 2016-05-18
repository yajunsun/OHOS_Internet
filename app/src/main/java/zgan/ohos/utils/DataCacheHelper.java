package zgan.ohos.utils;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import zgan.ohos.MyApplication;
import zgan.ohos.R;

/**
 * Created by Administrator on 16-3-21.
 */
public class DataCacheHelper {
    private static final String TAG = "sun.DataCache";
    /*********
     * disclrucache
     *********/
    private static final int MESSAGE_POST_RESULT = 1;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10000L;
    private static final int DISK_CACHE_INDEX = 0;
    private static final int TAG_KEY_URI = R.id.imageloader_uri;
    private static boolean mIsDiskLruCacheCreated = false;
    private static int IO_BUFFER_SIZE = 1 * 1024;
    private static final long DISK_CACHE_SIZE = 1004 * 1024 * 10;
    /************************************/

    /*************
     * memerylrucache
     *************/
    private static int maxMemery = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static LruCache<String, String> mCaches = new LruCache<String, String>(
            maxMemery / 20) {
        @Override
        protected int sizeOf(String key, String str) {
            return str.length() * 4;
        }
    };
    /*****************************************/
    public static DiskLruCache diskLruCache;

    static {
        try {
            if (!getDiskCacheDir().exists())
                getDiskCacheDir().mkdir();
            if (ImageLoader.getUsableSpace(getDiskCacheDir()) > DISK_CACHE_SIZE) {
                diskLruCache = DiskLruCache.open(getDiskCacheDir(), 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            }
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static String loadData(String uri) {
        String key = ImageLoader.hashKeyFromUrl(uri);
        String result = loadDataFrommemery(key);
        if (result != null && result.length() > 0)
            return result;
        result = loadDataFromdisk(key);
        if (result != null && result.length() > 0)
            return result;
        return "";
    }

    private static String loadDataFrommemery(String key) {
        return Get4Cache(key);
    }

    private static String loadDataFromdisk(String key) {
        if (diskLruCache == null)
            return "";
        String strData = "";
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);

            byte[] buffer = new byte[1024];
            if (snapshot != null) {
                FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(buffer, 0, 1024);
                strData = new String(buffer).trim();
                // buffer.toString();
                //Add2Cache(key, str.toString());
                fileInputStream.close();
            }
            return strData;
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }
        return "";
    }


    public static void Add2Cache(String key, String str) {
        if (Get4Cache(key) == null)
            mCaches.put(key, str);
    }

    public static String Get4Cache(String key) {
        return mCaches.get(key);
    }

    public static void Remove4Cache(String key) {
        if (Get4Cache(key) != null)
            mCaches.remove(key);
    }

    public static void add2DiskCache(final String uri, final InputStream is) {
//        Runnable addTsk = new Runnable() {
//            @Override
//            public void run() {
                OutputStream outputStream = null;
                String key = ImageLoader.hashKeyFromUrl(uri);
                try {
                    DiskLruCache.Editor editor = diskLruCache.edit(key);
                    BufferedInputStream in;
                    BufferedOutputStream out;
                    if (editor != null) {
                        outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                        in = new BufferedInputStream(is, IO_BUFFER_SIZE);
                        out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        in.close();
                        out.flush();
                        out.close();
                        editor.commit();
                        diskLruCache.flush();
                        outputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }
//        };
//        THREAD_POOL_EXECUTOR.execute(addTsk);
    }

    public static void add2DiskCache(final String uri, final String data) throws IOException {
        String key = ImageLoader.hashKeyFromUrl(uri);
        Add2Cache(key, data);
        if (diskLruCache == null)
            return;
        add2DiskCache(uri,new ByteArrayInputStream(data.getBytes()));
    }

    static File getDiskCacheDir() {
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = MyApplication.context.getExternalCacheDir().getPath();
        } else {
            cachePath = MyApplication.context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + "datacachefiles");
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "datacache#" + mCount.getAndIncrement());
        }
    };
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    // private static Handler mMainHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            LoaderResult result = (LoaderResult) msg.obj;
//            ImageView imageView = result.imageView;
//            imageView.setImageBitmap(result.bitmap);
//            String uri = (String) imageView.getTag(TAG_KEY_URI);
//            if (uri.equals(result.uri))
//                imageView.setImageBitmap(result.bitmap);
//            else {
//                Log.v(TAG, "set image bitmap,but url has changed,ignored!");
//                Log.i(TAG, "set image bitmap,but url has changed,ignored!");
//            }
//}
//};
}
