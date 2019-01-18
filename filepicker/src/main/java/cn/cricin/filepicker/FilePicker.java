package cn.cricin.filepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.cricin.filepicker.internal.FilePickerActivity;
import cn.cricin.filepicker.internal.Util;

/**
 * Author      :     Cricin
 * Date        :     19/1/13
 */
@SuppressWarnings("unused")
public final class FilePicker {
  public static final String RESULT_KEY = "file_picker_files";
  private static final int DEFAULT_REQUEST_CODE = 523;

  private final Activity mActivity;
  private final Fragment mFragment;
  private final Options mOptions;

  private FilePicker(Activity activity, Fragment fragment) {
    this.mActivity = activity;
    this.mFragment = fragment;
    this.mOptions = new Options();
  }

  @NonNull
  @CheckResult
  public static FilePicker with(@NonNull Activity activity) {
    return new FilePicker(activity, null);
  }

  @NonNull
  @CheckResult
  public static FilePicker with(@NonNull Context context) {
    Activity activity = Util.findActivity(context);
    if (activity != null) {
      return with(activity);
    } else {
      throw new IllegalArgumentException("Context: "
        + context.getClass().getSimpleName() + " is not an Activity");
    }
  }

  @NonNull
  @CheckResult
  public static FilePicker with(@NonNull Fragment fragment) {
    return new FilePicker(null, fragment);
  }

  @NonNull
  @CheckResult
  public FilePicker maxCount(int maxCount) {
    mOptions.mMaxCount = maxCount;
    return this;
  }

  @NonNull
  @CheckResult
  public FilePicker previewMode(PreviewMode mode) {
    mOptions.mPreviewMode = mode;
    return this;
  }

  @NonNull
  @CheckResult
  public FilePicker theme(int theme) {
    mOptions.mTheme = theme;
    return this;
  }

  @NonNull
  @CheckResult
  public FilePicker iconLoader(PreviewLoader previewLoader) {
    mOptions.mPreviewLoader = previewLoader;
    return this;
  }

  /**
   * @see FilterBuilder
   */
  @NonNull
  @CheckResult
  public FilePicker filter(FileFilter fileFilter) {
    mOptions.mFileFilter = fileFilter;
    return this;
  }

  @NonNull
  @CheckResult
  public FilePicker title(String title) {
    mOptions.mTitle = title;
    return this;
  }

  /**
   * @see Sorts
   */
  @NonNull
  @CheckResult
  public FilePicker sortWith(Comparator<File> sort) {
    mOptions.mSort = sort;
    return this;
  }

  public void start(int requestCode) {
    Context ctx = mActivity == null ? mFragment.getContext() : mActivity;
    Intent intent = new Intent(ctx, FilePickerActivity.class);
    mOptions.attachToIntent(intent);
    if (mFragment != null) mFragment.startActivityForResult(intent, requestCode);
    else mActivity.startActivityForResult(intent, requestCode);
  }

  /**
   * @see #DEFAULT_REQUEST_CODE
   */
  public void start() {
    start(DEFAULT_REQUEST_CODE);
  }

  @NonNull
  public static List<File> obtainResult(Intent data) {
    if (data == null) return Collections.emptyList();
    String[] extra = data.getStringArrayExtra(RESULT_KEY);
    if (extra == null || extra.length == 0) return Collections.emptyList();
    List<File> result = new ArrayList<>(extra.length);
    for (String s : extra) {
      result.add(new File(s));
    }
    return result;
  }
}
