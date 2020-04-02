package usage.ywb.personal.circlelayout.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;


import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Set;

import usage.ywb.personal.circlelayout.BaseActivity;
import usage.ywb.personal.circlelayout.R;
import usage.ywb.personal.circlelayout.colorpicker.ColorPickerHelper;
import usage.ywb.personal.circlelayout.utils.CaptureUtil;
import usage.ywb.personal.circlelayout.utils.Constants;
import usage.ywb.personal.circlelayout.utils.PermissionUtils;
import usage.ywb.personal.circlelayout.utils.StatusBarUtil;
import usage.ywb.personal.circlelayout.view.CircleImageView;
import usage.ywb.personal.circlelayout.view.CircleLayout;

/**
 * @author Kingdee.ywb
 * @version [ V.2.2.6  2018/9/11 ]
 */
public class MainActivity extends BaseActivity implements CircleLayout.OnItemClickListener, CircleLayout.OnItemLongClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String[] PERMISSION_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int REQUEST_CODE_ALBUM = 102;
    private static final int REQUEST_CODE_CROP = 103;

    private static final int REQUEST_PERMISSION = 1;//更换图像

    private CircleLayout circleLayout;
    private RecyclerView recyclerView;
    private PopupWindow popupWindow;
    private MenuAdapter adapter;

    private ImageView imageView;
    private String tempPath;
    private String[] menuArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitleText("菜单");
        circleLayout = findViewById(R.id.CircleLayout);

        circleLayout.setOnItemClickListener(this);
        circleLayout.setOnItemLongClickListener(this);
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        int mValue = preferences.getInt(Constants.KEY_THEME_COLOR, ContextCompat.getColor(this, R.color.main_color));
        setThemeColor(mValue);
        for (int i = 0; i < circleLayout.getChildCount(); i++) {
            String path = preferences.getString(String.valueOf(i), null);
            if (path != null && circleLayout.getChildAt(i) instanceof ImageView) {
                imageView = (ImageView) circleLayout.getChildAt(i);
                imageView.setImageBitmap(BitmapFactory.decodeFile(path));
                imageView.setTag(i);
            }
        }
        Set<String> strings = preferences.getStringSet(Constants.KEY_MENU, null);
        if (strings != null) {
            menuArray = strings.toArray(menuArray);
        }
        popupWindow = new PopupWindow(this);
        View root = LayoutInflater.from(this).inflate(R.layout.popup_menu, null);
        recyclerView = root.findViewById(R.id.menu_rv);
        popupWindow.setContentView(root);
        popupWindow.setOutsideTouchable(true);
    }

    @Override
    protected void rightClick(View view) {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }

        menuArray = new String[]{
                "换背景", "1", "2"
        };
        if (adapter == null) {
            adapter = new MenuAdapter(this, menuArray) {
                @Override
                protected void onItemClickListener(View view, int position) {
                    if (position == 0) {
                        pickerColor();
                    } else {
                        Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
                    }
                    popupWindow.dismiss();
                }
            };
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        popupWindow.showAsDropDown(view);
    }

    private void pickerColor() {
        ColorPickerHelper pickerHelper = new ColorPickerHelper(this) {
            @Override
            public void onColorChanged(int color) {
                super.onColorChanged(color);
                setThemeColor(color);
            }
        };
        pickerHelper.setAlphaSliderEnabled(true);
        pickerHelper.setHexValueEnabled(true);
        pickerHelper.showDialog();
    }

    private void setThemeColor(int color) {
        StatusBarUtil.setColor(MainActivity.this, color, 0);
        setTitleBackgroundColor(color);
        setBackgroundColor(color);
    }

    @Override
    public void onItemClick(CircleLayout circleLayout, View child, int position, long id) {
        if (position == -1) {
            Toast.makeText(this, "·O(∩_∩)O·", Toast.LENGTH_SHORT).show();
        } else {
            if (child instanceof CircleImageView) {
                imageView = (CircleImageView) child;
                if (imageView.getTag() == null) {
                    PermissionUtils.requestPermissions(this, REQUEST_PERMISSION, PERMISSION_STORAGE);
                } else {
                    Toast.makeText(this, "就选你了·O(∩_∩)O·", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onItemLongClick(CircleLayout circleLayout, View child, int position, long id) {
        if (child instanceof CircleImageView) {
            imageView = (CircleImageView) child;
            if (imageView.getTag() != null) {
                PermissionUtils.requestPermissions(this, REQUEST_PERMISSION, PERMISSION_STORAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_ALBUM:
                    File directory = new File(CaptureUtil.CAMERA_PATH);
                    if (!directory.exists() && !directory.mkdirs()) {
                        return;
                    }
                    File outputFile = new File(directory, circleLayout.indexOfChild(imageView) + ".jpg");
                    tempPath = outputFile.getPath();
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    Uri outputUri = Uri.fromFile(outputFile);
                    intent.setDataAndType(data.getData(), "image/*");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    // crop为true是设置在开启的intent中设置显示的view可以剪裁
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    // 剪裁图片的宽高
                    intent.putExtra("outputX", 200);
                    intent.putExtra("outputY", 200);
                    intent.putExtra("return-data", false);
                    intent.putExtra("noFaceDetection", true);//去除默认的人脸识别，否则和剪裁匡重叠
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    startActivityForResult(intent, REQUEST_CODE_CROP);
                    break;
                case REQUEST_CODE_CROP://图片裁剪后的回调
                    if (tempPath != null) {
                        imageView.setImageBitmap(BitmapFactory.decodeFile(tempPath));
                        imageView.setTag(circleLayout.indexOfChild(imageView));
                        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
                        preferences.edit().putString(imageView.getTag().toString(), tempPath).apply();
                    }
                    break;
            }
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, String[] perms) {
        if (requestCode == REQUEST_PERMISSION) {
            CaptureUtil.selectPictureFromAlbum(this, REQUEST_CODE_ALBUM);
        }
    }


}
