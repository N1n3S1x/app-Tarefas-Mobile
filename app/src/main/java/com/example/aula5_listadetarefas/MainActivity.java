package com.example.aula5_listadetarefas;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText editTextTarefa;
    private Button botaoInserir;
    private ListView minhaListView;
    private ArrayList<String> itens;
    private ArrayList<String> datasHoras;
    private ArrayList<Integer> ids;
    private File arquivoTexto;
    private ArrayList<String> statusTarefas;
    private ArrayAdapter<String> adaptador;
    private TextView campoDataHora;
    private SQLiteDatabase bancoDeDados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializa os componentes
        editTextTarefa = findViewById(R.id.editTextTarefa);
        botaoInserir = findViewById(R.id.botaoInserir);
        minhaListView = findViewById(R.id.minhaListView);
        campoDataHora = findViewById(R.id.textviewDataHora);

        criarBancoDeDados();

        botaoInserir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novaTarefa = editTextTarefa.getText().toString().trim();
                String dataHora = campoDataHora.getText().toString();

                if (novaTarefa.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Digite uma tarefa!",
                            Toast.LENGTH_SHORT).show();
                } else if (dataHora.equals("Data e Hora")) {
                    Toast.makeText(MainActivity.this, "Selecione a data e hora!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //se ambos os campos estão devidamente preenchidos, chama o método para adicionar
                    adicionarTarefa(novaTarefa, dataHora);
                }
            }
        });

        minhaListView.setOnItemClickListener(((parent, view, position, id) -> {
            alternarStatusTarefa(position);
        }));

        minhaListView.setOnItemLongClickListener(((parent, view, position, id) -> {
            editarTarefa(position);
            return true;
        }));

        carregarTarefas();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) { // ✅ Verifica se veio da MainActivity2
            int id = data.getIntExtra("id", 0);
            String tarefaAtualizada = data.getStringExtra("tarefa");
            String dataHoraAtualizada = data.getStringExtra("dataHora");

            // Atualiza os dados no banco ou na interface
            carregarTarefas();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        carregarTarefas(); // Recarrega a lista sempre que voltar para MainActivity
    }

    public void criarBancoDeDados() {
        try {
            bancoDeDados = openOrCreateDatabase("ListaTarefasApp", MODE_PRIVATE, null);
            bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS " +
                    "tarefas (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tarefa VARCHAR, dataHora VARCHAR, status VARCHAR)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adicionarTarefa(String tarefa, String dataHora) {
        try {
            //Usando o SQLiteStatement para segurança e eficiência
            String sql = "INSERT INTO tarefas (tarefa, dataHora, status) VALUES (?, ?, ?)";
            SQLiteStatement stmt = bancoDeDados.compileStatement(sql);
            stmt.bindString(1, tarefa);
            stmt.bindString(2, dataHora);
            stmt.bindString(3, "1"); //status de ativo
//            stmt.executeInsert();

            editTextTarefa.setText("");
            campoDataHora.setText("Data e Hora");

            long id = stmt.executeInsert(); // Pega o ID gerado


            // Exibe o ID gerado pelo SQLite
            Toast.makeText(this, "Tarefa inserida com ID: " + id, Toast.LENGTH_SHORT).show();

            carregarTarefas();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void carregarTarefas() {
        try {
            Cursor cursor = bancoDeDados.rawQuery("SELECT * FROM tarefas " +
                    "ORDER BY id DESC", null);
            //indíces das colunas
            int indiceId = cursor.getColumnIndex("id");
            int indiceTarefa = cursor.getColumnIndex("tarefa");
            int indiceDataHora = cursor.getColumnIndex("dataHora");
            int indiceStatus = cursor.getColumnIndex("status");

            //inicializar as listas
            ids = new ArrayList<>();
            itens = new ArrayList<>();
            datasHoras = new ArrayList<>();
            statusTarefas = new ArrayList<>();

            //adaptador usando o layout customizado
            adaptador = new ArrayAdapter<String>(this, R.layout.linhacustomizada,
                    R.id.texto1, itens) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView texto1 = view.findViewById(R.id.texto1);
                    TextView texto2 = view.findViewById(R.id.texto2);

                    // Define o valor do TextView de data/hora com base na posição -- retornando do BD
                    texto1.setText(itens.get(position));
                    texto2.setText(datasHoras.get(position));

                    // Define o estilo do texto com base no status da tarefa
                    if (statusTarefas.get(position).equals("1")) {
                        texto1.setTextColor(Color.BLACK);
                        texto2.setTextColor(Color.BLUE);
                        texto1.setPaintFlags(texto1.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        texto2.setPaintFlags(texto2.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    } else {
                        texto1.setTextColor(Color.GRAY);
                        texto2.setTextColor(Color.GRAY);
                        texto1.setPaintFlags(texto1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        texto2.setPaintFlags(texto2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }

                    return view;
                }
            };
            minhaListView.setAdapter(adaptador);

            //itera sober o cursor e adiciona às listas
            cursor.moveToFirst();
            while (cursor != null) {
                ids.add(cursor.getInt(indiceId));
                itens.add(cursor.getString(indiceTarefa));
                datasHoras.add(cursor.getString(indiceDataHora));
                statusTarefas.add(cursor.getString(indiceStatus));
                cursor.moveToNext();
            }
            cursor.close(); //liberando memória, tornando mais eficiente

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void excluirTarefa(int position) {
        try {
            bancoDeDados.execSQL("DELETE FROM tarefas WHERE id = " + ids.get(position));
            Toast.makeText(MainActivity.this, "Tarefa excluída com sucesso!",
                    Toast.LENGTH_SHORT).show();
            carregarTarefas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mudarTelaEdicao(int position) {

        int id = ids.get(position);

        try {

            Toast.makeText(MainActivity.this, "Editar tarefa!",
                    Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("id", id);
            intent.putExtra("tarefa", itens.get(position));
            intent.putExtra("dataHora", datasHoras.get(position));

            startActivityForResult(intent, 1);


        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void editarTarefa(int position) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Editar Tarefa")
                .setMessage("Deseja editar a tarefa \"" + itens.get(position) + "\"?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    mudarTelaEdicao(position);
                })
                .setNegativeButton("Não", null)
                .setNeutralButton("Compartilhar", (dialogInterface, i) -> {
                    compartilharTarefa(position);
                })

                .show();
    }

    private void alternarStatusTarefa(int position) {
        try {
            String novoStatus = statusTarefas.get(position).equals("1") ? "0" : "1";
            bancoDeDados.execSQL("UPDATE tarefas SET status = '" +
                    novoStatus + "' WHERE id = " + ids.get(position));
            carregarTarefas();
            if (novoStatus.equals("1")) {
                Toast.makeText(MainActivity.this, "Tarefa ativa!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Tarefa concluída!",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void _pegarDataHora(View view) {
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

    private void compartilharTarefa(int position) {

        gerarArquivoTexto(position);

        // Verificar se o arquivo foi criado
        if (arquivoTexto.exists()) {
            // Usar FileProvider para obter o URI do arquivo
            Uri arquivoUri = FileProvider.getUriForFile(MainActivity.this,
                    "com.example.aula5_listadetarefas.provider", arquivoTexto);


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");  // Tipo de arquivo a ser compartilhado
            intent.putExtra(Intent.EXTRA_STREAM, arquivoUri);  // Usar o URI do arquivo

            // Conceder permissão para a aplicação que receber o arquivo
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            startActivity(Intent.createChooser(intent, "Compartilhar tarefa via"));
        } else {
            Toast.makeText(MainActivity.this, "Erro ao gerar o arquivo para compartilhar!", Toast.LENGTH_SHORT).show();
        }
    }


    private void gerarArquivoTexto(int position) {

        String tarefa = itens.get(position);
        String dataHora = datasHoras.get(position);

        String conteudo = "Tarefa: " + tarefa + "\n" +
                "Data e Hora: " + dataHora;

        try {
            // Cria o diretório para armazenar o arquivo, se necessário
            File diretorio = getExternalFilesDir(null);  // Usando o diretório apropriado para armazenar arquivos
            arquivoTexto = new File(diretorio, "tarefa_" + ids.get(position) + ".txt");

            // Escrever o conteúdo no arquivo
            FileWriter escritor = new FileWriter(arquivoTexto);
            escritor.write(conteudo);
            escritor.close();


            Toast.makeText(this, "Arquivo gerado com sucesso: " + arquivoTexto.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar o arquivo", Toast.LENGTH_SHORT).show();
        }
    }


}
