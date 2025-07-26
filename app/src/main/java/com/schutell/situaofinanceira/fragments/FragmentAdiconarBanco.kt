package com.schutell.situaofinanceira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityAdicionarbancoBinding
import com.schutell.situaofinanceira.databinding.ActivityHomeBinding
import com.schutell.situaofinanceira.databinding.ActivityLoginBinding

class FragmentAdiconarBanco : Fragment() {
    private var _binding: ActivityAdicionarbancoBinding? = null
    private val binding get() = _binding!!
    private val data by lazy {
        FirebaseFirestore.getInstance()
    }
    private val autenticar by lazy {
        FirebaseAuth.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityAdicionarbancoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnAdicionar.setOnClickListener {
            adicionarNovaConta()
        }


    }

    private fun adicionarNovaConta() {
        if (binding.editTextNomeConta.text != null && binding.radioGroup.checkedRadioButtonId != -1) {
            var nomeDoBanco = binding.editTextNomeConta.text.toString()
            var tipo = binding.radioGroup.checkedRadioButtonId

            //Banco
            if (tipo == 2131231140) {
                val dados = mapOf(
                    "nome" to nomeDoBanco,
                    "credito" to 0,
                    "debito" to 0,
                    "tipoDeConta" to tipo
                )

                if (autenticar.currentUser != null) {


                    var userID = autenticar.currentUser?.uid.toString()
                    data.collection("usuarios")
                        .document(userID)
                        .collection("bancos")
                        .document(nomeDoBanco)
                        .set(dados)

                        .addOnSuccessListener { ok ->
                            Toast.makeText(
                                requireContext(),
                                "Sucesso ao Adicionar!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.action_fragmen_para_contasList)
                        }
                } else {
                    findNavController().navigate(R.id.action_fragment_login)
                }

            }

            //Corretora
            if (tipo == 2131231142){

                val dados = mapOf(
                    "nome" to nomeDoBanco,
                    "tipoDeConta" to tipo,
                    "acoes" to 0,
                    "fiis" to 0,
                    "rendaFixa" to 0,
                    "proventos" to 0,
                    "contaDeInvestimentos" to 0

                )

                if (autenticar.currentUser != null) {


                    var userID = autenticar.currentUser?.uid.toString()
                    data.collection("usuarios")
                        .document(userID)
                        .collection("bancos")
                        .document(nomeDoBanco)
                        .set(dados)

                        .addOnSuccessListener { ok ->
                            Toast.makeText(
                                requireContext(),
                                "Sucesso ao Adicionar!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.action_fragmen_para_contasList)
                        }
                } else {
                    findNavController().navigate(R.id.action_fragment_login)
                }
            }

        }
    }

}

