package com.example.aula5_listadetarefas;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Objects;
import com.example.aula5_listadetarefas.MainActivity.*;

public class MainActivity2 extends AppCompatActivity {








    Integer id;
    String tarefa;
    String dataHora;
    EditText editText;
    TextView receberDataHora;

    Intent intent;
    private SQLiteDatabase bancoDeDados;
    private DatabaseHelper dbHelper;
    Button btnAtualizar;





    Button btnExcluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Inicializar componentes da interface



        editText = findViewById(R.id.editTextTarefa);
        receberDataHora = findViewById(R.id.receberDataHora);
        btnExcluir = findViewById(R.id.btnExcluir);
        btnAtualizar = findViewById(R.id.btnAtualizar);


        intent = getIntent();

        id = intent.getIntExtra("id",0);

        carregarBancoDeDados();
        carregarTarefa();

        btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novaTarefa = editText.getText().toString().trim();
                String dataHora = receberDataHora.getText().toString();

                if (novaTarefa.isEmpty() || dataHora.isEmpty()) {
                    Toast.makeText(MainActivity2.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                id = intent.getIntExtra("id",0);

                atualizarTarefa(novaTarefa, dataHora, id);

                int id = getIntent().getIntExtra("id", 0);

                Intent resultadoIntent = new Intent();
                resultadoIntent.putExtra("id", id);
                resultadoIntent.putExtra("tarefa", novaTarefa);
                resultadoIntent.putExtra("dataHora", dataHora);

                setResult(RESULT_OK, resultadoIntent); // Enviando os dados de volta
                finish();
            }
        });

        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultadoIntent = new Intent();
                confirmarExclusao(id);
            }
        });
    }

    private void carregarBancoDeDados() {
        dbHelper = new DatabaseHelper(this);
        bancoDeDados = dbHelper.getWritableDatabase(); // Abre o banco para leitura e escrita
    }

    public void carregarTarefa() {
        try {

            tarefa = intent.getStringExtra("tarefa");
            dataHora = intent.getStringExtra("dataHora");
            id = intent.getIntExtra("id",0);
            String texto = tarefa + id;

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
                    receberDataHora.setText(dataSelecionada);
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
                    receberDataHora.setText(receberDataHora.getText().toString() + " " + time);

                }, hora, minuto, true);
        timePickerDialog.show();
    }




    private void atualizarTarefa(String tarefaAtt, String dataHoraAtt, Integer id) {
        SQLiteStatement stmt = null;  // Declare o Statement fora do try para fechar no finally
        try {
            // Usando o SQLiteStatement para segurança e eficiência
            String sql = "UPDATE tarefas SET tarefa = ?, dataHora = ?, status = ? WHERE id = ?";
            stmt = bancoDeDados.compileStatement(sql);
            stmt.bindString(1, tarefaAtt);   // Vincula a tarefa
            stmt.bindString(2, dataHoraAtt); // Vincula a data e hora
            stmt.bindString(3, "1");         // Define o status (considere usar uma constante ou um valor mais significativo)
            stmt.bindLong(4, id); // Vincula o id

            stmt.executeUpdateDelete();  // Executa a atualização


            // Mensagem de sucesso
            Toast.makeText(this, "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            // Aqui você pode adicionar um Toast de erro se desejar
            Toast.makeText(this, "Erro ao atualizar a tarefa!", Toast.LENGTH_SHORT).show();
        } finally {
            if (stmt != null) {
                stmt.close(); // Certifica-se de fechar o Statement para evitar vazamento de memória
            }
        }
    }

    private void confirmarExclusao(final Integer id) {
        // Criar um AlertDialog para confirmação
        new AlertDialog.Builder(MainActivity2.this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Você tem certeza que deseja excluir esta tarefa?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent resultadoIntent = new Intent();
                        // Caso o usuário confirme, chama o método de exclusão
                        _excluirTarefa(id);
                        setResult(RESULT_OK,resultadoIntent); // Passa a resposta de sucesso para a atividade anterior
                        finish(); // Finaliza a atividade após a exclusão
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Se o usuário cancelar, não faz nada
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    private void _excluirTarefa(Integer id) {
        SQLiteStatement stmt = null;
        try {
            // Usando o SQLiteStatement para segurança e eficiência
            String sql = "DELETE FROM tarefas WHERE id = ?";
            stmt = bancoDeDados.compileStatement(sql);
            stmt.bindLong(1, id); // Vincula o id da tarefa a ser excluída

            int rowsDeleted = stmt.executeUpdateDelete(); // Executa a exclusão

            if (rowsDeleted > 0) {
                Toast.makeText(MainActivity2.this, "Tarefa excluída com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity2.this, "Erro ao excluir a tarefa.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity2.this, "Erro ao excluir a tarefa.", Toast.LENGTH_SHORT).show();
        } finally {
            if (stmt != null) {
                stmt.close(); // Fecha o Statement para evitar vazamento de memória
            }
        }
    }


}