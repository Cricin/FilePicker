package cn.cricin.filepicker;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author      :     Cricin
 * Date        :     19/1/13
 */
public interface PreviewLoader {

  int TYPE_NONE = 0;
  int TYPE_IMAGE = 1;
  int TYPE_TEXT = 2;
  int TYPE_VIDEO = 3;

  @IntDef({TYPE_NONE, TYPE_IMAGE, TYPE_TEXT, TYPE_VIDEO})
  @Target({ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
  @Retention(RetentionPolicy.SOURCE)
  @interface PreviewType {}

  void loadPreview(@NonNull Context ctx, @NonNull FileInfo file, @NonNull Preview preview);

  @PreviewType
  int getLoadType(FileInfo file);
}