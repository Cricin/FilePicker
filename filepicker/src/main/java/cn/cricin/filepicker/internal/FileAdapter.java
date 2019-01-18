package cn.cricin.filepicker.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.cricin.filepicker.Options;
import cn.cricin.filepicker.Preview;
import cn.cricin.filepicker.PreviewLoader;
import cn.cricin.filepicker.PreviewMode;
import cn.cricin.filepicker.R;

/**
 * Author      :     Cricin
 * Date        :     19/1/14
 */
/*package*/final class FileAdapter extends RecyclerView.Adapter<FileAdapter.ItemHolder> {
  private List<FileInfoImpl> mFiles;
  private Options mOptions;

  FileAdapter(Options options) {
    this.mOptions = options;
  }

  @NonNull
  @Override
  public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater factory = LayoutInflater.from(parent.getContext());
    int layoutId = (mOptions.mPreviewMode == PreviewMode.LIST)
      ? R.layout.file_picker_list_item : R.layout.file_picker_grid_item;
    View root = factory.inflate(layoutId, parent, false);
    return new ItemHolder(root);
  }

  @Override
  public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
    holder.onBind(mFiles.get(position), mOptions);
  }

  @Override
  public int getItemCount() {
    if (mFiles == null) return 0;
    return mFiles.size();
  }

  void onFilesChanged(List<FileInfoImpl> files) {
    this.mFiles = files;
    notifyDataSetChanged();
  }

  static class ItemHolder extends RecyclerView.ViewHolder {
    ImageView mPreview;
    TextView mTitle;
    TextView mContent;

    TextView mFileName;
    TextView mModifyTime;
    TextView mSize;
    View mSelectView;

    ItemHolder(View itemView) {
      super(itemView);
      mPreview = itemView.findViewById(R.id.file_picker_item_preview);
      mTitle = itemView.findViewById(R.id.file_picker_item_title);
      mContent = itemView.findViewById(R.id.file_picker_item_text_content);

      mFileName = itemView.findViewById(R.id.file_picker_item_name);
      mModifyTime = itemView.findViewById(R.id.file_picker_item_modify_time);
      mSize = itemView.findViewById(R.id.file_picker_item_size);
      mSelectView = itemView.findViewById(R.id.file_picker_item_select);
    }

    void onBind(final FileInfoImpl file, final Options options) {
      final Context ctx = itemView.getContext();

      mContent.setVisibility(View.INVISIBLE);
      mTitle.setVisibility(file.isDir() ? View.INVISIBLE : View.VISIBLE);
      mPreview.setVisibility(View.VISIBLE);
      mPreview.setTag(null);//防止错位

      if (file.isDir()) {
        if (options.mDirDrawableId == R.drawable.file_picker_icon_folder) {
          Drawable d = ctx.getResources().getDrawable(options.mDirDrawableId);
          DrawableCompat.setTint(d, Util.getColor(ctx, R.attr.file_picker_folder_tint));
          mPreview.setImageDrawable(d);
        } else {
          mPreview.setImageResource(options.mDirDrawableId);
        }
      } else {
        mTitle.setText(file.extension().toUpperCase());
        if (options.mFileDrawableId == R.drawable.file_picker_icon_file) {
          Drawable d = ctx.getResources().getDrawable(options.mFileDrawableId);
          int color = FileTypeTint.getColorForExt(file.extension());
          DrawableCompat.setTint(d, color);
          mPreview.setImageDrawable(d);
        } else {
          mPreview.setImageResource(options.mFileDrawableId);
        }
      }

      @PreviewLoader.PreviewType int type = options.mPreviewLoader.getLoadType(file);
      Preview icon = Preview.obtain(mPreview, mContent);
      if (type == PreviewLoader.TYPE_IMAGE || type == PreviewLoader.TYPE_VIDEO) {
        mContent.setVisibility(View.INVISIBLE);
        mTitle.setVisibility(View.INVISIBLE);
        mPreview.setVisibility(View.VISIBLE);
        options.mPreviewLoader.loadPreview(ctx, file, icon);
      } else if (type == PreviewLoader.TYPE_TEXT) {
        mContent.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.INVISIBLE);
        mPreview.setVisibility(View.INVISIBLE);
        options.mPreviewLoader.loadPreview(ctx, file, icon);
      }
      icon.release();

      mFileName.setText(file.fileName());
      if (mSize != null && mModifyTime != null) {
        mSize.setText(file.length());
        mModifyTime.setText(file.modifyTime());
      }
      mSelectView.setVisibility(file.isEnabled() ? View.VISIBLE : View.INVISIBLE);
      mSelectView.setSelected(file.isSelected());

      mSelectView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onClickFileOrSelect(file, options.mMaxCount);
        }
      });

      if (file.isDir()) {
        itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            getActivity().enterIntoDir(file.fileName());
          }
        });
      } else {
        itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onClickFileOrSelect(file, options.mMaxCount);
          }
        });
      }
    }

    void onClickFileOrSelect(FileInfoImpl file, int maxCount){
      if (!file.isEnabled()) return;
      boolean selected = file.isSelected();
      FilePickerActivity activity = getActivity();
      int count = activity.selectedFileCount();
      if (!selected) {
        if (count < maxCount) {
          file.setSelected(true);
          activity.addFile(file);
          mSelectView.setSelected(true);
        } else {
          mSelectView.setSelected(false);
          Toast.makeText(mSelectView.getContext(),
            "最多选择" + maxCount + "个文件", Toast.LENGTH_SHORT).show();
        }
      } else {
        file.setSelected(false);
        activity.removeFile(file);
        mSelectView.setSelected(false);
      }
    }

    FilePickerActivity getActivity() {
      return ((FilePickerActivity) itemView.getContext());
    }
  }

}
