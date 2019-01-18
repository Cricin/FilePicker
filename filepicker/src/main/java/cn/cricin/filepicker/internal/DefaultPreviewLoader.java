package cn.cricin.filepicker.internal;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import cn.cricin.filepicker.FileInfo;
import cn.cricin.filepicker.PreviewLoader;
import cn.cricin.filepicker.Preview;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
public class DefaultPreviewLoader implements PreviewLoader {
  private static boolean sFoundGlide;
  private static boolean sFoundPicasso;

  private static final List<String> sSupportedImageTypes =
    Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp");
  private static final List<String> sSupportedVideoTypes =
    Arrays.asList("mp4", "mkv", "webm", "avi");
  private static final List<String> sSupportedTextTypes =
    Arrays.asList("txt", "html", "css", "log", "json", "xml");

  static {
    try {
      Class.forName("com.bumptech.glide.Glide");
      sFoundGlide = true;
    } catch (ClassNotFoundException ignore) {
    }
    try {
      Class.forName("com.squareup.picasso.Picasso");
      sFoundPicasso = true;
    } catch (ClassNotFoundException ignore) {
    }
  }

  @Override
  public void loadPreview(@NonNull Context ctx, @NonNull FileInfo file, @NonNull Preview preview) {
    @PreviewType int type = getLoadType(file);
    if (type == TYPE_IMAGE) {
      if (sFoundGlide) {
        Glide.with(ctx).load(file.file()).into(preview.image());
      } else if (sFoundPicasso) {
        Picasso.get().load(file.file()).into(preview.image());
      } else {
        ImageLoader.getInstance().load(preview.image(), file.file());
      }
    } else if (type == TYPE_VIDEO) {
      if (sFoundGlide) {
        Glide.with(ctx).load(file.file()).into(preview.image());
      } else {
        ImageLoader.getInstance().load(preview.image(), file.file());
      }
    } else if (type == TYPE_TEXT) {
      TextLoader.getInstance().load(preview.text(), file.file());
    }
  }

  @PreviewType
  @Override
  public int getLoadType(FileInfo file) {
    final String ext = file.extension();
    if (sSupportedImageTypes.contains(ext)) {
      return TYPE_IMAGE;
    } else if (sSupportedVideoTypes.contains(ext)) {
      return TYPE_VIDEO;
    } else if (sSupportedTextTypes.contains(ext)) {
      return TYPE_TEXT;
    }
    return TYPE_NONE;
  }
}
