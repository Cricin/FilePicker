package cn.cricin.filepicker.internal;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import cn.cricin.filepicker.FileInfo;

/**
 * Author      :     Cricin
 * Date        :     19/1/13
 */
/*package*/final class FileInfoImpl implements FileInfo {
  private static final boolean INIT_EAGERLY = false;

  private File mFile;
  private String extension;
  private String size;
  private String filename;
  private String modifyTime;
  private boolean selected;
  private boolean enabled;

  private FileInfoImpl(File file) {
    this.mFile = file;
    if (INIT_EAGERLY) {
      fileName();
      length();
      extension();
      modifyTime();
    }
  }

  @Override
  @NonNull
  public File file() {
    return mFile;
  }

  @Override
  @NonNull
  public String fileName() {
    if (filename != null) return filename;
    String path = mFile.getAbsolutePath();
    int index = path.lastIndexOf(File.separatorChar);
    return filename = path.substring(index + 1);
  }

  @Override
  @NonNull
  public String length() {
    if (size != null) return size;
    if (mFile.isDirectory()) return size = "";
    return size = Util.sizeToText(mFile.length());
  }

  @Override
  @NonNull
  public String extension() {
    if (extension != null) return extension;
    return extension = Util.getExtension(mFile);
  }

  @Override
  @NonNull
  @SuppressWarnings("deprecation")
  public String modifyTime() {
    if (modifyTime != null) return modifyTime;
    long lastModified = mFile.lastModified();
    Date date = new Date(lastModified);
    return modifyTime = String.format(Locale.CHINA, "%4d-%02d-%02d %02d:%02d:%02d",
      date.getYear() + 1900,
      date.getMonth() + 1,
      date.getDate(),
      date.getHours(),
      date.getMinutes(),
      date.getSeconds());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof FileInfoImpl) {
      return ((FileInfoImpl) obj).mFile.equals(mFile);
    }
    return false;
  }

  boolean isDir() {
    return mFile.isDirectory();
  }

  boolean isSelected() {return selected;}

  boolean isEnabled() {return enabled;}

  void setEnabled(boolean enabled) {this.enabled = enabled;}

  void setSelected(boolean selected) {this.selected = selected;}

  static FileInfoImpl valueOf(@NonNull File file) {return new FileInfoImpl(file);}
}
