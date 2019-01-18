package cn.cricin.filepicker;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
public final class Preview {
  @SuppressLint("StaticFieldLeak")
  private static final Preview INSTANCE = new Preview();

  private ImageView mImageView;
  private TextView mContent;

  @NonNull
  public ImageView image() {
    return mImageView;
  }

  @NonNull
  public TextView text() {
    return mContent;
  }

  public void release() {
    this.mImageView = null;
    this.mContent = null;
  }

  public static Preview obtain(ImageView imageView, TextView content) {
    INSTANCE.mImageView = imageView;
    INSTANCE.mContent = content;
    return INSTANCE;
  }

  private Preview() {}
}
