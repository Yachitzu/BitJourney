package com.example.agenda_t;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UtilidadesFecha {

    public static String obtenerFechaActual() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}
