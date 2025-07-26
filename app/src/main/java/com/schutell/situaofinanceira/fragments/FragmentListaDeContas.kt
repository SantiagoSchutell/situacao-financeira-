package com.schutell.situaofinanceira.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.ContaAdapter
import com.schutell.situaofinanceira.OnContaClicada
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityListadecontasBinding


class FragmentListaDeContas: Fragment(), OnContaClicada {
    private var _binding: ActivityListadecontasBinding? = null
    private val binding get() = _binding!!

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


        val banco  = data
            .collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .get()

        banco.addOnSuccessListener { bancoName->
            if (bancoName!=null){
                val lista = mutableListOf<String>()
                for(document in bancoName){
                    lista.add(document.id)
                }


                binding.recycleContas.adapter = ContaAdapter(lista, this)
                binding.recycleContas.layoutManager = LinearLayoutManager(requireContext())

            }
        }





        binding.fabAddConta.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null){
                findNavController().navigate(R.id.action_fragment_AddConta)
            }else{
                findNavController().navigate(R.id.action_fragment_login)
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
                val tipoDaConta = dados.getDouble("tipoDeConta")


                if (tipoDaConta?.toInt() == 2131231140){
                    val acao = FragmentListaDeContasDirections.actionFragmentEntrarNoBanco(nomeDoBanco)

                    findNavController().navigate(acao)
                }

                if (tipoDaConta?.toInt() == 2131231142){
                    val acao = FragmentListaDeContasDirections.actionFragmentEntrarCorretora(nomeDoBanco)

                    findNavController().navigate(acao)
                }

            }
        }
    }
}
