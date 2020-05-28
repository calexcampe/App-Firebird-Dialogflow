package com.appemergencias.carlos.app_emergencias;

import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.PooledConnection;

/**
 * Classe para objetos do tipo ConexaoFDB, onde é contido os valores e métodos para o mesmo (JavaBeans).
 * A cada requisição é verificada as preferências caso haja um caminho personalizado.
 * <p>Utilizado para obter conexão com o servidor Firebird e autenticação.</p>
 *

 */
public class ConexaoFDB {

    private Connection connection = null;
    private PreparedStatement auth = null;
    private PreparedStatement verificatabela = null;
    SharedPreferences prefs;
    private String databaseURL = "144.217.210.44/3050:c:\\Dados\\APPEMERGENCNIA.FDB";
    private String user = "SYSDBA";
    private String password = "masterkey";
    private String queryAuth = "SELECT COD_USER, AUTH FROM MOBILE WHERE COD_USER = ?";
    private String qryscript = "SELECT COUNT(*) QTDE FROM RDB$RELATIONS " +
            "WHERE RDB$FLAGS=1 and RDB$RELATION_NAME = 'MOBILE' ";




    /**
     * Método para obter conexão com o banco de dados e retornar um valor String.
     *
     * @param context informação de qual Context (tela) necessária para obter o caminho do banco de dados
     *                (se houver alteração do caminho original)
     * @return        valor String "erro" ou "ok" caso consiga conexão
     *
     *
     *
     */

    /////////////////=========================================//////////////////////////



    public String conectarFDBString(Context context){
        String result= "erro";

        try{
            org.firebirdsql.ds.FBConnectionPoolDataSource pool =
                    new org.firebirdsql.ds.FBConnectionPoolDataSource();

            user = user.trim();
            password = password.trim();

            pool.setLoginTimeout(3);
            pool.setDatabase(databaseURL);
            pool.setUser(user);
            pool.setPassword(password);
            PooledConnection pooledCon = pool.getPooledConnection();
            this.connection = pooledCon.getConnection();
            result = "ok";
        }catch (Exception e){
            e.printStackTrace();
        }

        if(result.equals("erro")){
            try{
                org.firebirdsql.ds.FBConnectionPoolDataSource pool =
                        new org.firebirdsql.ds.FBConnectionPoolDataSource();
                pool.setLoginTimeout(5);
                pool.setDatabase(databaseURL);
                pool.setUser(user);
                pool.setPassword(password);
                PooledConnection pooledCon = pool.getPooledConnection();
                this.connection = pooledCon.getConnection();
                result = "ok";
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return result;
    }


    /**
     * Fecha conexões abertas com o banco Firebird.
     */
    public void fecharConexoes(){

        try {

            if (connection != null)
                connection.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public Connection getConnection() {
        return connection;
    }

    



}
