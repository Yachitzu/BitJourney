package com.example.agenda_t;

import android.os.Bundle;
import android.os.Handler;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class BienvenidadActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent ventanaLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(ventanaLogin);
                finish();
            }
        }, 3000);
    }
}

