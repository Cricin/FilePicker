package cn.cricin.filepicker.internal;

import android.support.annotation.ColorInt;

import java.util.HashMap;

/**
 * Author      :     Cricin
 * Date        :     19/1/15
 */
/*package*/final class FileTypeTint {
  private static final HashMap<String, Integer> sExtColorMapping = new HashMap<>();

  @ColorInt
  static int getColorForExt(String ext) {
    if ("?".equals(ext)) return 0xFFDDDDDD;
    Integer i = sExtColorMapping.get(ext);
    if (i == null) {
      int color = nextColor(ext);
      sExtColorMapping.put(ext, color);
      return color;
    }
    return sExtColorMapping.get(ext);
  }

  @ColorInt
  private static int nextColor(String ext) {
    int value = 0;
    for (int i = 0; i < ext.length(); i++) {
      value += ext.charAt(i);
    }
    return 0xFF000000 | (int) (nextInt(value, 1000) / (float) 1000 * 0xFFFFFF);
  }

  @SuppressWarnings({"SameParameterValue", "StatementWithEmptyBody"})
  private static int nextInt(int seed, int bound) {
    int r = next(seed);
    int m = bound - 1;
    if ((bound & m) == 0)  // i.e., bound is a power of 2
      r = (int) ((bound * (long) r) >> 31);
    else {
      for (int u = r;
           u - (r = u % bound) + m < 0;
           u = next(seed))
        ;
    }
    return r;
  }

  private static final long multiplier = 0x5DEECE66DL;
  private static final long addend = 0xBL;
  private static final long mask = (1L << 48) - 1;

  private static int next(int seed) {
    long nextseed = (seed * multiplier + addend) & mask;
    return (int) (nextseed >>> (48 - 31));
  }

  private FileTypeTint() {}
}
