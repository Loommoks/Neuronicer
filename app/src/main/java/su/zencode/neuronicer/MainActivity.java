package su.zencode.neuronicer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    boolean permissionRequestTest = true;
    public static final String NETWORK_CODE = "net";
    private final int loadImage = 1;
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 0;
    private static final int BITMAP_TARGET_DIMENSION = 28;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;
    private ImageView imageView;
    private ImageView thumbnailImageView;
    private ImageView grayScaleImageView;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            Toast.makeText(getApplicationContext(),"Camera connected",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private HandlerThread mBackgroungHandlerThread;
    private Handler mBackgroundHandler;
    private String mCameraId;
    private Size mPreviewSize;
    private Size mImageSize;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new
            ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    mBackgroundHandler.post(new ImageSaver(imageReader.acquireLatestImage()));
                }
            };
    private class ImageSaver implements Runnable {

        private final Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

        }
    }

    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult captureResult){
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            //Ничего не делает, todo рассмотреть механизм
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(),"AF locked", Toast.LENGTH_SHORT).show();
                                startStillCaptureRequest();
                            }

                            break;
                    }
                }
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process(result);
                }
            };
    private CaptureRequest.Builder mCaptureRequestBuilder;

    private ImageButton mRecordImageButton;
    private boolean mIsRecording = false;

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0,0);
        ORIENTATIONS.append(Surface.ROTATION_90,90);
        ORIENTATIONS.append(Surface.ROTATION_180,180);
        ORIENTATIONS.append(Surface.ROTATION_270,270);
    }

    Button loadImageButton;
    Button videoCaptureButton;
    Button takePictureButton;
    private ImageButton mStillImageButton;

    Bitmap bitmapToCrop;
    Bitmap croppedBitmap;
    Bitmap grayscaleBitmap;
    Network net;

    private static class CompareSizeByArea implements Comparator<Size>{
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }

    private void closeCamera() {
        if (mCameraDevice!=null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //InputStream inputStream = getResources().openRawResource(R.raw.network980);
        //Intent netLoadIntent = new Intent(this,AndroidNetworkLoader.class);
        //startActivity(netLoadIntent);
        //AndroidNetworkLoader loadNetwork = new LoadNetwork();
        //net = loadNetwork.go(inputStream);
        readAFile();


        mRecordImageButton = (ImageButton) findViewById(R.id.videoOnLineImageButton);
        mRecordImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsRecording) {
                    mIsRecording = false;
                    mRecordImageButton.setImageResource(R.drawable.baseline_videocam_off_black_24dp);
                } else {
                    mIsRecording = true;
                    mRecordImageButton.setImageResource(R.drawable.baseline_videocam_black_24dp);
                }
            }
        });

        imageView = (ImageView) findViewById(R.id.image_view);
        mTextureView = (TextureView) findViewById(R.id.texture_view);
        mStillImageButton = (ImageButton) findViewById(R.id.cameraImageButton2);
        mStillImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                lockFocus();
            }
        });
        thumbnailImageView = (ImageView) findViewById(R.id.image_thumbnail);
        grayScaleImageView = (ImageView) findViewById(R.id.grayscale_image_thumbnail);

        loadImageButton = (Button) findViewById(R.id.load_image_button);
        loadImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextureView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                mRecordImageButton.setVisibility(View.INVISIBLE);
                mStillImageButton.setVisibility(View.INVISIBLE);
                Intent loadImageIntent = new Intent(Intent.ACTION_PICK);

                loadImageIntent.setType("image/*");

                startActivityForResult(loadImageIntent,loadImage);
            }
        });


        // assert mTextureView != null;
        /*mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });*/

        videoCaptureButton = (Button) findViewById(R.id.capture_video);
        videoCaptureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setVisibility(View.INVISIBLE);
                mTextureView.setVisibility(View.VISIBLE);
                mRecordImageButton.setVisibility(View.VISIBLE);
                mStillImageButton.setVisibility(View.VISIBLE);

            }
        });

        takePictureButton = (Button) findViewById(R.id.take_picture);
        takePictureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //takePicture();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if(mTextureView.isAvailable()){
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "App can't use camera without permission",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent imageReturnedIntent){
        super.onActivityResult(requestCode,resultCode,imageReturnedIntent);

        if (requestCode == loadImage && resultCode == RESULT_OK){

            try {
                final Uri imageUri = imageReturnedIntent.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                bitmapToCrop = selectedImage;
                imageView.setImageBitmap(selectedImage);
                cropImage();
                grayscaleImage();

            } catch (FileNotFoundException ex){
                ex.printStackTrace();
            }
        }
    }

    private void setupCamera(int width, int height){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                int totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth,rotatedHeight);
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth,rotatedHeight);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(),mImageSize.getHeight(), ImageFormat.JPEG,1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    //todo Заменил проверку на заглушку (обязательный запрос разрешения доступа к камере)
                    //запрос был: shouldShowRequestPermissionRationale(Manifest.permission.CAMERA
                    if(permissionRequestTest) {
                        Toast.makeText(this,
                                "App requires access camera, or we can't feed the Network ", Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
                    }
                }
            } else {
                cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            mPreviewCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(getApplicationContext(), "Something goes wrong :( Can't setup camera preview)", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);
                            //todo здесь можно создать файл/директория, если мы хотим сохранить изображение (!пока не нужно!)
                        }
                    };
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void cropImage (){
        croppedBitmap = ThumbnailUtils.extractThumbnail(bitmapToCrop,BITMAP_TARGET_DIMENSION,BITMAP_TARGET_DIMENSION);
        thumbnailImageView.setImageBitmap(croppedBitmap);
        //grayScaleImageView.setImageBitmap(croppedBitmap);

        int bitmapWidth = croppedBitmap.getWidth();
        Toast bw = Toast.makeText(getApplicationContext(),"Width: "+bitmapWidth,Toast.LENGTH_SHORT);
        bw.setGravity(Gravity.TOP,0,0);
        //bw.show();
        int bitmapHeight = croppedBitmap.getHeight();
        Toast bh = Toast.makeText(getApplicationContext(),"Height: "+bitmapHeight,Toast.LENGTH_SHORT);
        bh.setGravity(Gravity.TOP,0,200);
        //bh.show();

        int[] pixels = new int[bitmapWidth * bitmapHeight];
        bw.setText("Array lenght: "+pixels.length);
        bw.setGravity(Gravity.CENTER,0,0);
        //bw.show();

        croppedBitmap.getPixels(pixels,0,bitmapWidth,0,0,bitmapWidth,bitmapHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmapWidth,bitmapHeight, Bitmap.Config.ARGB_8888);
        newBitmap.setPixels(pixels,0,bitmapWidth,0,0,bitmapWidth,bitmapHeight);
        //grayScaleImageView.setImageBitmap(newBitmap);
    }

    public void grayscaleImage(){
        int bitmapWidth = croppedBitmap.getWidth();
        int bitmapHeight = croppedBitmap.getHeight();
        int[] pixels = new int[ bitmapHeight * bitmapWidth ];
        double[] inputForNetwork = new double[bitmapHeight * bitmapWidth];
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;
        int A, R, G, B;
        int pixel;
        croppedBitmap.getPixels(pixels,0,bitmapWidth,0,0,bitmapWidth,bitmapHeight);
        //Toast.makeText(this, ""+Color.alpha(pixels[0])+" "+Color.red(pixels[0])+" "+Color.green(pixels[0])+" "+Color.blue(pixels[0]), Toast.LENGTH_SHORT).show();
        int colorMiddle =0;
        grayscaleBitmap = Bitmap.createBitmap(bitmapWidth,bitmapHeight, Bitmap.Config.ARGB_8888);

        for(int i=0; i<bitmapWidth;i++) {
            for (int j = 0; j < bitmapHeight; j++) {

                //***
                pixel = croppedBitmap.getPixel(i, j);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                colorMiddle = colorMiddle + (+R + G + B) / 3;
            }
        }
        colorMiddle=colorMiddle/748;

                for(int i=0; i<bitmapWidth;i++){
            for (int j=0;j<bitmapHeight;j++){

                //***
                pixel = croppedBitmap.getPixel(i, j);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                //colorMiddle = colorMiddle+(+R+G+B)/3;
                // take conversion up to one single value
                R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                // set new pixel color to output bitmap
                if (R>colorMiddle*0.85){
                    inputForNetwork[i+j*28] = 0;
                    grayscaleBitmap.setPixel(i,j,Color.argb(255,255,255,255));
                } else {
                    inputForNetwork[i+j*28] = 255;
                grayscaleBitmap.setPixel(i, j, Color.argb(255, 0, 0, 0));
                }
                //***

                /*pixel = (Color.red(croppedBitmap.getPixel(i,j))
                                +Color.green(croppedBitmap.getPixel(i,j))
                                +Color.blue(croppedBitmap.getPixel(i,j))
                )/3;*/
                //pixel = Color.blue(pixels[i])*1000;
                //inputForNetwork[i+j*27] = 255-R;
                //grayscaleBitmap.setPixel(i,j,Color.argb(255,pixel,pixel,pixel));
            }
        }
        //colorMiddle=colorMiddle/748;
        System.out.println("Среднее значение цвета: "+colorMiddle);
        /*
        for(int y=0; y<bitmapHeight;y++){
            for (int x=0;x<bitmapWidth;x++){
                pixel = (Color.red(grayscaleBitmap.getPixel(x,y))
                        +Color.green(grayscaleBitmap.getPixel(x,y))
                        +Color.blue(grayscaleBitmap.getPixel(x,y))
                )/3;
                //pixel = Color.blue(pixels[i])*1000;
                inputForNetwork[y*28+x] = 255-pixel;
                System.out.println("Пиксель ["+(y*28+x)+"] = "+inputForNetwork[y*28+x]);
            }
        }*/

        //grayscaleBitmap.setPixels(pixels,0,bitmapWidth,0,0,bitmapWidth,bitmapHeight);
        grayScaleImageView.setImageBitmap(grayscaleBitmap);
        int answer = net.startAndroidNetworking(inputForNetwork);
        System.out.println(answer);
        //net.showNetworkData();
        Toast.makeText(this, ""+answer, Toast.LENGTH_LONG).show();

    }

    public void readAFile() {
        try {

            InputStream inputStream = getResources().openRawResource(R.raw.network995);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            LinesConvertor linesConvertor = new LinesConvertor();
            String lineToConvert;
            while ((lineToConvert = reader.readLine()) !=null){
                linesConvertor.readALine(lineToConvert);
            }
            reader.close();
            net = linesConvertor.getNet();
            Toast.makeText(this,"Dimensions: " +net.getNetworkDimension()[0], Toast.LENGTH_LONG).show();


        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void startBackgroundThread() {
        mBackgroungHandlerThread = new HandlerThread("Camera2VideoStream");
        mBackgroungHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroungHandlerThread.getLooper());

    }

    private void stopBackgroundThread() {
        mBackgroungHandlerThread.quitSafely();
        try {
            mBackgroungHandlerThread.join();
            mBackgroungHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        System.out.println("sensorToDeviceRotation in progress...");
        return (sensorOrientation + deviceOrientation +360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        System.out.println("chooseOptimalSize in progress...");
        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough,new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private void lockFocus() {
        mCaptureState = STATE_WAIT_LOCK;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}
