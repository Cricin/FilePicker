# FilePicker for Android

For a working implementation, please have a look at the Sample Project - app

<img src="https://raw.githubusercontent.com/Cricin/FilePicker/master/pics/pic1.png" width="30%"></img>
<img src="https://raw.githubusercontent.com/Cricin/FilePicker/master/pics/pic2.png" width="30%"></img>
<img src="https://raw.githubusercontent.com/Cricin/FilePicker/master/pics/pic3.png" width="30%"></img>

Usage:

1 gradle dependency, add `compile 'cn.cricin:filepicker:0.0.1` to `build.gradle`

2 The FilePicker configuration is created using the builder pattern.
```
FilePicker.with(this) //context, activity or fragment
  .maxCount(3) //maximum count of files to be selected(optional)
  .theme(R.style.FilePicker)//activity theme(optional)
  .sortWith(Sorts.byNameAsc)//how the files sorted(optional)
  .filter(FilterBuilder.include().sizeLessThan(1024 * 1024).andInclude().suffixBy("jpg").build)//exclude or include files(optional)
  .title("Choose Files")//activity title(optional)
  .previewMode(PreviewMode.LIST)//LIST or GRID(optional)
  .iconLoader(new DefaultIconLoader())//load the file preview(optional)
  .start()
```   

3 Override `onActivityResult` method and handle file pick result.
```
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (resultCode == RESULT_OK) {
       List<File> files = FilePicker.obtainResult(data)
       //do your logic here
     }
   }
```
## Custom
FilePicker provided two built-in theme `FilePicker` and `FilePicker_Dark`, you can customize
by declare you theme extends FilePicker, and pass it to `FilePicker.theme()`.
available attr are:
```
  <item name="file_picker_main_color">#008577</item>
  <item name="file_picker_select_border_color">#AAAAAA</item>
  <item name="file_picker_select_solid_color">?file_picker_main_color</item>
  <item name="file_picker_usage_color">?file_picker_main_color</item>
  <item name="file_picker_folder_tint">#68C9F9</item>
  <item name="file_picker_common_text_color">@android:color/black</item>
  <item name="file_picker_frame_color">@android:color/white</item>
```
## Download
[Demo Download here](https://raw.githubusercontent.com/Cricin/FilePicker/master/demo.apk)

## License

Copyright 2019 Cricin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.