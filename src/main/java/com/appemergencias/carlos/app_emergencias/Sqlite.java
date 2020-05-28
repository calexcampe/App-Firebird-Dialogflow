package com.appemergencias.carlos.app_emergencias;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Sqlite extends SQLiteOpenHelper {

    private static final String NOME_DATABASE = "baseappemercia.db";
    private static final int VERSAO_DATABASE = 5;

    public Sqlite(Context context){

        super(context,NOME_DATABASE,null,VERSAO_DATABASE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(Tabela_Cadastro);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL(Tabela_Cadastro);

    }

    private static final String Tabela_Cadastro = "CREATE TABLE IF NOT EXISTS CADASTRO(" +
            " NOME TEXT," +
            " TELEFONE TEXT," +
            " CPF TEXT)";


    public void insertcadastro(String nome,String telefone, String cpf){

        SQLiteDatabase db = getWritableDatabase();
        String comando = "insert into cadastro(nome,telefone,cpf)" +
                " values('"+nome+"','"+telefone+"','"+cpf+"')";
        db.execSQL(comando);
    }

    public void atualizacadastro(String nome,String telefone, String cpf){

        SQLiteDatabase db = getWritableDatabase();
        String comando = "update cadastro set nome ="+"'"+nome+"',"+"telefone = "+"'"+telefone+"'"+","+"cpf = "+"'"+telefone+"'";
        db.execSQL(comando);
    }

    public Boolean verificacadastro(){

        SQLiteDatabase db = getReadableDatabase();
        String comando = "select * from CADASTRO";
        Cursor cursor = db.rawQuery(comando,null);
        if (cursor.getCount() > 0){

            return  true;
        }else {
            return false;
        }
    }

    public List<Cadastro_local> pegacadastro(){

        List<Cadastro_local> listacad = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String comando = "select nome,telefone,cpf from cadastro";
        Cursor cursor = db.rawQuery(comando,null);

        if (cursor.moveToFirst()){

            do{
                Cadastro_local cadastro_local = new Cadastro_local();
                cadastro_local.setCpf(cursor.getString(2));
                cadastro_local.setNome(cursor.getString(0));
                cadastro_local.setTelefone(cursor.getString(1));
                listacad.add(cadastro_local);
            }while(cursor.moveToNext());

        }
        cursor.close();
        return listacad;
    }

}
