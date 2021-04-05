package inf.um.comov;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.UiModeManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.security.Signature;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.loadMap:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick2G(View v) {
        Intent intent = new Intent(this, MapsActivity.class);

        //Información para la actividad: se utilizará la tecnología 2G
        intent.putExtra("tecnology", "2G");
        startActivity(intent);
    }

    public void onClick3G(View v) {
        Intent intent = new Intent(this, MapsActivity.class);

        //Información para la actividad: se utilizará la tecnología 3G
        intent.putExtra("tecnology", "3G");
        startActivity(intent);
    }

    public void onClick4G(View v) {
        Intent intent = new Intent(this, MapsActivity.class);

        //Información para la actividad: se utilizará la tecnología 4G
        intent.putExtra("tecnology", "4G");
        startActivity(intent);
    }

    public void onClickLoadMap(View v) {
        //MEJORA OPCIONAL: guardar los mapas dibujados y ofrecer la funcionalidad de visualizarlos
    }


}