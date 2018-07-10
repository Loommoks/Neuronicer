package su.zencode.neuronicer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final int loadImage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loadImageButton = (Button) findViewById(R.id.load_image_button);
        loadImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loadImageIntent = new Intent(Intent.ACTION_PICK);

                loadImageIntent.setType("image/*");

                startActivityForResult(loadImageIntent,loadImage);
            }
        }

        );

    }
}
