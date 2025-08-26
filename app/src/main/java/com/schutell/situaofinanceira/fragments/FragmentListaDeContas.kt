package com.schutell.situaofinanceira.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.Banco
import com.schutell.situaofinanceira.ContaAdapter
import com.schutell.situaofinanceira.OnContaClicada
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityListadecontasBinding


class FragmentListaDeContas: Fragment(), OnContaClicada {

    private var _binding: ActivityListadecontasBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContaAdapter

    private val data by lazy {
        FirebaseFirestore.getInstance()
    }

    private val user by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityListadecontasBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        }


    override fun onResume() {
        super.onResume()
        val banco  = data
            .collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .get()

        banco.addOnSuccessListener { contaAdicionada->
            if (contaAdicionada != null && !contaAdicionada.isEmpty) {
                val lista = mutableListOf<Banco>()

                for(document in contaAdicionada){
                    val idBanco = document.id
                    val nome = document.getString("nome").toString()
                    val urldaImagem = document.getString("imageUrl")

                    lista.add(Banco(idBanco, nome,urldaImagem))
                }

                adapter = ContaAdapter(lista, this)
                if (isAdded) {
                    adapter = ContaAdapter(lista, this)
                    binding.recycleContas.adapter = adapter
                    binding.recycleContas.layoutManager = GridLayoutManager(requireContext(), 2)
                }
            }
    }

        binding.fabAddConta.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null){
                findNavController().navigate(FragmentListaDeContasDirections.actionFragmentAddConta())
            }else{
                findNavController().navigate(FragmentListaDeContasDirections.actionFragmentListaDeContasToFragmentLogin())
            }
        }




    }

    override fun OnContaClicada(idbanco: String) {
        val docRef = data
            .collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .document(idbanco)
            .get()

        docRef.addOnSuccessListener { dados ->
            if (dados != null && dados.exists()) {
                val tipoDaConta = dados.getString("tipoDeConta")


                when (tipoDaConta) {
                    "CONTA_CORRENTE" -> {
                        val acao =
                            FragmentListaDeContasDirections.actionFragmentEntrarNoBanco(idbanco)
                        findNavController().navigate(acao)
                    }

                    "CONTA_INVESTIMENTOS" -> {
                        val acao = FragmentListaDeContasDirections.actionFragmentEntrarCorretora(
                            idbanco
                        )
                        findNavController().navigate(acao)
                    }

                    else -> {
                        Snackbar.make(requireView(), "Tipo de conta desconhecido!", Snackbar.LENGTH_SHORT).show()

                    }
                }
            } else {
                Snackbar.make(requireView(), "Documento não encontrado.", Snackbar.LENGTH_SHORT).show()

            }
        }
            .addOnFailureListener { exception ->
                Snackbar.make(requireView(), "Erro ao ler dados: ${exception.localizedMessage}", Snackbar.LENGTH_SHORT).show()
            }
    }
}
