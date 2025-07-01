package com.example.agenda_t;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntradaAdapter extends RecyclerView.Adapter<EntradaAdapter.EntradaViewHolder> implements Filterable {

    private List<Entrada> listaEntradas;
    private List<Entrada> listaEntradasFiltradas; // Lista original sin cambios
    private OnItemClickListener listener;

    public EntradaAdapter(List<Entrada> listaEntradas, OnItemClickListener listener) {
        this.listaEntradas = listaEntradas;
        this.listaEntradasFiltradas = new ArrayList<>(listaEntradas);
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntradaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entrada, parent, false);
        return new EntradaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntradaViewHolder holder, int position) {
        Entrada entrada = listaEntradasFiltradas.get(position);
        holder.tvTitulo.setText(entrada.getTitulo());
        holder.tvFecha.setText(entrada.getFechaIngreso());
        holder.btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onEditClick(position, entrada.getKey());
                }
            }
        });

        holder.btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaEntradasFiltradas.size();
    }

    public static class EntradaViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitulo;
        public TextView tvFecha;
        public Button btnEditar;
        public Button btnBorrar;

        public EntradaViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnBorrar = itemView.findViewById(R.id.btnBorrar);
        }
    }

    public interface OnItemClickListener {
        void onEditClick(int position, String entradaId);

        void onDeleteClick(int position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filtro = charSequence.toString().toLowerCase().trim();
                List<Entrada> listaFiltrada = new ArrayList<>();

                if (filtro.isEmpty()) {
                    listaFiltrada.addAll(listaEntradas);
                } else {
                    for (Entrada entrada : listaEntradas) {
                        if (entrada.getTitulo().toLowerCase().contains(filtro)) {
                            listaFiltrada.add(entrada);
                        }
                    }
                }

                FilterResults resultados = new FilterResults();
                resultados.values = listaFiltrada;
                return resultados;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listaEntradasFiltradas.clear();
                listaEntradasFiltradas.addAll((List<Entrada>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    // Método de ordenación por fecha
    public void ordenarPorFecha() {
        Collections.sort(listaEntradasFiltradas, new Comparator<Entrada>() {
            @Override
            public int compare(Entrada entrada1, Entrada entrada2) {
                return entrada1.getFechaIngreso().compareTo(entrada2.getFechaIngreso());
            }
        });
        notifyDataSetChanged();
    }

    // Método de ordenación por título
    public void ordenarPorTitulo() {
        Collections.sort(listaEntradasFiltradas, new Comparator<Entrada>() {
            @Override
            public int compare(Entrada entrada1, Entrada entrada2) {
                return entrada1.getTitulo().compareTo(entrada2.getTitulo());
            }
        });
        notifyDataSetChanged();
    }
}
