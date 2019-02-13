package cn.cricin.filepicker.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import cn.cricin.filepicker.FilePicker;
import cn.cricin.filepicker.FilterBuilder;
import cn.cricin.filepicker.PreviewMode;
import cn.cricin.filepicker.Sorts;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    List<File> files = FilePicker.obtainResult(data);
    if (!files.isEmpty()) {
      LinearLayout ll = findViewById(R.id.ll);
      ll.removeAllViews();
      for (File file : files) {
        TextView text = new TextView(MainActivity.this);
        text.setText(file.getAbsolutePath());
        ll.addView(text);
      }
    }
  }

  public void noRestriction(View view) {
    FilePicker.with(this)
      .maxCount(Integer.MAX_VALUE)
      .start();
  }

  public void chooseDir(View view) {
    FilePicker.with(this)
      .filter(FilterBuilder.include().directory().build())
      .previewMode(PreviewMode.GRID)
      .start();
  }

  public void chooseFile(View view) {
    FilePicker.with(this)
      .filter(FilterBuilder.include().directory().build())
      .start();
  }

  public void darkTheme(View view) {
    FilePicker.with(this)
      .theme(R.style.FilePicker_Dark)
      .start();
  }

  public void sortByName(View view) {
    FilePicker.with(this)
      .sortWith(Sorts.byNameAsc())
      .start();
  }

  public void sortByLength(View view) {
    FilePicker.with(this)
      .sortWith(Sorts.byLengthAsc())
      .start();
  }

  public void sortByModifyTime(View view) {
    FilePicker.with(this)
      .sortWith(Sorts.byModifyTimeAsc())
      .start();
  }


  public void chooseJpg(View view) {
    FilePicker.with(this)
      .filter(FilterBuilder.include().suffixBy("jpg").build())
      .start();
  }

}
