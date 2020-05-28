package com.appemergencias.carlos.app_emergencias;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Cadastro extends AppCompatActivity {

    Cadastro_local cadastro_local;
    List<Cadastro_local> list;
    EditText ednome, edtel,edcpf;
    Button btnsalvar;

    Sqlite sqlite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        edcpf = (EditText)findViewById(R.id.edtcpf);
        ednome = (EditText)findViewById(R.id.edtnome);
        edtel = (EditText)findViewById(R.id.edtfone);
        btnsalvar = (Button)findViewById(R.id.btn_salvacadastro);
        sqlite = new Sqlite(Cadastro.this);


       if(sqlite.verificacadastro()) {

           list = sqlite.pegacadastro();

           for (int i = 0; i < list.size(); i++) {

               ednome.setText(list.get(i).getNome().toString());
               edtel.setText(list.get(i).getTelefone().toString());
               edcpf.setText(list.get(i).getCpf().toString());
           }
       }

       btnsalvar.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               if(!sqlite.verificacadastro()) {
                   if (edcpf.getText().toString().equals("")) {

                       Toast.makeText(Cadastro.this, "Informe o numero de seu Documento", Toast.LENGTH_LONG).show();
                       return;
                   }
                   if (ednome.getText().toString().equals("")) {

                       Toast.makeText(Cadastro.this, "Informe seu Nome", Toast.LENGTH_LONG).show();
                       return;
                   }
                   if (edtel.getText().toString().equals("")) {

                       Toast.makeText(Cadastro.this, "Informe o numero de seu Telefone", Toast.LENGTH_LONG).show();
                       return;
                   }

                   sqlite.insertcadastro(ednome.getText().toString().toUpperCase(), edtel.getText().toString(), edcpf.getText().toString());
                   Toast.makeText(Cadastro.this, "Cadastro Salvo com Sucesso", Toast.LENGTH_LONG).show();

                   Intent a = new Intent(Cadastro.this,Novousuario_fdb.class);
                   a.putExtra("usuario","s");
                   startActivity(a);
                   finish();
               }else{

                   sqlite.atualizacadastro(ednome.getText().toString().toUpperCase(),edtel.getText().toString(),edcpf.getText().toString());
                   Toast.makeText(Cadastro.this,"Cadastro Atualizado",Toast.LENGTH_LONG).show();
                   Intent a = new Intent(Cadastro.this,Novousuario_fdb.class);
                   a.putExtra("usuario","s");
                   startActivity(a);
                   finish();

               }
           }
       });
    }
}
