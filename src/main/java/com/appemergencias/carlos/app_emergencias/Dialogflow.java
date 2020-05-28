package com.appemergencias.carlos.app_emergencias;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

//import com.google.cloud.dialogflow.V2beta1.*;

public class Dialogflow extends AppCompatActivity implements AIListener{


    TextView txt;
    Button btn;
    private AIService aiService;
    private static final int RECORD_AUDIO_PERMISSION = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    EditText  edtchat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogflow);


        txt = (TextView)findViewById(R.id.textView5);
        btn = (Button)findViewById(R.id.button15);
        edtchat = (EditText)findViewById(R.id.edtchat);
        edtchat.setText("");
        edtchat.setEnabled(false);



        //Setando as configurações
        final AIConfiguration config = new AIConfiguration("817f9e9d57674ffa976d8e2f5263f279",
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        TTS.init(getApplicationContext());
    }



    public void ouvir(View view) {

        //Obtendo a permissão para leitura do áudio
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
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
            txt.setText("Eu: " + r.getResolvedQuery() +
                    //"\nSafeBoot: " + r.getAction() +
                    //"\nParameters: " + parameterString+
                    "\nAssistente: " + r.getFulfillment().getSpeech());

            edtchat.setText(edtchat.getText().toString()+"\n"+txt.getText().toString());
            final String speech = r.getFulfillment().getSpeech();
            //Log.i(TAG, "Speech: " + speech);
            if ( r.getFulfillment().getSpeech().contains("policia")){

               Intent a = new Intent(Dialogflow.this,Ocorrencia.class);
               a.putExtra("em","police");
               startActivity(a);
            }

            if ( r.getFulfillment().getSpeech().contains("bombeiro")){

              Intent a = new Intent(Dialogflow.this,Ocorrencia.class);
              a.putExtra("em","bombeiro");
              startActivity(a);
            }

            TTS.speak(speech);

        }



    @Override
    public void onError(AIError error) {
        Toast.makeText(Dialogflow.this,"erro",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

        Toast.makeText(Dialogflow.this,"Finished",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onListeningCanceled() {

        Toast.makeText(Dialogflow.this,"Finished",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onListeningFinished() {

        Toast.makeText(Dialogflow.this,"Finished",Toast.LENGTH_LONG).show();
    }
}
