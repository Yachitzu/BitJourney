package com.example.agenda_t;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistroActivity extends AppCompatActivity {
    EditText username, email, password, confirmPassword;
    Button btn_Registro;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        username = findViewById(R.id.txtUsername);
        email = findViewById(R.id.txtEmailRegistro);
        password = findViewById(R.id.txtPasswordRegistro);
        confirmPassword = findViewById(R.id.txtConfirmPassword);
        btn_Registro = findViewById(R.id.btnRegistro);
        mAuth = FirebaseAuth.getInstance();

        btn_Registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameText = username.getText().toString().trim();
                String emailText = email.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                String confirmPasswordText = confirmPassword.getText().toString().trim();

                if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
                } else if (!passwordText.equals(confirmPasswordText)) {
                    Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(usernameText, emailText, passwordText);
                }
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d("RegistroActivity", "Perfil actualizado con éxito. Username: " + username);
                                        crearEntradaAsociada(user.getUid(), username);
                                        Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegistroActivity.this, "Error al actualizar el perfil del usuario", Toast.LENGTH_SHORT).show();
                                        Log.e("RegistroActivity", "Error al actualizar el perfil del usuario: " + task1.getException());
                                    }
                                });
                    } else {
                        Toast.makeText(RegistroActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("RegistroActivity", "Error de registro: " + e.getMessage());
                        }
                    }
                });
    }

    private void crearEntradaAsociada(String userId, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference entradasRef = databaseReference.child("entradas").child(userId);
        String nuevaEntradaId = entradasRef.push().getKey();
        Log.d("RegistroActivity", "Nueva entrada creada con ID: " + nuevaEntradaId);
    }

    public void volverLogin(View view) {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void abrirLog(View Vista) {
        try {
            Intent pantallaLog = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(pantallaLog);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de login", Toast.LENGTH_SHORT).show();
        }
    }
}