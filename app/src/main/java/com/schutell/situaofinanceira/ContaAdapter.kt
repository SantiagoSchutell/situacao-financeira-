package com.schutell.situaofinanceira

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schutell.situaofinanceira.ContaAdapter.ContasViewHolder

class ContaAdapter(private val lista: List<String>) : RecyclerView.Adapter<ContasViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContasViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val itemview = layoutInflater.inflate(R.layout.activity_contas_list_view, parent, false)

        return ContasViewHolder(itemview)

    }

    override fun onBindViewHolder(contasViewHolder: ContasViewHolder, position: Int) {

        val nome = lista[position]
        contasViewHolder.textname.text = nome



    }

    override fun getItemCount(): Int {
        return  lista.size
    }

    inner class ContasViewHolder(
        val itemview: View
    ) : RecyclerView.ViewHolder(itemview) {
         val textname: TextView = itemview.findViewById(R.id.textNomeDaConta)

        //Adicionar a Imagem do btn
         val ico: ImageButton = itemview.findViewById(R.id.btnConta)

    }

}