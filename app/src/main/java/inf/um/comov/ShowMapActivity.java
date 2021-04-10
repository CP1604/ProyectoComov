package inf.um.comov;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ShowMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_APP_PERMISSIONS = 1;
    private GoogleMap mMap;
    private String fichero;
    private Location location;
    private boolean readExternalStorage_granted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        Bundle extras = getIntent().getExtras();
        fichero = extras.getString("fichero");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkPermissions();
        processFile();
        // Add a marker in Sydney and move the camera
        if (readExternalStorage_granted) {
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(point).title(point.latitude + " : " + point.longitude));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 14));
        }
    }

    private void processFile() {
        //Comprobar la extension: String ext = getFileExtension(URL);
        if (readExternalStorage_granted) {
            BufferedReader br = null;
            try {
                //Solo podemos leer del directorio de /saved_maps
                String root = Environment.getExternalStorageDirectory().toString();
                File dir = new File(root + "/saved_maps");
                br = new BufferedReader(new FileReader(dir + "/" + fichero));
            }
            catch (FileNotFoundException e) {
                Toast.makeText(ShowMapActivity.this, "El fichero no existe o no se encuentra en el " +
                        "directorio de la aplicación.",  Toast.LENGTH_SHORT).show();
                this.location = null;
                return;
            }
            //Si el fichero se ha podido abrir correctament lo leemos
            try {
                String strLine;
                Location l = null;
                strLine = br.readLine();
                if (strLine != null)
                    Log.d("DEBUG", strLine);
                strLine = br.readLine();

                if (strLine != null) {
                    Log.d("DEBUG", strLine);
                    String[] campos = strLine.split(";");
                    int v = Integer.parseInt(campos[1]);
                    String[] campos2 = campos[0].split(":");
                    l = new Location("");
                    l.setLongitude(Double.parseDouble(campos2[1]));
                    l.setLatitude(Double.parseDouble(campos2[0]));
                    drawCircleOnMap(l, v);
                    this.location = l;
                }
                strLine = br.readLine();

                while (strLine != null) {
                    Log.d("DEBUG", strLine);
                    String[] campos = strLine.split(";");
                    int v = Integer.parseInt(campos[1]);
                    String[] campos2 = campos[0].split(":");
                    l = new Location("");
                    l.setLongitude(Double.parseDouble(campos2[1]));
                    l.setLatitude(Double.parseDouble(campos2[0]));
                    drawCircleOnMap(l, v);
                    strLine = br.readLine();
                }
            } catch (Exception e) {
                //Capturamos excepciones que se producen por errores de IO o de formato de fichero incorrecto
                Toast.makeText(ShowMapActivity.this, "Se ha producido un error al leer el fichero."
                        ,  Toast.LENGTH_SHORT).show();
                this.location = null;
                return;
            }
        }
        else
            Toast.makeText(ShowMapActivity.this, "Necesitas dar los permisos necesarios."
                    ,  Toast.LENGTH_SHORT).show();
    }

    private void drawCircleOnMap(Location location, int color) {
        int c = -1;
        switch (color){
            case -2: //No tenemos permisos...
                c = Color.BLACK;
                break;
            case 1:
                c = Color.RED;
                break;
            case 2:
                c = Color.YELLOW;
                break;
            case 3:
                c = Color.BLUE;
                break;
            case 4:
                c = Color.GREEN;
                break;
            default:
                c = Color.WHITE;
        }
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(5)
                .strokeColor(c)
                .fillColor(c));
    }

    public void checkPermissions() {
        Log.e("DEBUG", "Checking permissions");
        List<String> permissions = new ArrayList<String>();
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            else permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (permissions.size() != 0) {
            String[] stringArray = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, stringArray
                    , MY_APP_PERMISSIONS);
            return;
        }
        readExternalStorage_granted = true;
        Log.e("DEBUG", "Permissions check has finished.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode) {
            case MY_APP_PERMISSIONS: {
                if (grantResults.length > 0) {
                    boolean granted = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            granted = false;
                            break;
                        }
                    }
                    if (granted) {
                        Log.e("DEBUG", "Permissions Granted.");
                        readExternalStorage_granted = true;
                        onMapReady(mMap);
                    }
                    else
                        Log.e("DEBUG", "There was a problem about permissions.");
                } else {
                    Log.e("DEBUG", "There was a problem about permissions.");
                }
                break;
            }
        }
    }

}