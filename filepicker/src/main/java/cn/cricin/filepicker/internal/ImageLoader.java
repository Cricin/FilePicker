package cn.cricin.filepicker.internal;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

/**
 * Author      :     Cricin
 * Date        :     19/1/15
 */
/*package*/final class ImageLoader {
  private static final ImageLoader INSTANCE = new ImageLoader();

  private LruCache<File, Bitmap> mCache;
  private Executor mExecutor;
  private Handler mHandler;

  static ImageLoader getInstance() {
    return INSTANCE;
  }

  private void onLoaded(final ImageView view, final Bitmap bitmap, final File file) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        checkMainThread();
        mCache.put(file, bitmap);
        if (view != null && view.getTag() == file) {
          view.setImageBitmap(bitmap);
          view.setTag(null);
        }
      }
    });
  }

  void load(ImageView view, final File file) {
    checkMainThread();
    initIfNeeded(view.getContext());

    Bitmap b = mCache.get(file);
    if (b != null) {
      view.setImageBitmap(b);
      return;
    }
    view.setTag(file);
    if (isVideoFile(file)) {
      mExecutor.execute(new VideoDecodeRunnable(view, file));
    } else {
      mExecutor.execute(new ImageDecodeRunnable(view, file));
    }
  }

  void clearCache() {
    if (mCache != null) {
      mCache.evictAll();
    }
  }

  private boolean isVideoFile(File file) {
    return Arrays.asList("mp4", "mkv", "webm", "avi").contains(Util.getExtension(file));
  }

  @SuppressWarnings("ConstantConditions")
  private void initIfNeeded(Context ctx) {
    if (mCache != null) return;

    ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
    boolean largeHeap = (ctx.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
    int memoryClass = largeHeap ? am.getLargeMemoryClass() : am.getMemoryClass();
    // Target ~15% of the available heap.
    long memory = (1024L * 1024L * memoryClass / 7);
    mCache = new LruCache<File, Bitmap>((int) (memory / 8)) {
      @Override
      protected int sizeOf(File key, Bitmap value) {
        return getBitmapByteSize(value);
      }
    };
    mExecutor = new ThreadPoolExecutor(
      0, 3, 30, TimeUnit.SECONDS,
      new LinkedBlockingQueue<Runnable>(), Util.makeThreadFactory("ImageLoader"));
    mHandler = new Handler(Looper.getMainLooper());
  }

  static class ImageDecodeRunnable implements Runnable {
    private WeakReference<ImageView> mViewRef;
    private File mFile;

    ImageDecodeRunnable(ImageView view, File file) {
      mViewRef = new WeakReference<>(view);
      mFile = file;
    }

    @Override
    public void run() {
      BitmapFactory.Options options = new BitmapFactory.Options();
      BitmapFactory.decodeFile(mFile.getAbsolutePath(), options);
      options.inJustDecodeBounds = false;
      ImageView view = mViewRef.get();
      if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
        options.inSampleSize = calculateInSampleSize(options, view.getWidth(), view.getHeight());
      }
      Bitmap bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath(), options);
      ImageLoader.getInstance().onLoaded(mViewRef.get(), bitmap, mFile);
    }

    int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
      final int width = options.outWidth;
      final int height = options.outHeight;
      int inSampleSize = 1;

      if (height > reqHeight || width > reqWidth) {
        //使用需要的宽高的最大值来计算比率
        final int suitedValue = reqHeight > reqWidth ? reqHeight : reqWidth;
        final int heightRatio = Math.round((float) height / (float) suitedValue);
        final int widthRatio = Math.round((float) width / (float) suitedValue);

        inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;//用最大
      }

      return inSampleSize;
    }
  }
  //------- end of ImageDecodeRunnable ------//

  static class VideoDecodeRunnable implements Runnable {
    private WeakReference<ImageView> mViewRef;
    private File mFile;

    VideoDecodeRunnable(ImageView view, File file) {
      mViewRef = new WeakReference<>(view);
      mFile = file;
    }

    @Override
    public void run() {
      MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
      mediaMetadataRetriever.setDataSource(mFile.getAbsolutePath());

      int outWidth = Integer.MIN_VALUE;
      int outHeight = Integer.MIN_VALUE;
      ImageView view = mViewRef.get();
      if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
        outWidth = view.getWidth();
        outHeight = view.getHeight();
      }

      Bitmap result = null;
      try {
        result = decodeFrame(mediaMetadataRetriever, outWidth, outHeight);
      } catch (RuntimeException e) {
        // MediaMetadataRetriever APIs throw generic runtime exceptions when given invalid data.
      } finally {
        mediaMetadataRetriever.release();
      }

      if (result != null) {
        ImageLoader.getInstance().onLoaded(mViewRef.get(), result, mFile);
      }
    }

    @Nullable
    private static Bitmap decodeFrame(
      MediaMetadataRetriever mediaMetadataRetriever,
      int outWidth,
      int outHeight) {
      Bitmap result = null;
      // Arguably we should handle the case where just width or just height is set to
      // Target.SIZE_ORIGINAL. Up to and including OMR1, MediaMetadataRetriever defaults to setting
      // the dimensions to the display width and height if they aren't specified (ie
      // getScaledFrameAtTime is not used). Given that this is an optimization only if
      // Target.SIZE_ORIGINAL is not used and not using getScaledFrameAtTime ever would match the
      // behavior of Glide in all versions of Android prior to OMR1, it's probably fine for now.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
        && outWidth != Integer.MIN_VALUE
        && outHeight != Integer.MIN_VALUE) {
        result =
          decodeScaledFrame(
            mediaMetadataRetriever, outWidth, outHeight);
      }

      if (result == null) {
        result = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
      }

      return result;
    }

    @TargetApi(Build.VERSION_CODES.O_MR1)
    private static Bitmap decodeScaledFrame(
      MediaMetadataRetriever mediaMetadataRetriever,
      int outWidth,
      int outHeight) {
      try {
        int originalWidth =
          Integer.parseInt(
            mediaMetadataRetriever.extractMetadata(
              MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int originalHeight =
          Integer.parseInt(
            mediaMetadataRetriever.extractMetadata(
              MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int orientation =
          Integer.parseInt(
            mediaMetadataRetriever.extractMetadata(
              MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

        if (orientation == 90 || orientation == 270) {
          int temp = originalWidth;
          //noinspection SuspiciousNameCombination
          originalWidth = originalHeight;
          originalHeight = temp;
        }

        float widthPercentage = outWidth / (float) originalWidth;
        float heightPercentage = outHeight / (float) originalHeight;

        float scaleFactor = Math.min(1.f, Math.min(widthPercentage, heightPercentage));

        int decodeWidth = Math.round(scaleFactor * originalWidth);
        int decodeHeight = Math.round(scaleFactor * originalHeight);

        return mediaMetadataRetriever.getScaledFrameAtTime(
          1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, decodeWidth, decodeHeight);
      } catch (Throwable t) {
        // This is aggressive, but we'd rather catch errors caused by reading and/or parsing metadata
        // here and fall back to just decoding the frame whenever possible. If the exception is thrown
        // just from decoding the frame, then it will be thrown and exposed to callers by the method
        // below.
        Log.d("VideoDecodeRunnable", "Exception trying to decode frame on oreo+", t);
        return null;
      }
    }
  }
  //------ end of VideoDecodeRunnable -------//

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private static int getBitmapByteSize(@NonNull Bitmap bitmap) {
    // The return value of getAllocationByteCount silently changes for recycled bitmaps from the
    // internal buffer size to row bytes * height. To avoid random inconsistencies in caches, we
    // instead assert here.
    if (bitmap.isRecycled()) {
      throw new IllegalStateException("Cannot obtain size for recycled Bitmap: " + bitmap
        + "[" + bitmap.getWidth() + "x" + bitmap.getHeight() + "] " + bitmap.getConfig());
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // Workaround for KitKat initial release NPE in Bitmap, fixed in MR1. See issue #148.
      try {
        return bitmap.getAllocationByteCount();
      } catch (@SuppressWarnings("PMD.AvoidCatchingNPE") NullPointerException e) {
        // Do nothing.
      }
    }
    return bitmap.getHeight() * bitmap.getRowBytes();
  }

  private static void checkMainThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("Must called on main thread");
    }
  }


}
