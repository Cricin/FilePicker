package cn.cricin.filepicker.internal;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
/*package*/final class TextLoader {
  private static final TextLoader INSTANCE = new TextLoader();

  //max cache size for entry's value is 4kb
  private LruCache<File, String> mCache = new LruCache<File, String>(4096) {
    @Override
    protected int sizeOf(File key, String value) {
      return value.getBytes().length;
    }
  };
  private Executor mExecutor = new ThreadPoolExecutor(
    0, 3, 30, TimeUnit.SECONDS,
    new LinkedBlockingQueue<Runnable>(), Util.makeThreadFactory("TextLoader"));
  private Handler mHandler = new Handler(Looper.getMainLooper());

  static TextLoader getInstance() {
    return INSTANCE;
  }

  private void onLoaded(final TextView view, final String text, final File file) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        mCache.put(file, text);
        if (view != null && view.getTag() == file) {
          view.setText(text);
        }
      }
    });
  }

  void load(TextView view, final File file) {
    String s = mCache.get(file);
    if (s != null) {
      view.setText(s);
    } else {
      view.setTag(file);
      DecodeRunnable dr = new DecodeRunnable(view, file);
      mExecutor.execute(dr);
    }
  }

  void clearCache() {
    mCache.evictAll();
  }

  static class DecodeRunnable implements Runnable {
    private WeakReference<TextView> mTextViewRef;
    private File mFile;

    DecodeRunnable(TextView textView, File file) {
      mTextViewRef = new WeakReference<>(textView);
      mFile = file;
    }

    @Override
    public void run() {
      String text;
      try (InputStream in = new FileInputStream(mFile)) {
        InputStreamReader reader = new InputStreamReader(in);
        char[] temp = new char[(int) Math.min(mFile.length(), 20)];
        int read = reader.read(temp);
        text = new String(temp, 0, read);
      } catch (IOException e) {
        Log.w("FilePicker", "Unable to read content from file: " + mFile, e);
        text = "";
      }
      TextLoader.getInstance().onLoaded(mTextViewRef.get(), text, mFile);
    }
  }

}
