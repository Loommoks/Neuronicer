package su.zencode.neuronicer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class Loader extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);
        Toast.makeText(this, "Wait few seconds", Toast.LENGTH_SHORT).show();
        Intent startIntent =  new Intent(this,AndroidNetworkLoader.class);
        startActivity(startIntent);

    }
}
