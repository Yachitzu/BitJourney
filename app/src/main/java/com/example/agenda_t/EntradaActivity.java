package com.example.agenda_t;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EntradaActivity extends AppCompatActivity {
    private EditText edtTitulo;
    private EditText edtDescripcion;
    private Button btnGuardar;
    private Button btnAdjuntarArchivo;
    private ImageView imgAdjunto;
    private Uri archivoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnAdjuntarArchivo = findViewById(R.id.btnAdjuntarArchivo);
        imgAdjunto = findViewById(R.id.imgAdjunto);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarEntrada();
            }
        });

        btnAdjuntarArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjuntarArchivo();
            }
        });
    }

    private String obtenerFechaActual() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private void guardarEntrada() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entradas").child(currentUser.getUid());
            String fechaIngreso = obtenerFechaActual();
            String titulo = edtTitulo.getText().toString().trim();
            String descripcion = edtDescripcion.getText().toString().trim();
            if (titulo.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Ingrese los datos para poder guardar la entrada", Toast.LENGTH_SHORT).show();
            } else {
                Entrada nuevaEntrada = new Entrada();
                nuevaEntrada.setFechaIngreso(fechaIngreso);
                nuevaEntrada.setTitulo(titulo);
                nuevaEntrada.setDescripcion(descripcion);

                if (archivoUri != null) {
                    subirArchivoAFirebaseStorage(archivoUri, nuevaEntrada.getKey(), databaseReference);
                    // Muestra la imagen seleccionada
                    imgAdjunto.setVisibility(View.VISIBLE);
                    imgAdjunto.setImageURI(archivoUri);
                } else {
                    guardarEntradaEnFirebase(nuevaEntrada, databaseReference);
                }
            }
        }
    }

    private void adjuntarArchivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Solo imágenes
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                archivoUri = data.getData();
                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show();
                // Muestra la imagen seleccionada
                imgAdjunto.setVisibility(View.VISIBLE);
                imgAdjunto.setImageURI(archivoUri);
            }
        }
    }

    private void subirArchivoAFirebaseStorage(Uri archivoUri, String entradaKey, DatabaseReference databaseReference) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("imagenes_adjuntas/" + entradaKey + "/" + System.currentTimeMillis());

        storageRef.putFile(archivoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Entrada nuevaEntrada = new Entrada();
                        nuevaEntrada.setFechaIngreso(obtenerFechaActual());
                        nuevaEntrada.setTitulo(edtTitulo.getText().toString().trim());
                        nuevaEntrada.setDescripcion(edtDescripcion.getText().toString().trim());
                        nuevaEntrada.setArchivoAdjuntoUrl(uri.toString());
                        nuevaEntrada.setKey(databaseReference.push().getKey());
                        databaseReference.child(nuevaEntrada.getKey()).setValue(nuevaEntrada);
                        Toast.makeText(this, "Entrada con imagen adjunta guardada exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir la imagen adjunta", Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarEntradaEnFirebase(Entrada nuevaEntrada, DatabaseReference databaseReference) {
        nuevaEntrada.setKey(databaseReference.push().getKey());
        databaseReference.child(nuevaEntrada.getKey()).setValue(nuevaEntrada);
        Toast.makeText(this, "Entrada guardada exitosamente", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void RegresarMain(View Vista) {
        try {
            Intent regresar = new Intent(getApplicationContext(), MainActivity.class);
            regresar.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            finish();
            startActivity(regresar);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de recuperación", Toast.LENGTH_SHORT).show();
        }
    }
}
