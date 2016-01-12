package com.lwansbrough.ReactCamera;

import android.content.Context;
import android.view.View;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.bridge.ReactApplicationContext;

import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.hardware.Camera;
import java.io.IOException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.Surface;

class ReactCameraView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder surfaceHolder;
    Camera camera = null;
    ThemedReactContext context;
    CameraInstanceManager cameraInstanceManager;
    private Camera.Size previewSize;
    ReactApplicationContext reactApplicationContext;
    int cameraId = -1;

    public ReactCameraView(ThemedReactContext context, CameraInstanceManager cameraInstanceManager) {
        super(context);
        this.context = context;
        this.reactApplicationContext = reactApplicationContext;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        this.cameraInstanceManager = cameraInstanceManager;
    }

    public void maybeUpdateView() {
        return;
    }


    private Camera.Size getBestPictureSize(Camera.Parameters parameters) {
        Camera.Size result=null;
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result==null) {
                result=size;
            }
            int resultArea=result.width*result.height;
            int newArea=size.width*size.height;

            if (newArea>resultArea && newArea < 500000) {
                result=size;
            }
        }
        return(result);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("ReactCameraView", "surfaceCreated");
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFocusMode("continuous-picture");
                Camera.Size size = this.getBestPictureSize(parameters);
                parameters.setPictureSize(size.width, size.height);
                camera.setPreviewDisplay(getHolder());
                camera.setParameters(parameters);
                cameraInstanceManager.updateCameraOrientation(camera);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(cameraId >= 0) {
            camera = cameraInstanceManager.getCamera(cameraId);
            surfaceCreated(getHolder());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("ReactCameraView", "surfaceDestroyed");
        if (camera != null) {
            camera.stopPreview();
            cameraInstanceManager.releaseCamera(camera);
            camera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.v("ReactCameraView", "surfaceChanged");
    }

    public void updateCamera(Camera newCamera) {
        if (newCamera != camera) {
            try {
                camera = newCamera;
                cameraId = cameraInstanceManager.getCameraId(camera);
                surfaceCreated(getHolder());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
