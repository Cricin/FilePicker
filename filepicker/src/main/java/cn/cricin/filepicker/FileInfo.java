package cn.cricin.filepicker;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Project     :     FilePicker
 * Author      :     Cricin
 * Date        :     19/1/16
 */
public interface FileInfo {

  @NonNull
  File file();

  @NonNull
  String fileName();

  @NonNull
  String length();

  @NonNull
  String extension();

  @NonNull
  String modifyTime();

}
