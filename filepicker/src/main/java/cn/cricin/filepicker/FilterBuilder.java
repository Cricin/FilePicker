package cn.cricin.filepicker;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
@SuppressWarnings("unused")
public final class FilterBuilder {
  private boolean mAnd;
  private boolean mInclude;
  private FileFilter mPrevious;

  private FilterBuilder(boolean and, boolean include, FileFilter filter) {
    this.mAnd = and;
    this.mPrevious = filter;
    this.mInclude = include;
  }

  public Operator sizeLassThan(final long byteCount) {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.length() < byteCount;
      }
    }));
  }

  public Operator sizeGreatThan(final long byteCount) {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.length() > byteCount;
      }
    }));
  }

  public Operator prefixBy(final String prefix) {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.getAbsolutePath().startsWith(prefix);
      }
    }));
  }

  public Operator suffixBy(final String suffix) {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.getAbsolutePath().endsWith(suffix);
      }
    }));
  }

  public Operator directory() {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.isDirectory();
      }
    }));
  }

  public Operator file() {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.isFile();
      }
    }));
  }

  public Operator modifyTimeLessThan(final long epochMillis) {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.lastModified() < epochMillis;
      }
    }));
  }

  public Operator modifyTimeGreatThan(final long epochMillis) {
    return new Operator(createFilter(new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        return file.lastModified() > epochMillis;
      }
    }));
  }

  public Operator filter(FileFilter filter){
    return new Operator(createFilter(filter));
  }

  private FileFilter createFilter(final FileFilter filter) {
    return new FileFilter() {
      @Override
      public boolean accept(@NonNull File file) {
        if (mPrevious == null) {
          if (mInclude) return filter.accept(file);
          else return !filter.accept(file);
        }
        boolean accept = mPrevious.accept(file);
        if (mAnd) {
          if (!accept) return false;
          if (mInclude) return filter.accept(file);
          else return !filter.accept(file);
        } else {
          if (accept) return true;
          if (mInclude) return filter.accept(file);
          else return !filter.accept(file);
        }
      }
    };
  }

  public static FilterBuilder include() {
    return new FilterBuilder(false, true, null);
  }

  public static FilterBuilder exclude() {
    return new FilterBuilder(false, false, null);
  }

  public static class Operator {
    private FileFilter mFilter;

    private Operator(FileFilter filter) {
      mFilter = filter;
    }

    public FilterBuilder andExclude() {
      return new FilterBuilder(true, false, mFilter);
    }

    public FilterBuilder andInclude() {
      return new FilterBuilder(true, true, mFilter);
    }

    public FilterBuilder orInclude() {
      return new FilterBuilder(false, true, mFilter);
    }

    public FilterBuilder orExclude() {
      return new FilterBuilder(false, false, mFilter);
    }

    public FileFilter build() {
      if (mFilter != null) return mFilter;
      throw new IllegalStateException("You can not call directly after include() or exclude()");
    }
  }
}
