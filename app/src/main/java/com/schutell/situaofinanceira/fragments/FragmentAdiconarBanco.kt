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
        if (binding.editTextNomeConta.text.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "Por favor, insira o nome da conta.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        if (binding.radioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Por favor, selecione o tipo de conta.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val userID = autenticar.currentUser?.uid

        if (userID == null) {
            Toast.makeText(requireContext(), "Erro: Usuário não autenticado.", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigate(R.id.action_fragment_login)
            return
        }


        val nomeDoBanco = binding.editTextNomeConta.text.toString()
        val tipo = binding.radioGroup.checkedRadioButtonId
        val urlDaIamgem = ""


        when (tipo) {

            //banco
            R.id.radioBtnCorrente -> {

                val dados = mapOf(
                    "nome" to nomeDoBanco,
                    "imageUrl" to urlDaIamgem,
                    "credito" to 0,
                    "debito" to 0,
                    "tipoDeConta" to tipo
                )

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
                    .addOnFailureListener { erro ->
                        Toast.makeText(
                            requireContext(),
                            "Erro ao salvar! ${erro.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            }


            //Corretora
            R.id.radioBtnInvestimentos -> {


                val dados = mapOf(
                    "nome" to nomeDoBanco,
                    "tipoDeConta" to tipo,
                    "imageUrl" to urlDaIamgem,
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
                        .addOnFailureListener { erro ->
                            Toast.makeText(
                                requireContext(),
                                "Erro ao salvar! ${erro.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    findNavController().navigate(R.id.action_fragment_login)
                }
            }


        }
    }
}


