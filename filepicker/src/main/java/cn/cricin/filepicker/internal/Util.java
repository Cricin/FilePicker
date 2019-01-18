package cn.cricin.filepicker.internal;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
public final class Util {
  private static SparseIntArray sColorCache = new SparseIntArray();

  @ColorInt
  static int getColor(Context ctx, @AttrRes int attr) {
    @ColorInt int color = sColorCache.get(attr);
    if (color == 0) {
      TypedArray a = ctx.obtainStyledAttributes(new int[]{attr});
      color = a.getColor(0, -1);
      sColorCache.put(attr, color);
      a.recycle();
    }
    return color;
  }

  static void checkFilePickerTheme(Context ctx) {
    final int[] filePickerCheckAttrs = {cn.cricin.filepicker.R.attr.file_picker_main_color};
    TypedArray a = ctx.obtainStyledAttributes(filePickerCheckAttrs);
    boolean hasValue = a.hasValue(0);
    a.recycle();
    if (!hasValue) {
      throw new IllegalArgumentException("You need to use a FilePicker theme "
        + "(or descendant) with this library.");
    }
  }

  static ThreadFactory makeThreadFactory(final String prefix) {
    return new ThreadFactory() {
      AtomicInteger index = new AtomicInteger();

      @Override
      public Thread newThread(@NonNull Runnable r) {
        Thread result = new Thread(r);
        result.setDaemon(true);
        result.setName(prefix + " #" + index.getAndIncrement());
        return result;
      }
    };
  }

  static String sizeToText(long len) {
    if (len < 1024) return len + "B";
    if (len < 1024 * 1024) {
      return String.format(Locale.US, "%.1fKB", len / (float) (1 << 10));
    }
    if (len < 1024 * 1024 * 1024) {
      return String.format(Locale.US, "%.1fMB", len / (float) (1 << 20));
    }
    return String.format(Locale.US, "%.1fGB", len / (float) (1 << 30));
  }

  static List<File> selectDirs(@NonNull File[] files) {
    List<File> result = new ArrayList<>(files.length);
    for (File file : files) {
      if (file.isDirectory()) result.add(file);
    }
    return result;
  }

  static List<File> selectFiles(@NonNull File[] files) {
    List<File> result = new ArrayList<>(files.length);
    for (File file : files) {
      if (file.isFile()) result.add(file);
    }
    return result;
  }

  @Nullable
  public static Activity findActivity(Context ctx) {
    if (ctx == null) return null;
    if (ctx instanceof Activity) return (Activity) ctx;
    if (ctx instanceof ContextWrapper) {
      return findActivity(((ContextWrapper) ctx).getBaseContext());
    }
    return null;
  }

  public static String getExtension(File file) {
    String absPath = file.getAbsolutePath();
    int lastIndexOfDot = absPath.lastIndexOf('.');
    if (lastIndexOfDot == -1) {
      return "?";
    }
    int index = absPath.lastIndexOf(File.separatorChar);//以.开始的文件
    if (index + 1 == lastIndexOfDot) {
      return "?";
    }

    if (lastIndexOfDot < absPath.length() - 1) {
      String ext = absPath.substring(lastIndexOfDot + 1);
      if (ext.length() > 4 || ext.length() < 2) return "?";
      return ext.toLowerCase();
    }
    return "?";
  }

  private Util() {}
}
