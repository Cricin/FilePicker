package cn.cricin.filepicker;

import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.io.File;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import cn.cricin.filepicker.internal.DefaultPreviewLoader;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
public final class Options {
  private static final String OPTIONS_KEY = "options_key";
  private static final SparseArray<Options> OPTIONS_ARRAY = new SparseArray<>();
  private static final AtomicInteger KEY = new AtomicInteger();

  private int mKey = KEY.getAndIncrement();
  public int mMaxCount = 1;
  public PreviewMode mPreviewMode = PreviewMode.LIST;
  public int mTheme = R.style.FilePicker;
  public PreviewLoader mPreviewLoader = new DefaultPreviewLoader();
  public FileFilter mFileFilter = FileFilter.EMPTY;
  public Comparator<File> mSort = Sorts.NO_SORT;
  public @DrawableRes int mDirDrawableId = R.drawable.file_picker_icon_folder;
  public @DrawableRes int mFileDrawableId = R.drawable.file_picker_icon_file;
  public String mTitle = "选择文件";


  void attachToIntent(@NonNull Intent intent) {
    OPTIONS_ARRAY.put(mKey, this);
    intent.putExtra(OPTIONS_KEY, mKey);
  }

  public static Options get(Intent intent) {
    int key = intent.getIntExtra(OPTIONS_KEY, -1);
    if (key == -1) throw new IllegalStateException("no options found from intent: " + intent);
    Options options = OPTIONS_ARRAY.get(key);
    OPTIONS_ARRAY.delete(key);
    return options;
  }

  Options() {}
}
