package com.example.agenda_t;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderIntentService extends IntentService {

    private static final String CHANNEL_ID = "MyChannel";

    public ReminderIntentService() {
        super("ReminderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String reminderName = intent.getStringExtra("NAME");
            int radioButtonId = intent.getIntExtra("RADIO_BUTTON_ID", -1);

            Log.d("Reminder", "Recordatorio activado: " + reminderName + " Frecuencia: " + getFrequencyFromRadioButtonId(radioButtonId));

            // Muestra una notificación
            showNotification(reminderName);
        } catch (Exception e) {
            handleException("Error en el servicio", e);
        }
    }

    private void showNotification(String reminderName) {
        try {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager);
            }

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Recordatorio")
                            .setContentText("Es hora para: " + reminderName)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

            notificationManager.notify(1, builder.build());
        } catch (Exception e) {
            handleException("Error al mostrar la notificación", e);
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "MyChannel",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);
    }

    private void handleException(String message, Exception e) {
        e.printStackTrace();
        Log.e("Reminder", message + ": " + e.getMessage());
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
}
