package com.appemergencias.carlos.app_emergencias;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.tonicsystems.jarjar.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity implements AIListener {

    private TextView mTextMessage;
    LinearLayout lay1,lay2, lay3;
    Button btnpolice, btnbomb,btncad;
    Sqlite sqlite;

    TextView txt;
    ImageButton btnfalar;
    private AIService aiService;
    private static final int RECORD_AUDIO_PERMISSION = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    EditText edtchat;
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2, REQUEST_CAMERA_PERMISSION = 0x3;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    lay1.setVisibility(View.VISIBLE);
                    lay2.setVisibility(View.INVISIBLE);
                    lay3.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    lay1.setVisibility(View.INVISIBLE);
                    lay2.setVisibility(View.VISIBLE);
                    lay3.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    lay1.setVisibility(View.INVISIBLE);
                    lay2.setVisibility(View.INVISIBLE);
                    lay3.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setando as configurações
        final AIConfiguration config = new AIConfiguration("817f9e9d57674ffa976d8e2f5263f279",
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        TTS.init(getApplicationContext());

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        lay1 = (LinearLayout)findViewById(R.id.lay1);
        lay2 = (LinearLayout)findViewById(R.id.lay2);
        lay3 = (LinearLayout)findViewById(R.id.lay3);

        btnbomb = (Button)findViewById(R.id.btnbombeiro);
        btnpolice = (Button)findViewById(R.id.btnpolice);
        btncad = (Button)findViewById(R.id.btncadastro);
        btnfalar = (ImageButton) findViewById(R.id.imgbtnfala);
        edtchat = (EditText)findViewById(R.id.edtchat2);
        sqlite = new Sqlite(MainActivity.this);

        checkpermissao();

        btncad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent a = new Intent(MainActivity.this,Cadastro.class);
                startActivity(a);
            }
        });

        btnpolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent a = new Intent(MainActivity.this,Ocorrencia.class);
                a.putExtra("em","police");
                startActivity(a);
            }
        });


        btnbomb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent a = new Intent(MainActivity.this,Ocorrencia.class);
                a.putExtra("em","bombeiro");
                startActivity(a);
            }
        });


        btnfalar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ouvir();
            }
        });


    }

    public void checkpermissao(){

        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        int permissiontelefone = ContextCompat.checkSelfPermission(MainActivity.this,
                READ_PHONE_STATE);
        List<String> listPermissionfone = new ArrayList<>();
        if (permissiontelefone != PackageManager.PERMISSION_GRANTED) {
            listPermissionfone.add(READ_PHONE_STATE);
            if (!listPermissionfone.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionfone.toArray(new String[listPermissionfone.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        } else {}

        int permissionLocation = ContextCompat.checkSelfPermission(MainActivity.this,
                ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        } else {


        }
    }

    ///começa o dialogo
    public void ouvir() {

        //Obtendo a permissão para leitura do áudio
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION);
        } else {
            aiService.startListening();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_PERMISSION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                aiService.startListening();

            } else {
                txt.setText("Permissão não obtida");
            }
        }

    }

    @Override
    public void onResult(AIResponse result) {

        Result r = result.getResult();

        // Obtendo os parâmetros compreendidos pelo Dialogflow
        String parameterString = "";
        if (r.getParameters() != null && !r.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : r.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        // Exibindo os parâmetros e a resposta para o áudio enviado
        String txt =("Eu: " + r.getResolvedQuery() +
                //"\nSafeBoot: " + r.getAction() +
                //"\nParameters: " + parameterString+
                "\nAssistente: " + r.getFulfillment().getSpeech());

        edtchat.setText(edtchat.getText().toString()+"\n"+txt.toString());
        final String speech = r.getFulfillment().getSpeech();
        //Log.i(TAG, "Speech: " + speech);
        if ( r.getFulfillment().getSpeech().contains("polícia")){

            Intent a = new Intent(MainActivity.this,Ocorrencia.class);
            a.putExtra("em","police");
            a.putExtra("chat",edtchat.getText().toString());
            startActivity(a);
        }

        if ( r.getFulfillment().getSpeech().contains("bombeiro")){

            Intent a = new Intent(MainActivity.this,Ocorrencia.class);
            a.putExtra("em","bombeiro");
            a.putExtra("chat",edtchat.getText().toString());
            startActivity(a);
        }
        TTS.speak(speech);

    }

    @Override
    public void onError(AIError error) {
        Toast.makeText(MainActivity.this,"erro",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

        Toast.makeText(MainActivity.this,"Fale Agora",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onListeningCanceled() {

        Toast.makeText(MainActivity.this,"Fale cancelada",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onListeningFinished() {

        Toast.makeText(MainActivity.this,"Fim de fala",Toast.LENGTH_LONG).show();
    }


}
