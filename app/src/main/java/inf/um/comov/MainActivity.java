package inf.um.comov;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;
import java.security.Signature;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
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
                onClickLoadMap();
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

    public void onClickLoadMap() {
        //MEJORA OPCIONAL: guardar los mapas dibujados y ofrecer la funcionalidad de visualizarlos
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Load"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("DEBUG", "File Uri: " + uri.toString());
                    // Get the path
                    String path = queryName(getApplicationContext().getContentResolver(), uri);
                    Log.d("DEBUG", "File Path: " + path);
                    Intent intent = new Intent(this, ShowMapActivity.class);
                    intent.putExtra("fichero", path);
                    startActivity(intent);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    /*private String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }*/


}