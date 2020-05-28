package com.appemergencias.carlos.app_emergencias;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

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


public class Ocorrencia extends AppCompatActivity implements AIListener {

    TextView txt;
    Button btnenvia,btnfala;
    MultiAutoCompleteTextView msgoco;
    long codservico;
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2, REQUEST_CAMERA_PERMISSION = 0x3;
    private AIService aiService;
    private static final int RECORD_AUDIO_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocorrencia);

        txt = (TextView)findViewById(R.id.txtocorrencia);
        btnenvia = (Button)findViewById(R.id.btnenviaocorrencia);
        btnfala = (Button)findViewById(R.id.btnfalarocorrencia);
        msgoco = (MultiAutoCompleteTextView)findViewById(R.id.edtmsgocorrencia);

        //Setando as configurações
        final AIConfiguration config = new AIConfiguration("817f9e9d57674ffa976d8e2f5263f279",
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        checkpermissao();

        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("em").equals("police")){

            txt.setText(txt.getText().toString()+":Policial");
            msgoco.setText(bundle.getString("chat"));
            codservico = 1;
        }
        if (bundle.getString("em").equals("bombeiro")){

            txt.setText(txt.getText().toString()+":Bombeiro");
            msgoco.setText(bundle.getString("chat"));
            codservico = 2;
        }

        btnenvia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (msgoco.getText().toString().equals("")){

                    Toast.makeText(Ocorrencia.this,"Escreva a msg da ocorrencia",Toast.LENGTH_LONG).show();
                    return;
                }
                Intent a = new Intent(Ocorrencia.this,Novousuario_fdb.class);
                a.putExtra("usuario","n");
                a.putExtra("codserv",codservico);
                a.putExtra("ocor",msgoco.getText().toString());
                startActivity(a);
            }
        });

        btnfala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ouvir();
            }
        });
    }



    public void checkpermissao(){


        int permissionLocation = ContextCompat.checkSelfPermission(Ocorrencia.this,
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
        String txt =("Eu: " + r.getResolvedQuery().toString());
                //"\nSafeBoot: " + r.getAction() +
                //"\nParameters: " + parameterString+
               // "\nAssistente: " + r.getFulfillment().getSpeech());

        msgoco.setText(msgoco.getText().toString()+"\n"+txt.toString());
        final String speech = r.getFulfillment().getSpeech();
        //Log.i(TAG, "Speech: " + speech);

       // TTS.speak(speech);

    }

    @Override
    public void onError(AIError error) {
        Toast.makeText(Ocorrencia.this,"erro",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

        Toast.makeText(Ocorrencia.this,"Fale Agora",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onListeningCanceled() {

        Toast.makeText(Ocorrencia.this,"Fale cancelada",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onListeningFinished() {

        Toast.makeText(Ocorrencia.this,"Fim de fala",Toast.LENGTH_LONG).show();
    }

}



