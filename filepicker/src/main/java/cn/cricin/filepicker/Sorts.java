package cn.cricin.filepicker;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;

import cn.cricin.filepicker.internal.Util;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Sorts {

  public static final Comparator<File> NO_SORT = new Comparator<File>() {
    @Override
    public int compare(File o1, File o2) {
      return 0;
    }
  };

  public static Comparator<File> byNameAsc() {
    return new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        return o1.getName().compareTo(o2.getName());
      }
    };
  }

  public static Comparator<File> byNameDesc() {
    return Collections.reverseOrder(byNameAsc());
  }

  public static Comparator<File> byExtensionAsc() {
    return new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        return Util.getExtension(o1).compareTo(Util.getExtension(o2));
      }
    };
  }

  public static Comparator<File> byExtensionDesc() {
    return Collections.reverseOrder(byExtensionAsc());
  }

  public static Comparator<File> byModifyTimeAsc() {
    return new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        return Long.compare(o1.lastModified(), o2.lastModified());
      }
    };
  }

  public static Comparator<File> byModifyTimeDesc() {
    return Collections.reverseOrder(byModifyTimeAsc());
  }

  public static Comparator<File> byLengthAsc() {
    return new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        return Long.compare(o1.length(), o2.length());
      }
    };
  }

  public static Comparator<File> byLengthDesc() {
    return Collections.reverseOrder(byLengthAsc());
  }

  private Sorts() {}
}
