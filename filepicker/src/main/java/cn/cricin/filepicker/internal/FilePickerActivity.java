package cn.cricin.filepicker.internal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.cricin.filepicker.FilePicker;
import cn.cricin.filepicker.Options;
import cn.cricin.filepicker.PreviewMode;
import cn.cricin.filepicker.R;
import cn.cricin.filepicker.Sorts;

/**
 * Author      :     Cricin
 * Date        :     19/1/13
 */
public class FilePickerActivity extends AppCompatActivity {
  static final String[] STORAGE_PERM = {Manifest.permission.READ_EXTERNAL_STORAGE};
  static final int STORAGE_CODE = 111;

  Options mOptions;

  Toolbar mToolbar;
  RecyclerView mRecyclerView;
  LinearLayout mNavigator;
  FileAdapter mAdapter;

  View mStorageInfoView;
  TextView mStorageInfoText;

  DirectoryStack mStack = new DirectoryStack();
  List<FileInfoImpl> mSelectedFile = new ArrayList<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    mOptions = Options.get(getIntent());
    setTitle(mOptions.mTitle);

    setTheme(mOptions.mTheme);
    Util.checkFilePickerTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.file_picker_activity);

    findViews();

    initViews();

    int permResult = ActivityCompat.checkSelfPermission(this, STORAGE_PERM[0]);
    if (permResult == PackageManager.PERMISSION_GRANTED) {
      loadSdCard();
    } else {
      ActivityCompat.requestPermissions(this, STORAGE_PERM, STORAGE_CODE);
    }
  }

  void findViews() {
    mToolbar = findViewById(R.id.file_picker_toolbar);
    mNavigator = findViewById(R.id.file_picker_navigator);
    mRecyclerView = findViewById(R.id.file_picker_recycler_view);
    mStorageInfoView = findViewById(R.id.file_picker_storage_info_view);
    mStorageInfoText = findViewById(R.id.file_picker_storage_info_text);
  }

  void initViews() {
    setSupportActionBar(mToolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    RecyclerView.LayoutManager lmToUse = mOptions.mPreviewMode == PreviewMode.LIST
      ? new LinearLayoutManager(this) : new GridLayoutManager(this, 4);
    mRecyclerView.setLayoutManager(lmToUse);
    mRecyclerView.setAdapter(mAdapter = new FileAdapter(mOptions));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == STORAGE_CODE) {
      if (Arrays.equals(STORAGE_PERM, permissions)
        && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
        loadSdCard();
      } else {
        Toast.makeText(this, "文件选择需要SD卡读取权限", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add("确定").setEnabled(false).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.getItem(0);
    item.setEnabled(!mSelectedFile.isEmpty());
    item.setTitle("确定(" + mSelectedFile.size() + ")");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    String title = item.getTitle().toString();
    if (title.startsWith("确定")) {
      Intent data = new Intent();
      String[] arr = new String[mSelectedFile.size()];
      for (int i = 0; i < mSelectedFile.size(); i++) {
        FileInfoImpl info = mSelectedFile.get(i);
        arr[i] = info.file().getAbsolutePath();
      }
      data.putExtra(FilePicker.RESULT_KEY, arr);
      setResult(RESULT_OK, data);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (mStack.canBack()) {
      exitDir();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    ImageLoader.getInstance().clearCache();
    TextLoader.getInstance().clearCache();
  }

  void addFile(FileInfoImpl info) {
    mSelectedFile.add(info);
    invalidateOptionsMenu();
  }

  void removeFile(FileInfoImpl info) {
    mSelectedFile.remove(info);
    invalidateOptionsMenu();
  }

  int selectedFileCount() {
    return mSelectedFile.size();
  }

  @SuppressLint("SetTextI18n")
  void loadSdCard() {
    File sdCard = Environment.getExternalStorageDirectory();
    long total = sdCard.getTotalSpace();
    long free = sdCard.getFreeSpace();
    int contentWidth = getResources().getDisplayMetrics().widthPixels;
    final float percentUsed = (total - free) / (float) total;

    mStorageInfoView.getLayoutParams().width = (int) (contentWidth * percentUsed);
    mStorageInfoText.setText(Util.sizeToText(total - free) + "/" + Util.sizeToText(total));

    mStack.push(DirectoryInfo.create(sdCard, "SD卡"));
    loadFileFromDir(sdCard);
  }

  void loadFileFromDir(@NonNull File dir) {
    File[] subFiles = dir.listFiles();
    if (subFiles == null) {
      mAdapter.onFilesChanged(Collections.<FileInfoImpl>emptyList());
    } else {
      List<File> dirs = Util.selectDirs(subFiles);
      List<File> files = Util.selectFiles(subFiles);
      if (mOptions.mSort != Sorts.NO_SORT) {
        Collections.sort(dirs, mOptions.mSort);
        Collections.sort(files, mOptions.mSort);
      }
      dirs.addAll(files);
      List<FileInfoImpl> result = new ArrayList<>(dirs.size());
      for (File file : dirs) {
        FileInfoImpl info = FileInfoImpl.valueOf(file);
        info.setEnabled(mOptions.mFileFilter.accept(file));
        if (mSelectedFile.contains(info)) {
          info.setSelected(true);
        }
        result.add(info);
      }
      mAdapter.onFilesChanged(result);
    }
  }

  void enterIntoDir(String dirName) {
    DirectoryInfo current = mStack.peek();
    current.mItemPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
    File file = current.mDir;
    File subDir = new File(file, dirName);

    mStack.push(DirectoryInfo.create(subDir, dirName));
    View view = getLayoutInflater().inflate(R.layout.file_picker_navigator_item, mNavigator, false);
    TextView textView = view.findViewById(R.id.file_picker_navigator_text);
    textView.setText(dirName);
    mNavigator.addView(view);
    loadFileFromDir(subDir);
  }

  void exitDir() {
    mStack.pop();
    mNavigator.removeViewAt(mNavigator.getChildCount() - 1);
    DirectoryInfo back = mStack.peek();
    loadFileFromDir(back.mDir);
    if (back.mItemPosition != -1) {
      mRecyclerView.scrollToPosition(back.mItemPosition);
    }
  }

  static class DirectoryStack {
    List<DirectoryInfo> mStack = new ArrayList<>();

    void push(DirectoryInfo info) {
      mStack.add(info);
    }

    void pop() {
      mStack.remove(mStack.size() - 1);
    }

    DirectoryInfo peek() {
      if (mStack.isEmpty()) return null;
      return mStack.get(mStack.size() - 1);
    }

    boolean canBack() {
      return mStack.size() > 1;
    }
  }

  static final class DirectoryInfo {
    File mDir;
    String mDirName;
    int mItemPosition = -1;

    static DirectoryInfo create(File dir, String dirName) {
      DirectoryInfo info = new DirectoryInfo();
      info.mDir = dir;
      info.mDirName = dirName;
      return info;
    }
  }

}
