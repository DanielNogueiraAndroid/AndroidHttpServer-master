package server.http.android.androidhttpserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import server.http.android.androidhttpserver.server.MyServer;
import server.http.android.androidhttpserver.server.interfaces.ServerInterface;


public class MainActivity extends AppCompatActivity implements ServerInterface, SurfaceHolder.Callback {

    private MyServer server;
    private final String TAG = MainActivity.class.getSimpleName();
    //public class VideoServer extends Activity implements SurfaceHolder.Callback {
    TextView testView;
    Camera camera;
    Boolean cameraOn = false;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    android.hardware.Camera.PictureCallback rawCallback;
    android.hardware.Camera.ShutterCallback shutterCallback;
    android.hardware.Camera.PictureCallback jpegCallback;
    private String lastPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");
        // para manter a tela ligada
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        rawCallback = new android.hardware.Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "onPictureTaken - raw");
            }
        };

        /** Handles data for jpeg picture */
        shutterCallback = new android.hardware.Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };
        jpegCallback = new android.hardware.Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    lastPhotoPath = String.format(
                            "/sdcard/%d.jpg", System.currentTimeMillis());
                    outStream = new FileOutputStream(lastPhotoPath);
                    outStream.write(data);
                    outStream.close();
                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    send(lastPhotoPath);
                }
                Log.d(TAG, "onPictureTaken - jpeg");
            }
        };


    }

    private void send(String lastPhotoPath) {
        if(server != null) {
            server.newImage(lastPhotoPath);
            Log.d(TAG, "server.newImage ");
        }else{

            Log.e(TAG, "Camera OFF ");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            server = new MyServer();
            server.setServerInterface(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(server != null) {
            server.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop_camera();
    }

    @Override
    public void newConnection() {
        if(cameraOn) {
            captureImage();
            Log.d(TAG, "captureImage() ");
        }else{

            Log.e(TAG, "Camera OFF ");
        }
    }

    private void captureImage() {
        // TODO Auto-generated method stub
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    //  http://stackoverflow.com/questions/16602736/android-send-an-image-through-socket-programming

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    private void start_camera() {
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            Log.e(TAG, "init_camera: " + e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();
        //modify parameter
        param.setPreviewFrameRate(20);
        param.setPreviewSize(176, 144);
        camera.setParameters(param);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            //camera.takePicture(shutter, raw, jpeg)
        } catch (Exception e) {
            Log.e(TAG, "init_camera: " + e);
            return;
        }

        cameraOn = true;

    }

    private void stop_camera() {
        camera.stopPreview();
        camera.release();
        cameraOn = false;
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
    }

    public void surfaceCreated(SurfaceHolder holder) {
        start_camera();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraOn = false;
    }

}
