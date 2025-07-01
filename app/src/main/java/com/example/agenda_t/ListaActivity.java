package com.example.agenda_t;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListaActivity extends AppCompatActivity implements EntradaAdapter.OnItemClickListener {

    private static final String TAG = "ListaActivity"; // Log tag

    private RecyclerView recyclerViewEntradas;
    private List<Entrada> listaEntradas;
    private EntradaAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        // Iniciar logs
        Log.d(TAG, "onCreate: Iniciando ListaActivity");

        recyclerViewEntradas = findViewById(R.id.recyclerViewEntradas);
        listaEntradas = new ArrayList<>();
        adapter = new EntradaAdapter(listaEntradas, this);
        recyclerViewEntradas.setAdapter(adapter);
        recyclerViewEntradas.setLayoutManager(new LinearLayoutManager(this));

        // Configurar SearchView
        searchView = findViewById(R.id.searchView2);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Verificar si el texto de búsqueda no está vacío
                if (newText != null && !newText.isEmpty()) {
                    // Aplicar el filtro solo si hay texto de búsqueda
                    Log.d(TAG, "onQueryTextChange: Filtrando con: " + newText);
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        // Filtrar inicialmente con una cadena vacía para mostrar todas las entradas
        adapter.getFilter().filter("");

        // Iniciar la carga de entradas desde Firebase
        cargarEntradasFirebase();
    }

    private void cargarEntradasFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entradas").child(userId);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    listaEntradas.clear();
                    for (DataSnapshot entradaSnapshot : dataSnapshot.getChildren()) {
                        Entrada entrada = entradaSnapshot.getValue(Entrada.class);
                        listaEntradas.add(entrada);
                    }

                    if (listaEntradas.isEmpty()) {
                        Toast.makeText(ListaActivity.this, "No hay entradas disponibles", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                    adapter.getFilter().filter("");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ListaActivity.this, "Error al cargar entradas", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onEditClick(int position, String entradaId) {
        // Abrir la actividad de edición
        Intent intent = new Intent(ListaActivity.this, EdicionEntradaActivity.class);
        intent.putExtra("entradaId", entradaId);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        mostrarDialogoConfirmacionBorrado(position);
    }

    private void mostrarDialogoConfirmacionBorrado(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmación");
        builder.setMessage("¿Estás segur@ de que deseas borrar esta entrada?");

        builder.setPositiveButton("Sí", (dialog, which) -> borrarEntrada(position));

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void borrarEntrada(final int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entradas").child(userId);

            Entrada entradaSeleccionada = listaEntradas.get(position);
            String entradaId = entradaSeleccionada.getKey();
            if (entradaId != null) {
                DatabaseReference entradaRef = databaseReference.child(entradaId);
                entradaRef.removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ListaActivity.this, "Entrada borrada exitosamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ListaActivity.this, "Error al borrar la entrada", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menuOrdenarFecha) {
            ordenarPorFecha();
            return true;
        } else if (itemId == R.id.menuOrdenarTitulo) {
            ordenarPorTitulo();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void ordenarPorFecha() {
        adapter.ordenarPorFecha();
    }

    private void ordenarPorTitulo() {
        adapter.ordenarPorTitulo();
    }

    public void RegresarMN(View Vista) {
        try {
            Intent pantallaMain = new Intent(getApplicationContext(), MainActivity.class);
            pantallaMain.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            finish();
            startActivity(pantallaMain);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la pantalla de recuperación", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.setQuery(searchView.getQuery(), false);
    }
}
