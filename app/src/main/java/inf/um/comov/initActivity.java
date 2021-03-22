package inf.um.comov;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ProgressBar;

public class initActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        ProgressBar pb = findViewById(R.id.simpleProgressBar);
        for(int i = 0; i< 100; i++){
            try {
                wait(1000);
                pb.setProgress(i);
            } catch (InterruptedException w){

            }
        }
    }

}