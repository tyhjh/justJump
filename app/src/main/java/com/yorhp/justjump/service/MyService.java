package com.yorhp.justjump.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yorhp.justjump.R;
import com.yorhp.justjump.app.MyApplication;
import com.yorhp.justjump.opencv.ImageRecognition;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class MyService extends Service {


    int distence = 0;
    public static String screenPath = MyApplication.rootDir + "/screenshots.png";

    ImageRecognition imageRecognition;


    public static String mePath = MyApplication.rootDir + "/opencv_me/me.png";

    boolean start;

    ImageView btnView3;
    boolean isAdded;
    Thread thread;

    public static final int FLAG_LAYOUT_INSET_DECOR = 0x00000200;

    WindowManager.LayoutParams params3;

    WindowManager windowManager;

    public MyService() {


    }

    @Override
    public void onCreate() {
        super.onCreate();
        createWindowView();
        imageRecognition = new ImageRecognition();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i >= 0 && start) {
                    File file1 = new File(MyApplication.rootDir + "/screenshots.png");
                    if (file1.exists())
                        file1.delete();
                    execShellCmd("screencap -p " + MyApplication.rootDir + "/screenshots.png");
                    Bitmap bitmap = BitmapFactory.decodeFile(screenPath);
                    while (bitmap == null) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bitmap = BitmapFactory.decodeFile(screenPath);
                    }

                    distence = imageRecognition.getDistence();


                    System.out.println("距离为：" + distence);


                    File file = new File(screenPath);
                    if (file.exists()) {
                        file.delete();
                    }
                    int time=0;

                    if(distence<300){
                        time = (int) (distence * 1.41);
                        System.out.println("系数为：" + 1.41);
                    }else if(distence<400){
                        time = (int) (distence * 1.405);
                        System.out.println("系数为：" + 1.405);
                    }else if(distence<500){
                        time = (int) (distence * 1.400);
                        System.out.println("系数为：" + 1.400);
                    }else if(distence<600){
                        time = (int) (distence * 1.385);
                        System.out.println("系数为：" + 1.386);
                    }else if(distence<700){
                        time = (int) (distence * 1.375);
                        System.out.println("系数为：" + 1.375);
                    }else if(distence>700){
                        time = (int) (distence * 1.370);
                        System.out.println("系数为：" + 1.370);
                    }



                    String msg = "input touchscreen swipe 560 1600 560 1600 " + time;
                    execShellCmd(msg);
                    try {
                        Thread.sleep(3500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createWindowView() {
        btnView3 = new ImageView(getApplicationContext());
        btnView3.setImageResource(R.drawable.ic_star);

        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        params3 = new WindowManager.LayoutParams();

        // 设置Window Type
        params3.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params3.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params3.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params3.width = 200;
        params3.height = 200;
        params3.gravity = Gravity.TOP;
        params3.x = 300;
        params3.y = 200;


        btnView3.setOnTouchListener(new View.OnTouchListener() {

            //保存悬浮框最后位置的变量
            int lastX, lastY;
            int paramX, paramY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params3.x;
                        paramY = params3.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params3.x = paramX + dx;
                        params3.y = paramY + dy;
                        // 更新悬浮窗位置
                        windowManager.updateViewLayout(btnView3, params3);
                        break;
                }
                return false;
            }
        });


        btnView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!start) {
                    start = true;
                } else {
                    start = false;
                }
                //start=true;
                thread.start();
                //btnView3.setVisibility(View.INVISIBLE);
            }
        });
        windowManager.addView(btnView3, params3);
        isAdded = true;
    }

    public static void execShellCmd(String cmd) {
        try {
            // 获取输出流
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public static File bitmapToPath(Bitmap bitmap, String name) {
        String filepath = MyApplication.rootDir + "/" + name + ".png";
        File file = new File(filepath);
        //3.保存Bitmap
        try {
            //文件

            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);

    }


}
