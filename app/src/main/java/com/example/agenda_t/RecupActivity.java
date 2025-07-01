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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class RecupActivity extends AppCompatActivity {
    private static final String TAG = "RecupActivity";

    private EditText editTextEmail;
    private Button buttonEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        editTextEmail = findViewById(R.id.txtRecupEmail);
        buttonEnviar = findViewById(R.id.BtnRecupPass);

        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarYEnviarSolicitudRestablecimientoContraseña();
            }
        });
    }

    private void verificarYEnviarSolicitudRestablecimientoContraseña() {
        final String email = editTextEmail.getText().toString().trim();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "dummy_password")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RecupActivity.this, "Correo no registrado", Toast.LENGTH_SHORT).show();
                            eliminarUsuarioTemporal();
                        } else {
                            handleCreateUserError(task.getException(), email);
                        }
                    }
                });
    }


    private void handleCreateUserError(Exception exception, final String email) {
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(errorCode)) {
                enviarSolicitudRestablecimientoContraseña(email);
            } else {
                Toast.makeText(RecupActivity.this, "Error al intentar verificar el correo", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al intentar verificar el correo", exception);
            }
        } else {
            Toast.makeText(RecupActivity.this, "Error al intentar verificar el correo", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al intentar verificar el correo", exception);
        }
    }

    private void enviarSolicitudRestablecimientoContraseña(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Se ha enviado un correo electrónico para restablecer la contraseña");
                            Toast.makeText(RecupActivity.this, "Se ha enviado un correo electrónico para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                            finish(); // Cierra la actividad actual
                        } else {
                            Log.e(TAG, "Error al enviar la solicitud de restablecimiento de contraseña", task.getException());
                            Toast.makeText(RecupActivity.this, "Error al enviar la solicitud de restablecimiento de contraseña", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void regresar(View Vista) {
        try {
            Intent pantallaLogin = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(pantallaLogin);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error al abrir la pantalla de recuperación", e);
            Toast.makeText(this, "Error al abrir la pantalla de recuperación", Toast.LENGTH_SHORT).show();
        }
    }
    private void eliminarUsuarioTemporal() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Usuario temporal eliminado con éxito.");
                            } else {
                                Log.e(TAG, "Error al intentar eliminar el usuario temporal", task.getException());
                                // Manejar el error
                            }
                        }
                    });
        }
    }
    }