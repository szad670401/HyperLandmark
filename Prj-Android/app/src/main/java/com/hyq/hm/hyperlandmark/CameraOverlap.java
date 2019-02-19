package com.hyq.hm.hyperlandmark;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.List;

/**
 * Created by 海米 on 2018/11/29.
 */

public class CameraOverlap {

    protected Camera mCamera = null;
    protected Camera.CameraInfo mCameraInfo = null;
    public static final int PREVIEW_WIDTH = 640;
    public static final int PREVIEW_HEIGHT = 480;
    private int CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Camera.PreviewCallback mPreviewCallback;

    private Context context;
    public CameraOverlap(Context context){
        this.context = context;
    }


    public void openCamera(SurfaceTexture surfaceTexture) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraFacing) {
                try {
                    mCamera = Camera.open(i);
                    mCameraInfo = info;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    mCamera = null;
                    continue;
                }
                break;
            }
        }
        try {
            mCamera.setPreviewTexture(surfaceTexture);
            initCamera();
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }
    }


    private void initCamera() {
        if (null != mCamera) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                List<String> flashModes = parameters.getSupportedFlashModes();
                if(flashModes !=null && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF))
                {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }


                List<Camera.Size> pictureSizes = mCamera.getParameters()
                        .getSupportedPictureSizes();

                parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);

                Camera.Size fs = null;
                for (int i = 0; i < pictureSizes.size(); i++) {
                    Camera.Size psize = pictureSizes.get(i);
                    if (fs == null && psize.width >= 1280)
                        fs = psize;

                }
                parameters.setPictureSize(fs.width, fs.height);

                if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", 90);

                    int orientation = CameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ? 360 - mCameraInfo.orientation : mCameraInfo.orientation;
                    mCamera.setDisplayOrientation(orientation);

                } else {
                    parameters.set("orientation", "landscape");
                    mCamera.setDisplayOrientation(0);

                }

                if(CameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK){
                    if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else{
                        parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
                    }
                }

                mCamera.setParameters(parameters);
                mCamera.setPreviewCallback(this.mPreviewCallback);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.mPreviewCallback = previewCallback;
        if (mCamera != null) {
            mCamera.setPreviewCallback(previewCallback);
        }
    }
    public int getOrientation(){
        if(mCameraInfo != null){
            return mCameraInfo.orientation;
        }
        return 0;
    }

    public void release() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}
