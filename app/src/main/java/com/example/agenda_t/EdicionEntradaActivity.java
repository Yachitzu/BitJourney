package com.example.agenda_t;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EdicionEntradaActivity extends AppCompatActivity {
    private EditText edtTitulo;
    private EditText edtDescripcion;
    private Button btnGuardar;
    private Button btnAdjuntarArchivoE;
    private ImageView imgAdjunto;

    private String entradaId;
    private String archivoAdjuntoUrl;
    private String urlInicialImagen;
    private Uri selectedImageUri; // Nueva variable

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnAdjuntarArchivoE = findViewById(R.id.btnAdjuntarArchivoE);
        imgAdjunto = findViewById(R.id.imgAdjunto);

        entradaId = getIntent().getStringExtra("entradaId");
        cargarDetallesEntrada(entradaId);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambiosEntrada();
            }
        });

        btnAdjuntarArchivoE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void cargarDetallesEntrada(String entradaId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entradas")
                    .child(currentUser.getUid())
                    .child(entradaId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Entrada entrada = dataSnapshot.getValue(Entrada.class);
                        if (entrada != null) {
                            edtTitulo.setText(entrada.getTitulo());
                            edtDescripcion.setText(entrada.getDescripcion());
                            urlInicialImagen = entrada.getArchivoAdjuntoUrl();
                            if (urlInicialImagen != null && !urlInicialImagen.isEmpty()) {
                                Picasso.get().load(urlInicialImagen).into(imgAdjunto);
                                imgAdjunto.setVisibility(View.VISIBLE);
                            } else {
                                imgAdjunto.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                    Toast.makeText(EdicionEntradaActivity.this, "Error al cargar detalles de la entrada", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void guardarCambiosEntrada() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entradas")
                    .child(currentUser.getUid())
                    .child(entradaId);

            String fechaIngreso = obtenerFechaActual();
            String titulo = edtTitulo.getText().toString().trim();
            String descripcion = edtDescripcion.getText().toString().trim();

            Entrada entradaEditada = new Entrada();
            entradaEditada.setKey(entradaId);
            entradaEditada.setFechaIngreso(fechaIngreso);
            entradaEditada.setTitulo(titulo);
            entradaEditada.setDescripcion(descripcion);

            if (selectedImageUri != null && !TextUtils.isEmpty(selectedImageUri.toString())) {
                subirImagenAFirebaseStorage(selectedImageUri.toString(), databaseReference, entradaEditada);
            } else if (!TextUtils.isEmpty(archivoAdjuntoUrl) && !archivoAdjuntoUrl.equals(urlInicialImagen)) {
                subirImagenAFirebaseStorage(archivoAdjuntoUrl, databaseReference, entradaEditada);
            } else {
                entradaEditada.setArchivoAdjuntoUrl(urlInicialImagen);
                actualizarEntradaEnFirebase(databaseReference, entradaEditada);
            }
        }
    }

    private String obtenerNombreArchivoDesdeUrl(String url) {
        int index = url.lastIndexOf("/");
        return (index != -1) ? url.substring(index + 1) : url;
    }

    private void subirImagenAFirebaseStorage(String archivoAdjuntoUrl, DatabaseReference databaseReference, Entrada entradaEditada) {
        Log.d("TAG", "Comenzando subida de imagen");

        if (archivoAdjuntoUrl != null && !archivoAdjuntoUrl.isEmpty()) {
            if (TextUtils.equals("content", Uri.parse(archivoAdjuntoUrl).getScheme())) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("archivos_adjuntos")
                        .child(obtenerNombreArchivoDesdeUrl(archivoAdjuntoUrl));
                Uri fileUri = Uri.parse(archivoAdjuntoUrl);
                UploadTask uploadTask = storageRef.putFile(fileUri);
                uploadTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            entradaEditada.setArchivoAdjuntoUrl(uri.toString());
                            actualizarEntradaEnFirebase(databaseReference, entradaEditada);
                        }).addOnFailureListener(e -> {
                            Log.e("TAG", "Error al obtener la URL de descarga: " + e.getMessage());
                            Toast.makeText(EdicionEntradaActivity.this, "Error al obtener la URL de descarga: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e("TAG", "Error al subir la imagen: " + task.getException().getMessage());
                        Toast.makeText(EdicionEntradaActivity.this, "Error al subir la imagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("TAG", "La URL del archivo adjunto no es válida");
                Toast.makeText(EdicionEntradaActivity.this, "La URL del archivo adjunto no es válida", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("TAG", "La URL del archivo adjunto es nula o vacía");
            Toast.makeText(EdicionEntradaActivity.this, "La URL del archivo adjunto es nula o vacía", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarEntradaEnFirebase(DatabaseReference databaseReference, Entrada entradaEditada) {
        // Actualizar solo la entrada en la base de datos
        databaseReference.setValue(entradaEditada)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TAG", "Cambios guardados exitosamente");
                    Toast.makeText(EdicionEntradaActivity.this, "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("TAG", "Error al guardar cambios: " + e.getMessage());
                    Toast.makeText(EdicionEntradaActivity.this, "Error al guardar cambios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    archivoAdjuntoUrl = selectedImageUri.toString();
                    Picasso.get().load(archivoAdjuntoUrl).into(imgAdjunto);
                    imgAdjunto.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("TAG", "Error al obtener la URI del archivo adjunto");
                    Toast.makeText(this, "Error al obtener la URI del archivo adjunto", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    public void abrirLista(View Vista) {
        try {
            Intent pantallaEntradas = new Intent(getApplicationContext(), ListaActivity.class);
            finish();
            startActivity(pantallaEntradas);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de las listas", Toast.LENGTH_SHORT).show();
        }
    }

    private String obtenerFechaActual() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}
