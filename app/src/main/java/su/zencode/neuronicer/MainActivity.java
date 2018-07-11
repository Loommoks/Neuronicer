package su.zencode.neuronicer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    Bitmap bitmapToCrop;
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

        Button loadImageButton = (Button) findViewById(R.id.load_image_button);
        loadImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loadImageIntent = new Intent(Intent.ACTION_PICK);

                loadImageIntent.setType("image/*");

                startActivityForResult(loadImageIntent,loadImage);
            }
        });

        Button cropImageButton = (Button) findViewById(R.id.crop_button);
        cropImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmapToCrop = ThumbnailUtils.extractThumbnail(bitmapToCrop,BITMAP_TARGET_DIMENSION,BITMAP_TARGET_DIMENSION);
                imageView.setImageBitmap(bitmapToCrop);

                int bitmapWidth = bitmapToCrop.getWidth();
                Toast bw = Toast.makeText(getApplicationContext(),"Width: "+bitmapWidth,Toast.LENGTH_SHORT);
                bw.setGravity(Gravity.TOP,0,0);
                bw.show();
                int bitmapHeight = bitmapToCrop.getHeight();
                Toast bh = Toast.makeText(getApplicationContext(),"Height: "+bitmapHeight,Toast.LENGTH_SHORT);
                bh.setGravity(Gravity.TOP,0,200);
                bh.show();

                int[] pixels = new int[bitmapWidth * bitmapHeight];
                bw.setText("Array lenght: "+pixels.length);
                bw.setGravity(Gravity.CENTER,0,0);
                bw.show();

                bitmapToCrop.getPixels(pixels,0,bitmapWidth,0,0,bitmapWidth,bitmapHeight);
                Bitmap newBitmap = Bitmap.createBitmap(bitmapWidth,bitmapHeight, Bitmap.Config.ARGB_8888);
                newBitmap.setPixels(pixels,0,bitmapWidth,0,0,bitmapWidth,bitmapHeight);
                imageView.setImageBitmap(newBitmap);
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
            } catch (FileNotFoundException ex){
                ex.printStackTrace();
            }
        }
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
