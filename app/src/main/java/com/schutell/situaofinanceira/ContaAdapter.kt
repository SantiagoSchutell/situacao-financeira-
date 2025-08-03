package com.schutell.situaofinanceira

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.schutell.situaofinanceira.ContaAdapter.ContasViewHolder

interface OnContaClicada{
    fun OnContaClicada(nomeDoBanco: String)
}

class ContaAdapter(private val lista: List<Banco>, private val listener: OnContaClicada) : RecyclerView.Adapter<ContasViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContasViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val itemview = layoutInflater.inflate(R.layout.activity_contas_list_view, parent, false)

        return ContasViewHolder(itemview)

    }

    override fun onBindViewHolder(contasViewHolder: ContasViewHolder, position: Int) {

        val banco = lista[position]

        contasViewHolder.textname.text = banco.nome

        Glide.with(contasViewHolder.itemview.context)
            .load(banco.imageUrl)
            .placeholder(R.drawable.bank)
            .error(R.drawable.bank)
            .into(contasViewHolder.imageButton)


    }

    override fun getItemCount(): Int {
        return  lista.size
    }

    inner class ContasViewHolder(
        val itemview: View
    ) : RecyclerView.ViewHolder(itemview) {
         val textname: TextView = itemview.findViewById(R.id.textNomeDaConta)

         val imageButton: ImageButton = itemview.findViewById(R.id.btnConta)

        init {
            imageButton.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    val botaoClicado = lista[position]
                    listener.OnContaClicada(botaoClicado.nome)
                }
            }
        }

    }

}