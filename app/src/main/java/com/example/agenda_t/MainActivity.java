package com.example.agenda_t;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        TextView textViewUsername = findViewById(R.id.textViewUsername);
        textViewUsername.setText( username );
    }
    public void abrirRegistroAgenda(View Vista) {
        Intent pantallaRegistroAgenda = new Intent(getApplicationContext(), EntradaActivity.class);
        pantallaRegistroAgenda.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
        startActivity(pantallaRegistroAgenda);
    }
    public void abrirLista(View Vista) {
        try {
            Intent pantallaLista = new Intent(getApplicationContext(), ListaActivity.class);
            pantallaLista.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            startActivity(pantallaLista);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de las listas", Toast.LENGTH_SHORT).show();
        }
    }
    public void abrirRecordatorio(View Vista) {
        try {
            Intent pantallaRecordatorio = new Intent(getApplicationContext(), RecordatorioActivity.class);
            pantallaRecordatorio.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            startActivity(pantallaRecordatorio);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de Recordatorio", Toast.LENGTH_SHORT).show();
        }
    }

    public void cerrarSesion(){
        mAuth.signOut();

        // Redirigir a la página de inicio de sesión (LoginActivity)
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Limpiar la pila de actividades
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }

    public void mostrarDialogoConfirmacion(View Vista) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar Sesión");
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cerrarSesion();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // No hacer nada si el usuario elige no cerrar sesión
            }
        });
        builder.show();
    }


}
