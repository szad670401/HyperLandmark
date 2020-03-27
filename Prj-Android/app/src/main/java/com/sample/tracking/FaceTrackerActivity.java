package com.sample.tracking;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.hyq.hm.hyperlandmark.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class FaceTrackerActivity extends Activity {
    private final static int CAMERA_REQUEST_CODE = 0x111;

    public void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                // directory
                File file = new File(newPath);
                if (!file.mkdir())
                {
                    Log.d("mkdir","can't make folder");

                }

                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName,
                            newPath + "/" + fileName);
                }
            } else {
                // file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void InitModelFiles()
    {

        String assetPath = "ZeuseesFaceTracking";
        String sdcardPath = Environment.getExternalStorageDirectory()
                + File.separator + assetPath;
        copyFilesFromAssets(this, assetPath, sdcardPath);

    }
    private String[] denied;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multitracker);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionChecker.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    list.add(permissions[i]);
                }
            }
            if (list.size() != 0) {
                denied = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    denied[i] = list.get(i);
                }
                ActivityCompat.requestPermissions(this, denied, 5);
            } else {
                init();
            }
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 5) {
            boolean isDenied = false;
            for (int i = 0; i < denied.length; i++) {
                String permission = denied[i];
                for (int j = 0; j < permissions.length; j++) {
                    if (permissions[j].equals(permission)) {
                        if (grantResults[j] != PackageManager.PERMISSION_GRANTED) {
                            isDenied = true;
                            break;
                        }
                    }
                }
            }
            if (isDenied) {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            } else {
                init();

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void init(){
        InitModelFiles();
    }

    @Override
    public void onResume() {
        super.onResume();

        final FaceOverlapFragment fragment = (FaceOverlapFragment) getFragmentManager()
                .findFragmentById(R.id.overlapFragment);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
