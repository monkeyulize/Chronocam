package com.cevaone.chronocam;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

	private Camera mCamera;
	private SurfaceHolder mHolder;
	private List<Camera.Size> mSupportedPreviewSizes; 
	public Camera.Size mPreviewSize;
	public Camera.Size mBestPictureSize;
	public static Double mAspectRatio = 0.0;
	public ShowCamera(Context context, Camera camera) {
		
		super(context);
		Log.d("surface", "constructor");
		
		mCamera = camera;
		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
		mBestPictureSize = getBestPictureSize(mCamera.getParameters());
        mAspectRatio = ((double)mBestPictureSize.height / (double)mBestPictureSize.width);

		mHolder = getHolder();
		mHolder.addCallback(this);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("surface", "surfaceCreated");
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		Log.d("surface", "surfaceChanged" + Integer.valueOf(width).toString() + 'x' + Integer.valueOf(height).toString());
	
		
		Camera.Parameters mParameters = mCamera.getParameters();
		
		if((mPreviewSize != null) && (mBestPictureSize != null)) {
			mParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
			Log.d("PreviewSize", Integer.valueOf(mPreviewSize.width).toString() + 'x' + Integer.valueOf(mPreviewSize.height).toString());
			mParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mParameters.setPictureSize(mBestPictureSize.width, mBestPictureSize.height);
			mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			mCamera.setParameters(mParameters);
			mCamera.startPreview();
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHolder = null;
		mCamera = null;
		Log.d("surface", "surfaceDestroyed");
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d("onMeasure", "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        
		if (mSupportedPreviewSizes != null) {
           mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
           
        }
	}
	
	
	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) Math.max(height, width) / Math.min(height, width);
		
		if(sizes == null) {
			return null;
		}
		
		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		
		int targetHeight = height;
		
		for(Camera.Size size : sizes) {
			//Log.d("previewSizes", Integer.valueOf(size.width).toString() + 'x' + Integer.valueOf(size.height).toString());
			double ratio = (double) size.width / size.height;
			if(Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if(Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		
		if(optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for(Camera.Size size : sizes) {
				if(Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public static Camera.Size getBestPictureSize(Camera.Parameters parameters) {
		Camera.Size bestSize = null;
		List<Camera.Size>sizeList = parameters.getSupportedPictureSizes();
		bestSize = sizeList.get(0);
		
        for(int i = 1; i < sizeList.size(); i++) {
        	if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
       			bestSize = sizeList.get(i);
        	}
        }
        Log.d("bestPictureSize", Integer.valueOf(bestSize.width).toString() + 'x' + Integer.valueOf(bestSize.height).toString());

        return bestSize;
	}
	
	
/*	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        
        for(int i = 0; i < sizeList.size(); i++) {
        	Log.d("previewSizes", Integer.valueOf(sizeList.get(i).width).toString() + 'x' + Integer.valueOf(sizeList.get(i).height).toString());
        	if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
        		if((width * height) > (sizeList.get(i).width * sizeList.get(i).height)) {
        			bestSize = sizeList.get(i);
        		}
        	}
        }
        
        return bestSize;
	}*/
}
