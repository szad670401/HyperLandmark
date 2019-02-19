package com.sample.tracking;

import java.util.List;

import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.hyq.hm.hyperlandmark.R;


public class CameraOverlapFragment extends Fragment {

    protected Camera mCamera = null;
    protected CameraInfo mCameraInfo = null;
    protected int mCameraInit = 0;
    protected SurfaceView mSurfaceview = null;
    protected SurfaceView mOverlap = null;
    protected SurfaceHolder mSurfaceHolder = null;

    Camera.PreviewCallback mPreviewCallback;
    Matrix matrix = new Matrix();
    final int PREVIEW_WIDTH = 640;
    final int PREVIEW_HEIGHT = 480;

    int CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_overlap, container,
                false);


        mSurfaceview = (SurfaceView) view.findViewById(R.id.surfaceViewCamera);
        mOverlap = (SurfaceView) view.findViewById(R.id.surfaceViewOverlap);
        mOverlap.setZOrderOnTop(true);
        mOverlap.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder = mSurfaceview.getHolder();
        mSurfaceview.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            CameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
                        } else {
                            CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        }
                        openCamera(CameraFacing);
                    }
                }
        );
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {

                matrix.setScale(width / (float) PREVIEW_HEIGHT, height / (float) PREVIEW_WIDTH);
                initCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mCamera = null;
                openCamera(CameraFacing);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                if (null != mCamera) {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
                mCameraInit = 0;
            }


        });

        return view;
    }

    private void openCamera(int CameraFacing) {
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

            mCamera.setPreviewDisplay(mSurfaceHolder);
            initCamera();
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }
    }


    private void initCamera() {
        mCameraInit = 1;
        if (null != mCamera) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                List<String> flashModes = parameters.getSupportedFlashModes();
                if(flashModes !=null && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF))
                {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }


                List<Size> previewSizes = mCamera.getParameters()
                        .getSupportedPreviewSizes();
                List<Size> pictureSizes = mCamera.getParameters()
                        .getSupportedPictureSizes();

                for (int i = 0; i < previewSizes.size(); i++) {
                    Size psize = previewSizes.get(i);

                }
                parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);

                Size fs = null;
                for (int i = 0; i < pictureSizes.size(); i++) {
                    Size psize = pictureSizes.get(i);
                    if (fs == null && psize.width >= 1280)
                        fs = psize;

                }
                parameters.setPictureSize(fs.width, fs.height);

                if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", 90);

                    int orientation = CameraFacing == CameraInfo.CAMERA_FACING_FRONT ? 360 - mCameraInfo.orientation : mCameraInfo.orientation;
                    mCamera.setDisplayOrientation(orientation);

                } else {
                    parameters.set("orientation", "landscape");
                    mCamera.setDisplayOrientation(0);

                }
                
                if(CameraFacing == CameraInfo.CAMERA_FACING_BACK){
                	if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
    					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    				} else{
    					parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
    				}
                }
                
                mCamera.setParameters(parameters);
                mCamera.setPreviewCallback(this.mPreviewCallback);
                mCamera.startPreview();

                Camera.Size csize = mCamera.getParameters().getPreviewSize();

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

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraInit == 1 && mCamera == null) {
            openCamera(CameraFacing);
        }
    }

    @Override
    public void onPause() {

        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        super.onPause();
    }

    public Matrix getMatrix() {
        return matrix;
    }

}
