package com.example.aula5_listadetarefas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Objects;
import com.example.aula5_listadetarefas.MainActivity;

public class MainActivity2 extends AppCompatActivity {

    String id;
    String tarefa;
    String dataHora;
    EditText editText;
    TextView receberDataHora;
    Intent intent;
    private TextView campoDataHora;
    private SQLiteDatabase bancoDeDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Inicializar componentes da interface

        editText = findViewById(R.id.editTextTarefa);
        receberDataHora = findViewById(R.id.receberDataHora);

//        criarBancoDeDados();
        intent = getIntent();
        id = intent.getStringExtra("id");
        carregarTarefa();
    }




    private void carregarBancoDeDados() {
        bancoDeDados = openOrCreateDatabase("bancoDeDados", MODE_PRIVATE, null);
        bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS tarefas (id INTEGER PRIMARY KEY AUTOINCREMENT, tarefa TEXT, dataHora TEXT)");

    }

    public void carregarTarefa() {
        try {

            tarefa = intent.getStringExtra("tarefa");
            dataHora = intent.getStringExtra("dataHora");



            editText.setText(tarefa);
            receberDataHora.setText(dataHora);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    public void _pegarDataHoraAtt(View view) {
        Calendar calendario = Calendar.getInstance();
        int dia = calendario.get(Calendar.DAY_OF_MONTH);
        int mes = calendario.get(Calendar.MONTH);
        int ano = calendario.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view1, year, month, dayOfMonth) -> {
                    //Atualiza o textview com a data selecionada
                    String dataSelecionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    campoDataHora.setText(dataSelecionada);
                    //Após escolher a data, chama o método para escolher a hora
                    _pegarHora(null);
                }, ano, mes, dia);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void _pegarHora(View view) {
        Calendar calendario = Calendar.getInstance();
        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minuto = calendario.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view12, hourOfDay, minute) -> {

                    //Formatar Hora
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    //Concatena a data que ja existe com a hora
                    campoDataHora.setText(campoDataHora.getText().toString() + " " + time);

                }, hora, minuto, true);
        timePickerDialog.show();
    }




    private void atualizarTarefa(String tarefaAtt  , String dataHoraAtt) {

        try {
            //Usando o SQLiteStatement para segurança e eficiência
            String sql = "UPDATE tarefas SET tarefa = ?, dataHora = ?, status = ? WHERE id = ?";
            SQLiteStatement stmt = bancoDeDados.compileStatement(sql);
//            stmt.bindString(1, Tarefa);
//            stmt.bindString(2, DataHoraAtt);
            stmt.bindString(3, "1");
            stmt.bindLong(4, Long.parseLong(id));
            stmt.executeUpdateDelete();

//            editTextTarefa.setText("");
            campoDataHora.setText("Data e Hora");
            Toast.makeText(this, "Tarefa adicionada com sucesso!",
                    Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void _excluirTarefa(int id) {
        try {
            bancoDeDados.execSQL("DELETE FROM tarefas WHERE id = " + id);
            Toast.makeText(this, "Tarefa excluída com sucesso!",
                    Toast.LENGTH_SHORT).show();
                    MainActivity.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}