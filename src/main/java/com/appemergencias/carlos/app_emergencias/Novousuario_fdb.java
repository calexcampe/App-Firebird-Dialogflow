package com.appemergencias.carlos.app_emergencias;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;


public class Novousuario_fdb extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    Cadastro_local usuario = new Cadastro_local();
    List<Cadastro_local> listacad = new ArrayList<>();
    Sqlite sqlite;
    Insereusuariofdb inserirProdutoTask;
    Insereocorrencia insereocorrenciaTask;
    private LocationManager gerenciadordeLugar = null;
    private LocationListener lugarListener = null;
    private static final String TAG = "Debug";
    double lat, lg = 0;
    String endereco;
    ProgressDialog pDialog1 = null;
    private ProgressBar pb = null;
    Location location; // location
    private Boolean flag = false;
    protected LocationManager locationManager;
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2, REQUEST_CAMERA_PERMISSION = 0x3;
    List<Address> addresses;
    Location mylocation;
    GoogleApiClient googleApiClient;
    static TextView username, locationTxt;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    double latitude; // latitude
    double longitude; // longitude
    long codservico;
    String msg;
    String mPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);


        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("usuario").toString().equals("s")) {

            pegausuario();
        }

        if (bundle.getString("usuario").toString().equals("n")) {


            codservico = bundle.getLong("codservico");
            msg =( bundle.getString("ocor"));

            peganumero();

        }
    }

    public void peganumero() {

        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        int permissiontelefone = ContextCompat.checkSelfPermission(Novousuario_fdb.this,
                READ_PHONE_STATE);
        List<String> listPermissionfone = new ArrayList<>();
        if (permissiontelefone != PackageManager.PERMISSION_GRANTED) {
            listPermissionfone.add(READ_PHONE_STATE);
            if (!listPermissionfone.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionfone.toArray(new String[listPermissionfone.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
               Toast.makeText(Novousuario_fdb.this,"Voce nao forneceu as permissões reinicie o App",Toast.LENGTH_LONG).show();
               finish();
            }
        } else {
            mPhoneNumber = tMgr.getLine1Number();
            setUpGClient();

        }





    }

    @Override
    public void onConnected(Bundle bundle) {
        checkPermissions();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    private void checkPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(Novousuario_fdb.this,
                ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                Toast.makeText(Novousuario_fdb.this,"Voce nao forneceu as permissões reinicie o App",Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            getMyLocation();

        }


    }

   // @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        if ( mylocation != null) {
             latitude = mylocation.getLatitude();
             longitude = mylocation.getLongitude();
           // Log.v(TAG, "lat = " + LocationActivity.lat + "&lon=" + LocationActivity.lon);
           // Log.e(TAG, "setLocationTxt: " + mylocation);
          //  refreshLocation();
        } //else if (!LocationActivity.location.equals(getString(R.string.world_wide))) {
          //  locationTxt.setText(LocationActivity.location);
       // }
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (com.google.android.gms.location.LocationListener) this);
            insereocorrenciaTask = new Insereocorrencia();
            insereocorrenciaTask.execute();
        }
    }


    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
       // getMyLocation();
    }

    private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(Novousuario_fdb.this,
                        ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) Novousuario_fdb.this);
                    PendingResult result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(Novousuario_fdb.this,
                                                    ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                        Log.v(TAG, "mylocation=" + mylocation);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(Novousuario_fdb.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way to fix the
                                    // settings so we won't show the dialog.
                                    //finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        locationTxt.setText(getString(R.string.world_wide));
                        break;
                }
                break;
        }
    }


    public void pegausuario(){

        sqlite = new Sqlite(Novousuario_fdb.this);
        listacad = sqlite.pegacadastro();

        inserirProdutoTask = new Insereusuariofdb();
        inserirProdutoTask.execute();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class Insereusuariofdb extends AsyncTask<Cadastro_local, Void, String> {
        ConexaoFDB conec = new ConexaoFDB();
        private PreparedStatement select = null;
        private ResultSet rs = null;
        private String result = "";
        ProgressDialog progress = new ProgressDialog(Novousuario_fdb.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.novousuariofdb);

            progress.setMessage("Salvando dados no servidor...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

        }

        @Override
        protected String doInBackground(Cadastro_local... params) {

            result = conec.conectarFDBString(Novousuario_fdb.this);

            if (result.equals("erro")) {
                conec.fecharConexoes();
                return result;
            }
            try{
                //verifica se ja abertura de mesa ja
                select = null;
                long contausuario = 0;
                select = conec.getConnection().prepareStatement("select count(*) from usuario where telefone = " +
                        ""+listacad.get(0).getTelefone().toString());
                rs = select.executeQuery();
                rs.next();
                contausuario = rs.getLong(1);
                if(contausuario == 0) {

                    select = null;
                    String abremesa = " EXECUTE PROCEDURE NOVOUSUARIO( ?, ? ) ";
                    select = conec.getConnection().prepareStatement(abremesa);
                    String nome = listacad.get(0).getNome().toString();
                    select.setString(2, listacad.get(0).getNome().toString().toUpperCase());
                    select.setString(1, listacad.get(0).getTelefone().toString());
                    select.execute();
                }else{

                    select = null;
                    select = conec.getConnection().prepareStatement(" update usuario set nome = ? where telefone = " +
                            ""+listacad.get(0).getTelefone().toString());
                    select.setString(1,listacad.get(0).getNome().toString());
                    select.execute();
                }
                if (rs != null)
                    rs.close();

                if (select != null)
                    select.close();


                }catch (Exception e) {

                e.printStackTrace();
                System.err.println("AsyncTask erro: " + e.getMessage());
                result = "erro";
            }


            conec.fecharConexoes();
            return result;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();

            switch (aVoid) {
                case "erro":
                    Toast.makeText(Novousuario_fdb.this,"Erro ao enviar dados ao servidor, tente novamente",Toast.LENGTH_LONG).show();
                    break;

                case "1":
                    //aVoid = "Produto lançado com sucesso";
                    Toast.makeText(Novousuario_fdb.this,"Dados enviados ao servidor",Toast.LENGTH_LONG).show();

                    break;

                case "ok":
                    //aVoid = "Produto lançado com sucesso";
                    Toast.makeText(Novousuario_fdb.this,"Dados enviados ao servidor",Toast.LENGTH_LONG).show();

                    break;
                    }

            finish();
        }
    }




    public class Insereocorrencia extends AsyncTask<Cadastro_local, Void, String> {
        ConexaoFDB conec = new ConexaoFDB();
        private PreparedStatement select = null;
        private ResultSet rs = null;
        private String result = "";
        ProgressDialog progress = new ProgressDialog(Novousuario_fdb.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.novousuariofdb);

            progress.setMessage("Salvando dados no servidor...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

        }

        @Override
        protected String doInBackground(Cadastro_local... params) {

            result = conec.conectarFDBString(Novousuario_fdb.this);

            if (result.equals("erro")) {
                conec.fecharConexoes();
                return result;
            }
            try{
                //verifica se ja abertura de mesa ja
                sqlite = new Sqlite(Novousuario_fdb.this);
                listacad = sqlite.pegacadastro();
                if (listacad.size()==0){

                    Toast.makeText(Novousuario_fdb.this,"Cadastre-se para usar o App",Toast.LENGTH_LONG).show();
                    finish();
                }
                if (listacad.isEmpty()){

                    Toast.makeText(Novousuario_fdb.this,"Cadastre-se para usar o App",Toast.LENGTH_LONG).show();
                    finish();
                }
                select = null;
                long contausuario = 0;
                select = conec.getConnection().prepareStatement("select count(*) from usuario where telefone = " +
                        ""+listacad.get(0).getTelefone().toString());
                rs = select.executeQuery();
                rs.next();
                contausuario = rs.getLong(1);
                if(contausuario > 0) {

                    select = null;
                    String abremesa = " EXECUTE PROCEDURE NOVAOCORRENCIA( ?,?,?,?,?,?,?) ";
                    select = conec.getConnection().prepareStatement(abremesa);
                    //nome, telefone, latitude,longitude,codservico,ocorrencia,telefone_origem,codigo
                    String nome = listacad.get(0).getNome().toString();
                    select.setBytes(2, listacad.get(0).getNome().getBytes("Windows-1252"));
                    select.setString(1, listacad.get(0).getTelefone().toString());
                    select.setString(4, String.valueOf(latitude) );
                    select.setString(5, String.valueOf(longitude) );
                    select.setLong(6, codservico );
                    select.setBytes(3, msg.getBytes("Windows-1252"));
                    select.setString(7, mPhoneNumber);
                    select.execute();
                }else{

                    result = "erro";
                }
                if (rs != null)
                    rs.close();

                if (select != null)
                    select.close();


            }catch (Exception e) {

                e.printStackTrace();
                System.err.println("AsyncTask erro: " + e.getMessage());
                result = "erro";
            }


            conec.fecharConexoes();
            return result;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();

            switch (aVoid) {
                case "erro":
                    Toast.makeText(Novousuario_fdb.this,"Erro ao enviar dados ao servidor, tente novamente",Toast.LENGTH_LONG).show();
                    break;

                case "1":
                    //aVoid = "Produto lançado com sucesso";
                    Toast.makeText(Novousuario_fdb.this,"Dados enviados ao servidor",Toast.LENGTH_LONG).show();

                    break;

                case "ok":
                    //aVoid = "Produto lançado com sucesso";
                    Toast.makeText(Novousuario_fdb.this,"Dados enviados ao servidor",Toast.LENGTH_LONG).show();

                    break;
            }

            finish();
        }
    }
}
