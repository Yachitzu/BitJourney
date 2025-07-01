package com.example.agenda_t;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class RecordatorioActivity extends AppCompatActivity {

    private RadioGroup radioGroupFrequency;
    private static final String CHANNEL_ID = "channel_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordatorio);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        final EditText editTextReminderName = findViewById(R.id.editTextReminderName);
        final TimePicker timePickerReminder = findViewById(R.id.timePickerReminder);
        Button buttonSetReminder = findViewById(R.id.buttonSetReminder);


        radioGroupFrequency = findViewById(R.id.radioGroupFrequency);

        // Configura el canal de notificación
        createNotificationChannel();

        buttonSetReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderName = editTextReminderName.getText().toString();
                int hour, minute;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hour = timePickerReminder.getHour();
                    minute = timePickerReminder.getMinute();
                } else {
                    hour = timePickerReminder.getCurrentHour();
                    minute = timePickerReminder.getCurrentMinute();
                }

                int checkedRadioButtonId = radioGroupFrequency.getCheckedRadioButtonId();
                if (reminderName.isEmpty()) {
                    showToast("Ingrese un título para el recordatorio");
                } else if (checkedRadioButtonId == -1) {
                    showToast("Seleccione una frecuencia para el recordatorio");
                } else {
                    // Ambos campos están llenos, proceder a establecer el recordatorio
                    showConfirmationDialog(reminderName, hour, minute, checkedRadioButtonId);
                }
            }
        });
    }

    private void showConfirmationDialog(final String reminderName, final int hour, final int minute, final int checkedRadioButtonId) {
        // Muestra aquí tu pantalla de confirmación (DialogFragment o AlertDialog)
        // Puedes usar un AlertDialog básico como ejemplo

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de establecer el recordatorio?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Usuario hizo clic en "Sí", establece el recordatorio
                    setReminder(reminderName, hour, minute, checkedRadioButtonId);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // Usuario hizo clic en "No", puedes realizar acciones adicionales si es necesario
                })
                .show();
    }

    private void setReminder(String name, int hour, int minute, int checkedRadioButtonId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, ReminderIntentService.class);

        intent.putExtra("NAME", name);
        intent.putExtra("RADIO_BUTTON_ID", checkedRadioButtonId);

        String frequency = getFrequencyFromRadioButtonId(checkedRadioButtonId);
        intent.putExtra("FREQUENCY", frequency);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        long interval;

        if (checkedRadioButtonId == R.id.radioDiario) {
            interval = AlarmManager.INTERVAL_DAY;
        } else if (checkedRadioButtonId == R.id.radioSemanal) {
            interval = AlarmManager.INTERVAL_DAY * 7;
        } else if (checkedRadioButtonId == R.id.radioMensual) {
            interval = AlarmManager.INTERVAL_DAY * 30;
        } else {
            interval = 0;
        }

        if (interval > 0) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent);
            showToast("Notificación establecida con éxito.");
            finish();
        } else {
            showToast("Frecuencia no válida");
        }
    }

    private String getFrequencyFromRadioButtonId(int radioButtonId) {
        if (radioButtonId == R.id.radioDiario) {
            return "Diario";
        } else if (radioButtonId == R.id.radioSemanal) {
            return "Semanal";
        } else if (radioButtonId == R.id.radioMensual) {
            return "Mensual";
        } else {
            return "";
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    public void RegresarPrincipal(View Vista) {
        try {
            Intent pantallaMain2 = new Intent(getApplicationContext(), MainActivity.class);
            pantallaMain2.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            finish();
            startActivity(pantallaMain2);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de recuperación", Toast.LENGTH_SHORT).show();
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);


        }


    }
}
