package cn.cricin.filepicker;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
public interface FileFilter {
  boolean accept(@NonNull File file);

  FileFilter EMPTY = new FileFilter() {
    @Override
    public boolean accept(@NonNull File file) {
      return true;
    }
  };
}
