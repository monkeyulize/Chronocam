package com.cevaone.chronocam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


public class MainActivity extends Activity {

	private static final int SET_PORTRAIT = 1;
	private static final int SET_LANDSCAPE = 2;
	private static final int SET_REVERSE_LANDSCAPE = 3;
	private static final int SELECT_PICTURE = 4;
	private static final int SELECT_PICTURE_KITKAT = 5;
	private static final int CAPTURE_PORTRAIT = 6;
	private static final int CAPTURE_LANDSCAPE = 7;
	private int capture_orientation = CAPTURE_PORTRAIT; //TODO should probably be changed to allow startup in landscape
	private Camera camera;
	private ShowCamera showCamera;
	private String filename;
	public static int mScreenWidth, mScreenHeight;
	private int mPreviewWidth, mPreviewHeight;
	
	/* layout */
	
	private RelativeLayout portraitLayout, landscapeLayout;
	public static ImageView img;
	public static FrameLayout preview;
	private SeekBar transparency_seekBar, transparency_seekBar_land;
	private Button img_select, img_clear, button_capture;
	private Button img_select_land, img_clear_land, button_capture_land;
	
	/* layout */
	
	
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
	
	File outFile;
	FileOutputStream fos;
	
	private void setPreviewDimensions(int which) {
		switch(which) {
		case SET_PORTRAIT:
			Log.d("DEBUG", "SET PORTRAIT");
			preview.getLayoutParams().width = mPreviewWidth;
			preview.getLayoutParams().height = mPreviewHeight;
			img.getLayoutParams().width = preview.getLayoutParams().width;
	        img.getLayoutParams().height = preview.getLayoutParams().height;
	        
	        img_select.getLayoutParams().height = mScreenHeight - mPreviewHeight;
	        img_clear.getLayoutParams().height = mScreenHeight - mPreviewHeight;
	        button_capture.getLayoutParams().height = mScreenHeight - mPreviewHeight;
	        
	        img_select.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
	        img_clear.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
	        button_capture.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
	        break;
		case SET_LANDSCAPE:
			Log.d("DEBUG", "SET LANDSCAPE");
			preview.getLayoutParams().width = mPreviewHeight;
			preview.getLayoutParams().height = mPreviewWidth;
			img.getLayoutParams().width = preview.getLayoutParams().width;
	        img.getLayoutParams().height = preview.getLayoutParams().height;
	        
	        img_select_land.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
	        img_clear_land.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
	        button_capture_land.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
	        
	        img_select_land.getLayoutParams().width = mScreenHeight - mPreviewHeight;
	        img_clear_land.getLayoutParams().width = mScreenHeight - mPreviewHeight;
	        button_capture_land.getLayoutParams().width = mScreenHeight - mPreviewHeight;
	        break;
		case SET_REVERSE_LANDSCAPE:
			
			break;
		}
	}
	
	
	
	
	public static Camera isCameraAvailable() {
		Camera object = null;
		try {
			object = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return object;
		
	}
	
	
	private PictureCallback capturedIt = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			
			switch(capture_orientation) {
			case CAPTURE_PORTRAIT:
				bitmap = RotateBitmap(bitmap, 90);
				break;
			case CAPTURE_LANDSCAPE:
				
				break;
			}
			
			filename = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());

			filename += ".jpeg";
			Log.d("filename", filename);
			
			outFile = new File(Environment.getExternalStorageDirectory(), filename);
			try {
				fos = new FileOutputStream(outFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos); 
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				camera.startPreview();
			}
			
			if(bitmap == null) {
				Toast.makeText(getApplicationContext(), "Not Captured", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "Captured!", Toast.LENGTH_SHORT).show();
				scanFile(outFile.getAbsolutePath());
			}
		}
	};
	
	
	private void scanFile(String path) {
        MediaScannerConnection.scanFile(MainActivity.this,
        	new String[] { path }, null,
            new MediaScannerConnection.OnScanCompletedListener() {

        		public void onScanCompleted(String path, Uri uri) {
        			Log.i("TAG", "Finished scanning " + path);
        		}
        	});
    }
	
	
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
	
	public void selectImage(View view) {
		if(Build.VERSION.SDK_INT < 19) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, SELECT_PICTURE);
		} else {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			startActivityForResult(intent, SELECT_PICTURE_KITKAT);
		}
	}
	
	public void clearImage(View view) {
		img.setImageDrawable(null);
		img.setBackground(null);
    	editor.remove(getString(R.string.pref_imageUri));
    	editor.apply();
    	transparency_seekBar.setProgress(50);
	}
	
	@SuppressLint("NewApi")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			if(requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				Log.d("uri", selectedImageUri.toString());
				img.setImageBitmap(getScaledBitmap(selectedImageUri));
				editor.putString(getString(R.string.pref_imageUri), selectedImageUri.toString());
				editor.apply();
				img.setAlpha(0.5f);
				transparency_seekBar.setProgress(50);
			} else if(requestCode == SELECT_PICTURE_KITKAT) {
				Uri selectedImageUri = data.getData();
				Log.d("uri", selectedImageUri.toString());
				final int takeFlags = data.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION
					| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				
				getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
				img.setImageBitmap(getScaledBitmap(selectedImageUri));
				editor.putString(getString(R.string.pref_imageUri), selectedImageUri.toString());
				editor.apply();
				img.setAlpha(0.5f);
				transparency_seekBar.setProgress(50);
			}
		}
	}
	
	public Bitmap getScaledBitmap(Uri uri) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inSampleSize = 4;
		String pathName = getPath(this, uri);
		bm = BitmapFactory.decodeFile(pathName, options);
		return bm;
	}
	
	public String getPath(Context context, Uri uri) {
		  Cursor cursor = null;
		  try { 
		    String wholeID = DocumentsContract.getDocumentId(uri);
		    String id = wholeID.split(":")[1];
		    String[] column = {MediaStore.Images.Media.DATA};
		    String sel = MediaStore.Images.Media._ID + "=?";
		    cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);
		    int columnIndex = cursor.getColumnIndex(column[0]);
		    cursor.moveToFirst();
		    return cursor.getString(columnIndex);
		  } finally {
		    if (cursor != null) {
		      cursor.close();
		    }
		  }
 	}
	
	private void acquire_and_add_camera() {
		preview.removeAllViews();
		if(camera != null) {
			camera.release();
			camera = null;
		}
		Log.d("acquire", "camera acquired");
		camera = isCameraAvailable();
		showCamera = new ShowCamera(this, camera);
		mPreviewWidth = MainActivity.mScreenWidth;
		mPreviewHeight = (int) (MainActivity.mScreenWidth / ShowCamera.mAspectRatio);
		Integer rotation = getWindowManager().getDefaultDisplay().getRotation();
		switch(rotation) {
		case Surface.ROTATION_0:
			Log.d("orientation", "ROTATION_0");
			setPreviewDimensions(SET_PORTRAIT);
			camera.setDisplayOrientation(90);
			break;
		case Surface.ROTATION_90:
			Log.d("orientation", "ROTATION_90");
			setPreviewDimensions(SET_LANDSCAPE);
			camera.setDisplayOrientation(0);
			break;
		case Surface.ROTATION_270:
			Log.d("orientation", "ROTATION_270");
			setPreviewDimensions(SET_LANDSCAPE);
			camera.setDisplayOrientation(180);
			break;
		}
		preview.addView(showCamera);
	}
	
	private void setRotationAnimation() {
	    int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
	    Window win = getWindow();
	    WindowManager.LayoutParams winParams = win.getAttributes();
	    winParams.rotationAnimation = rotationAnimation;
	    win.setAttributes(winParams);
	}
	
	
	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		Integer rotation = getWindowManager().getDefaultDisplay().getRotation();
		switch(rotation) {
		case Surface.ROTATION_0:
			Log.d("orientation", "ROTATION_0");
			setPreviewDimensions(SET_PORTRAIT);
			change_buttons(true);
			camera.setDisplayOrientation(90);
			capture_orientation = CAPTURE_PORTRAIT;
			break;
		case Surface.ROTATION_90:
			Log.d("orientation", "ROTATION_90");
			setPreviewDimensions(SET_LANDSCAPE);
			change_buttons(false);
			camera.setDisplayOrientation(0);
			capture_orientation = CAPTURE_LANDSCAPE;
			break;
		case Surface.ROTATION_270:
			Log.d("orientation", "ROTATION_270");
			change_buttons(false);
			setPreviewDimensions(SET_LANDSCAPE);
			camera.setDisplayOrientation(180);
			break;
		}
		transparency_seekBar = (SeekBar) findViewById(R.id.transparency_seekBar);
		transparency_seekBar.setOnSeekBarChangeListener(transparency_seekBar_listener);
		super.onConfigurationChanged(newConfig);
	}
	
	public void change_buttons(boolean portrait) {
		if(portrait) {
			portraitLayout.setVisibility(View.VISIBLE);
			landscapeLayout.setVisibility(View.GONE);
		} else {
			portraitLayout.setVisibility(View.GONE);
			landscapeLayout.setVisibility(View.VISIBLE);
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("activityState", "onCreate");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        String saved_img_uri = sharedPref.getString(getString(R.string.pref_imageUri), "");

        setRotationAnimation();
        
        portraitLayout = (RelativeLayout) findViewById(R.id.portrait_layout);
        landscapeLayout = (RelativeLayout) findViewById(R.id.landscape_layout);
        
        img = (ImageView) findViewById(R.id.shadow_img);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        transparency_seekBar = (SeekBar) findViewById(R.id.transparency_seekBar);
        transparency_seekBar_land = (SeekBar) findViewById(R.id.transparency_seekBar_land);
        
        img_clear = (Button) findViewById(R.id.img_clear);
        img_select = (Button) findViewById(R.id.img_select);
        button_capture = (Button) findViewById(R.id.button_capture);
        
        img_clear_land = (Button) findViewById(R.id.img_clear_land);
        img_select_land = (Button) findViewById(R.id.img_select_land);
        button_capture_land = (Button) findViewById(R.id.button_capture_land);
        
        transparency_seekBar.setOnSeekBarChangeListener(transparency_seekBar_listener);
        transparency_seekBar_land.setOnSeekBarChangeListener(transparency_seekBar_listener);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
        
        if(saved_img_uri != "") {
        	img.setImageURI(Uri.parse(saved_img_uri));
        	img.setAlpha(0.5f);
        	transparency_seekBar.setProgress(50);
        }
    }

    
    SeekBar.OnSeekBarChangeListener transparency_seekBar_listener = new OnSeekBarChangeListener() {
		
		  @Override
		  public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
			  img.setAlpha(((float)progressValue / 100));
			  //Log.d("seekBar", "Changing seekbar's progress");
		  }
		
		  @Override
		  public void onStartTrackingTouch(SeekBar seekBar) {
		  }
		
		  @Override
		  public void onStopTrackingTouch(SeekBar seekBar) {
		  }
	};
    
    
    
    public void snapIt(View view) {
    	camera.takePicture(null, null, capturedIt);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	acquire_and_add_camera();
    	Log.d("activityState", "onStart");
    }
    
    @Override
    protected void onPause() {
    	if(camera != null) {
    		camera.release();
    		camera = null;
    		showCamera = null;

    	}
   	
    	super.onPause();
    	Log.d("activityState", "onPause");
    }
    
    @Override
    protected void onStop() {
    	if(camera != null) {
    		camera.release();
    		camera = null;
    		showCamera = null;
    	}
    	Log.d("activityState", "onStop");
    	editor.remove(getString(R.string.pref_imageUri));
    	editor.apply();
    	super.onStop();
    }


    
    
    
}
