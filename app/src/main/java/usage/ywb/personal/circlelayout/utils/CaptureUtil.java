package usage.ywb.personal.circlelayout.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * 图片获取与裁剪
 *
 * @author Kingdee.ywb
 * @version [ V.2.1.8  2018/6/22 ]
 */
public class CaptureUtil {

    /**
     * 系统相册的路径
     */
    public static final String CAMERA_PATH = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Allodoxaphobia" + File.separator;

    private static final String FILE_PROVIDER = "usage.ywb.personal.allodoxaphobia.provider";


    /**
     * 拍照
     *
     * @param requestCode 请求码
     * @param file        拍照后图片文件位置
     */
    public static void takePicture(@NonNull Activity activity, int requestCode, File file) {
        activity.startActivityForResult(getCaptureIntent(activity, file), requestCode);
    }


    /**
     * 根据SDK版本适配，获取拍照的Intent
     *
     * @param context 上下文
     * @param file    拍照之后的文件
     */
    private static Intent getCaptureIntent(@NonNull Context context, File file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Android7.0以上URI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //通过FileProvider创建一个content类型的Uri
            Uri uri = FileProvider.getUriForFile(context, FILE_PROVIDER, file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));//指定图片存放位置，指定后，在onActivityResult里得到的Data将为null
        }
        return intent;
    }

    /**
     * 裁剪图片
     *
     * @param srcFile     目标文件
     * @param outputX     输出宽度
     * @param outputY     输出高度
     * @param aspectX     裁剪框比例
     * @param aspectY     裁剪框比例
     * @param requestCode 请求码
     */
    public static File cropPicture(Activity activity, int requestCode, File srcFile, int aspectX, int aspectY, int outputX, int outputY) {
        if (srcFile == null || !srcFile.exists()) {
            return null;
        }
        File outputFile = new File(CAMERA_PATH, "crop.jpg");
        activity.startActivityForResult(getCropIntent(activity, srcFile, outputFile, aspectX, aspectY, outputX, outputY), requestCode);
        return outputFile;
    }

    //裁剪方形图图片
    public static File cropSquarePicture(Activity activity, File srcFile, int outputX, int outputY, int requestCode) {
        if (srcFile == null) {
            srcFile = new File(CAMERA_PATH, "picture.jpg");
        }
        return cropPicture(activity, requestCode, srcFile, 1, 1, outputX, outputY);
    }

    /**
     * 获取裁剪Intent
     *
     * @param context    上下文
     * @param srcFile    目标文件 原图片
     * @param outputFile 输出文件 裁剪图
     * @param aspectX    裁剪框比例
     * @param aspectY    裁剪框比例
     * @param outputX    输出宽度
     * @param outputY    输出高度
     * @return intent
     */
    public static Intent getCropIntent(@NonNull Context context, File srcFile, File outputFile, int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        Uri outPutUri = Uri.fromFile(outputFile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(FileProvider.getUriForFile(context, FILE_PROVIDER, srcFile), "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(srcFile), "image/*");
        }
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // 剪裁图片的宽高
        if (outputX > 0) {
            intent.putExtra("outputX", outputX);
        }
        if (outputY > 0) {
            intent.putExtra("outputY", outputY);
        }
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);//去除默认的人脸识别，否则和剪裁匡重叠
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        return intent;
    }

    /**
     * 从相册选取图片
     *
     * @param activity    DealedCustomerSearchActivity
     * @param requestCode 请求码
     */
    public static void selectPictureFromAlbum(@NonNull Activity activity, int requestCode) {
        // 调用系统的相册
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        // 调用剪切功能
        activity.startActivityForResult(intent, requestCode);
    }

}
