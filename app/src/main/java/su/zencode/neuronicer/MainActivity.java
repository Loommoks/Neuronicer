package su.zencode.neuronicer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    public static final String NETWORK_CODE = "net";
    private final int loadImage = 1;
    private static final int BITMAP_TARGET_DIMENSION = 28;
    private ImageView imageView;
    private ImageView thumbnailImageView;
    private ImageView grayScaleImageView;

    Bitmap bitmapToCrop;
    Bitmap croppedBitmap;
    Bitmap grayscaleBitmap;
    Network net;

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


        imageView = (ImageView) findViewById(R.id.image_view);
        thumbnailImageView = (ImageView) findViewById(R.id.image_thumbnail);
        grayScaleImageView = (ImageView) findViewById(R.id.grayscale_image_thumbnail);

        Button loadImageButton = (Button) findViewById(R.id.load_image_button);
        loadImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loadImageIntent = new Intent(Intent.ACTION_PICK);

                loadImageIntent.setType("image/*");

                startActivityForResult(loadImageIntent,loadImage);
            }
        });



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
        bw.show();

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



        grayscaleBitmap = Bitmap.createBitmap(bitmapWidth,bitmapHeight, Bitmap.Config.ARGB_8888);
        for(int i=0; i<bitmapWidth;i++){
            for (int j=0;j<bitmapHeight;j++){
                pixel = (Color.red(croppedBitmap.getPixel(i,j))
                                +Color.green(croppedBitmap.getPixel(i,j))
                                +Color.blue(croppedBitmap.getPixel(i,j))
                )/3;
                //pixel = Color.blue(pixels[i])*1000;
                //inputForNetwork[i+j] = (255-pixel)/255;
                grayscaleBitmap.setPixel(i,j,Color.argb(255,pixel,pixel,pixel));
            }
        }

        for(int y=0; y<bitmapHeight;y++){
            for (int x=0;x<bitmapWidth;x++){
                pixel = (Color.red(grayscaleBitmap.getPixel(x,y))
                        +Color.green(grayscaleBitmap.getPixel(x,y))
                        +Color.blue(grayscaleBitmap.getPixel(x,y))
                )/3;
                //pixel = Color.blue(pixels[i])*1000;
                inputForNetwork[y+x] = 255-pixel;
            }
        }

        //grayscaleBitmap.setPixels(pixels,0,bitmapWidth,0,0,bitmapWidth,bitmapHeight);
        grayScaleImageView.setImageBitmap(grayscaleBitmap);
        int answer = net.startAndroidNetworking(inputForNetwork);
        System.out.println(answer);
        Toast.makeText(this, ""+answer, Toast.LENGTH_LONG).show();

    }

    public void readAFile() {
        try {

            InputStream inputStream = getResources().openRawResource(R.raw.network980);
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

}
