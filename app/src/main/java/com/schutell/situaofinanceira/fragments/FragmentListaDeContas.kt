package com.schutell.situaofinanceira.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
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
                    val nome = document.id
                    val urldaImagem = document.getString("imageUrl")

                    lista.add(Banco(nome,urldaImagem))
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
                findNavController().navigate(R.id.action_fragment_AddConta)
            }else{
                findNavController().navigate(R.id.action_fragmentListaDeContas_to_fragmentLogin)
            }
        }




    }

    override fun OnContaClicada(nomeDoBanco: String) {


        val docRef = data
            .collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .document(nomeDoBanco)
            .get()

        docRef.addOnSuccessListener { dados ->
            if (dados != null && dados.exists()) {
                val tipoDaConta = dados.getLong("tipoDeConta")?.toInt()


                when (tipoDaConta) {
                    R.id.radioBtnCorrente -> {
                        val acao =
                            FragmentListaDeContasDirections.actionFragmentEntrarNoBanco(nomeDoBanco)
                        findNavController().navigate(acao)
                    }

                    R.id.radioBtnInvestimentos -> {
                        val acao = FragmentListaDeContasDirections.actionFragmentEntrarCorretora(
                            nomeDoBanco
                        )
                        findNavController().navigate(acao)
                    }

                    else -> {
                        Toast.makeText(
                            requireContext(),
                            "Tipo de conta desconhecido!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Documento não encontrado.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao ler dados: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
