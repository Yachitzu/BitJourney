package com.example.agenda_t;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button btn_Login;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getDisplayName();
            // Si hay una sesión iniciada, redirige directamente a MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);
        btn_Login = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String EmailUser = email.getText().toString().trim();
                String PasswordUser = password.getText().toString().trim();
                if (EmailUser.isEmpty() || PasswordUser.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Ingrese los Datos", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(EmailUser, PasswordUser);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                       guardarEstadoSesionFirebase(true);
                        loadUserProfile();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error de inicio de sesión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void guardarEstadoSesionFirebase(boolean estado) {
        // Guardar el estado de inicio de sesión en Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            databaseReference.child("usuarios").child(currentUser.getUid()).child("sesion_iniciada").setValue(estado);
        }
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final FirebaseUser finalUser = user;
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String username = finalUser.getDisplayName();
                    Log.d("LoginActivity", "Username: " + username); // Agrega este log
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Error al cargar el perfil del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d("LoginActivity", "User is null");
        }
    }

    public void abrirRegistro(View Vista) {
        Intent pantallaRegistro = new Intent(getApplicationContext(), RegistroActivity.class);
        startActivity(pantallaRegistro);
    }

    public void abrirRecuperacion(View Vista) {
        try {
            Intent pantallaRecuperacion = new Intent(getApplicationContext(), RecupActivity.class);
            startActivity(pantallaRecuperacion);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de recuperación", Toast.LENGTH_SHORT).show();
        }
    }
}
