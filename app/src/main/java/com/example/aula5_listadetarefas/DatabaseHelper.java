package com.example.aula5_listadetarefas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ListaTarefasApp"; // Nome do banco
    private static final int DATABASE_VERSION = 1; // Versão do banco

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Criação da tabela caso o banco seja novo
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                "tarefas (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tarefa VARCHAR, dataHora VARCHAR, status VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Atualização do banco, se necessário
        db.execSQL("DROP TABLE IF EXISTS tarefas");
        onCreate(db);
    }
}
