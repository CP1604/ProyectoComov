package inf.um.comov;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

public class CellInfoActivity extends AppCompatActivity {
    private final int MY_APP_PERMISSIONS = 1;
    private boolean readPhoneStatusGranted = false;
    private boolean coarse_permissions_granted = false;
    private boolean fine_permissions_granted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell);
        checkPermissions();
    }

    @SuppressLint("MissingPermission")
    public void getCellInfo() {
        //Check permissions

        if (readPhoneStatusGranted && coarse_permissions_granted) {
            //Obtenemos información

            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                //List<NeighboringCellInfo> neighCells = telephonyManager.getNeighboringCellInfo();
            }
            else {*/
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            StringBuilder text = new StringBuilder();
            text.append("Found").append(cellInfoList.size()).append(" cells\n");
            Log.e("DEBUG", "Empezando...");
            for (CellInfo info : cellInfoList) {
                if (info instanceof CellInfoWcdma) {
                    CellInfoWcdma infoWcdma = (CellInfoWcdma) info;
                    CellIdentityWcdma id = infoWcdma.getCellIdentity();

                    text.append("WCDMA ID: {cid: ").append(id.getCid());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" lac: ").append(id.getLac());
                    text.append("} Level:  ").append(infoWcdma.getCellSignalStrength().getLevel())
                            .append("\n");
                }
                else if (info instanceof CellInfoLte) {
                    CellInfoLte infoLte = (CellInfoLte) info;
                    CellIdentityLte id = infoLte.getCellIdentity();

                    text.append("LTE ID: {ci: ").append(id.getCi());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" tac: ").append(id.getTac());
                    text.append("} Level:  ").append(infoLte.getCellSignalStrength().getLevel())
                            .append("\n");
                }

                else if (info instanceof CellInfoGsm) {
                    CellInfoGsm infoGsm = (CellInfoGsm) info;
                    CellIdentityGsm id = infoGsm.getCellIdentity();

                    text.append("GSM ID: {cid: ").append(id.getCid());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" lac: ").append(id.getLac());
                    text.append("} Level:  ").append(infoGsm.getCellSignalStrength().getLevel())
                            .append("\n");
                }
            }
            Log.e("DEBUG", text.toString());
            TextView tv = findViewById(R.id.texto);
            tv.setText(text.toString());

        }
        else
            //No tenemos permisos y por tanto la funcionalidad no es accesible
            return;

    }

    public void checkPermissions() {
        Log.e("DEBUG", "Checking permissions HOLA");
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_APP_PERMISSIONS);
            }
            return;
        }
        readPhoneStatusGranted = true;
        coarse_permissions_granted = true;
        fine_permissions_granted = true;
        getCellInfo();
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
                        readPhoneStatusGranted = true;
                        coarse_permissions_granted = true;
                        fine_permissions_granted = true;
                        getCellInfo();
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

    /*GoogleMap fusedLocationClient;
    private LocationRequest mLocationRequest;
    private static final int MY_LOCATION_PERMISSION_FINE = 1;
    private Object Location;*/


/*
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, MY_LOCATION_PERMISSION_FINE);
            }
            return;
        }
        fusedLocationClient.setMyLocationEnabled(true);
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode) {
            case MY_LOCATION_PERMISSION_FINE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLastLocation(fusedLocationClient, this);
                } else {
                    //??
                }
            }
        }
    }

    private void getLastLocation(GoogleMap fusedLocationProviderClient, final inf.um.comov.Location activity) {
        fusedLocationProviderClient = LocationClient.getLocationFusedInstance(this);

        public void onSuccess(Location location) {
            if (location != null){
                LocationClient.location = location;
                LocationAddressModel address = LocationClient.getAddress(location.getLatitude(), location.getLongitude(), getApplicationContext());

            }
        }

    }

}*/
}