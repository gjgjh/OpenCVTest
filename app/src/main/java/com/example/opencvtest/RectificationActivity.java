package com.example.opencvtest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class RectificationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private static final int height=3000;           // 文档纠正后的高（自定义的尺寸）
    private static final int width=2000;            // 文档纠正后的宽

    private DrawImageView rawImageView;             // 原图控件
    private ImageView rectifiedImageView;           // 纠正后图控件
    private Bitmap bitmap=null;                     // 原图
    private Bitmap rectified=null;                  // 纠正后图

    private Uri imageUri;                           // 照片路径，Uri格式
    private String imagePath;                       // 照片路径，全局路径格式

    BaseLoaderCallback baseLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS: {
                    Log.d("gjgjh", "OpenCV loaded successfully");
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rectification);

        initWidgets();
    }

    private void initWidgets() {
        Button buttonRectify=findViewById(R.id.rectify);
        buttonRectify.setOnClickListener(this);
        Button buttonPhoto=findViewById(R.id.select_photo);
        buttonPhoto.setOnClickListener(this);
        Button buttonClear=findViewById(R.id.clear);
        buttonClear.setOnClickListener(this);
        Button buttonSave=findViewById(R.id.save);
        buttonSave.setOnClickListener(this);

        rawImageView =findViewById(R.id.raw_image);
        rectifiedImageView =findViewById(R.id.rectified);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select_photo:
                clear();
                setPhoto();
                break;
            case R.id.clear:
                clear();
                Toast.makeText(this,"ROI is cleared now",Toast.LENGTH_SHORT).show();
                break;
            case R.id.rectify:
                rectify();
                break;
            case R.id.save:
                if (rectified==null){
                    Toast.makeText(this,"Please rectify first",Toast.LENGTH_SHORT).show();
                    return;
                }

                String path=SaveImage(rectified);
                Toast.makeText(this,"Result saved: "+path,Toast.LENGTH_LONG).show();
                break;
        }
    }

    // 拍照或选择照片
    protected void setPhoto(){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view= LayoutInflater.from(this).inflate(R.layout.dialog_choose_photo,null);

        Window window=dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.DialogAnimation);
        dialog.setView(view);
        dialog.show();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 设置弹窗点击事件
        TextView take_photo=(TextView)view.findViewById(R.id.take_photo);
        TextView choose_from_album=(TextView) view.findViewById(R.id.choose_from_album);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                dialog.cancel();
            }
        });
        choose_from_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum();
                dialog.cancel();
            }
        });
    }

    // 打开摄像头拍照
    protected void takePhoto(){
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions,TAKE_PHOTO);
        } else {
            File outputImage = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
            try {
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT < 24) {
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            } else {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        }
    }

    // 从相册选择照片
    protected void openAlbum(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, CHOOSE_PHOTO);
        }else {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_PHOTO);
        }
    }

    // 4.4及以上系统使用这个方法处理图片。并将图片路径保存到数据库
    @TargetApi(19)
    protected void handleImageOnKitKat(Intent data) {
        imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
    }

    // 4.4以下系统使用这个方法处理图片。并将图片路径保存到数据库
    protected void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        imagePath = getImagePath(uri, null);
    }

    // 辅助函数
    protected String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    // 动态处理权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (grantResults.length > 0) {
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    takePhoto();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case CHOOSE_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
            default:
        }
    }

    // 回调函数。处理拍照，照片选择的结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        rawImageView.setImageBitmap(bitmap);
                        rectifiedImageView.setImageBitmap(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);
                        bitmap = BitmapFactory.decodeFile(imagePath);
                    } else {
                        handleImageBeforeKitKat(data);
                        bitmap = BitmapFactory.decodeFile(imagePath);
                    }
                    rawImageView.setImageBitmap(bitmap);
                    rectifiedImageView.setImageBitmap(null);
                }
                break;
            default:
                break;
        }
    }

    // 保存图片函数，以当前时间命名图片，并返回存储路径
    public String SaveImage(Bitmap finalBitmap) {
        String root = getExternalStorageDirectory().toString();
        File myDir = new File(root + "/OpenCVTest");
        if (!myDir.exists()) {
            myDir.mkdir();
        }

        String currTime= new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
        String fname = "/Image-"+ currTime +".jpg";
        String filePath=myDir+fname;
        File file = new File (filePath);
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }

    // 图像纠正
    private void rectify(){
        if(rawImageView.verticesX.size()!=4){
            Toast.makeText(this,"Please select your ROI first",Toast.LENGTH_SHORT).show();
            return;
        }

        if(bitmap==null){
            Toast.makeText(this,"Please select photo first",Toast.LENGTH_SHORT).show();
            return;
        }

        // 1--获取多边形顶点在图像坐标系下的坐标（需要转换一下）
        List<Point> ptsSrc=new ArrayList<>();
        Matrix invertMatrix = new Matrix();
        ((ImageView) rawImageView).getImageMatrix().invert(invertMatrix);
        for(int i = 0; i< rawImageView.verticesX.size(); ++i){
            float[] pixXY=new float[]{rawImageView.verticesX.get(i), rawImageView.verticesY.get(i)};
            invertMatrix.mapPoints(pixXY);
            ptsSrc.add(new Point(pixXY[0],pixXY[1]));
        }

        MatOfPoint2f src=new MatOfPoint2f();
        src.fromList(ptsSrc);

        // 2--设置纠正后图像坐标（与多边形顶点顺序要一一对应）
        List<Point> ptsDst=new ArrayList<>();
        ptsDst.add(new Point(0,0));
        ptsDst.add(new Point(width,0));
        ptsDst.add(new Point(width,height));
        ptsDst.add(new Point(0,height));

        MatOfPoint2f dst=new MatOfPoint2f();
        dst.fromList(ptsDst);

        // 3--格式转换，bitmap到Mat
        Mat img=new Mat();
        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp, img);
        Mat res=new Mat();

        // 4--计算单应矩阵，并进行warp变换
        Mat h=Calib3d.findHomography(src,dst);
        Imgproc.warpPerspective(img,res,h,img.size());

        // 5--裁剪得到结果
        Rect roi = new Rect(0, 0 , Math.min(width,res.cols()), Math.min(height,res.rows()));
        Mat croped= res.submat(roi);
        Imgproc.resize(croped,croped,new Size(width,height));
        rectified=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(croped,rectified);

        rectifiedImageView.setImageBitmap(rectified);
    }

    // 清空ROI
    private void clear(){
        rawImageView.clearCanvas();
        rawImageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d("gjgjh", "OpenCV successful loaded ");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else {
            Log.d("gjgjh", "OpenCV not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,baseLoaderCallback);
        }
    }
}
