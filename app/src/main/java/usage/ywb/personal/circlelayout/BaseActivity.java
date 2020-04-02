package usage.ywb.personal.circlelayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import usage.ywb.personal.circlelayout.utils.PermissionUtils;


/**
 * 基类：所有有title的Activity都继承该类
 *
 * @author yuwenbo
 * @version [V1.0.0, 2016/3/15]
 */
public class BaseActivity extends AppCompatActivity implements PermissionUtils.PermissionCallbacks {


    private View titleView;
    /**
     * 标题左边的按钮
     */
    private TextView leftTv;

    /**
     * 标题中间的文字
     */
    private TextView centerTv;

    /**
     * 标题右边的按钮
     */
    public TextView rightTv;

    /**
     * 内容区域
     */
    private FrameLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        titleView = findViewById(R.id.title_layout);
        leftTv = findViewById(R.id.title_left_iv);
        centerTv = findViewById(R.id.title_name_tv);
        rightTv = findViewById(R.id.title_right_tv);
        contentLayout = findViewById(R.id.content_layout);
        leftTv.setOnClickListener(this::leftClick);
        rightTv.setOnClickListener(this::rightClick);
    }

    /**
     * 设置title左边的图标的显示状态
     *
     * @param visibility 状态
     */
    protected void setLeftVisibility(int visibility) {
        leftTv.setVisibility(visibility);
    }

    /**
     * 设置title右边的图标的显示状态
     *
     * @param visibility 状态
     */
    protected void setRightVisibility(int visibility) {
        rightTv.setVisibility(visibility);
    }

    /**
     * 设置左边的图标
     *
     * @param imageResID 图标资源id
     */
    protected void setLeftImage(@DrawableRes int imageResID) {
        leftTv.setCompoundDrawablesWithIntrinsicBounds(imageResID, 0, 0, 0);
        leftTv.setVisibility(View.VISIBLE);
    }

    /**
     * 设置右边的图标
     *
     * @param imageResID 图标资源id
     */
    protected void setRightImage(int imageResID) {
        rightTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageResID, 0);
        rightTv.setVisibility(View.VISIBLE);
    }

    /**
     * 设置title中间的文字
     *
     * @param strResID 文字资源id
     */
    protected void setTitleText(@StringRes int strResID) {
        centerTv.setText(strResID);
    }

    /**
     * 设置title中间的文字
     *
     * @param text 文字字符串
     */
    protected void setTitleText(String text) {
        centerTv.setText(text);
    }

    /**
     * 左边按钮的点击事件
     *
     * @param view 左边的按钮
     */
    protected void leftClick(View view) {
        finish();
    }

    /**
     * 右边按钮的点击事件
     *
     * @param view 右边的按钮
     */
    protected void rightClick(View view) {

    }

    protected void setTitleBackgroundColor(int color) {
        titleView.setBackgroundColor(color);
    }

    protected void setBackgroundColor(int color) {
        if (contentLayout.getChildCount() == 0) {
            contentLayout.setBackgroundColor(color);
        } else {
            getContentView().setBackgroundColor(color);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        contentLayout.removeAllViews();
        contentLayout.addView(LayoutInflater.from(this).inflate(layoutResID, contentLayout, false));
    }

    @Override
    public void setContentView(View view) {
        contentLayout.removeAllViews();
        contentLayout.addView(view);
    }

    public View getContentView() {
        if (contentLayout.getChildCount() == 1) {
            return contentLayout.getChildAt(0);
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, String[] perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, String[] perms) {

    }

}
