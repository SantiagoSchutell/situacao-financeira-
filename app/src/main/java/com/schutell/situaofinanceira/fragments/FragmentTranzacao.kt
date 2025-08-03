package com.schutell.situaofinanceira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityTranzacaoBinding
import kotlin.getValue

class FragmentTranzacao : Fragment() {
    private var _binding: ActivityTranzacaoBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentTranzacaoArgs by navArgs()

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
        _binding = ActivityTranzacaoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idDaConta = args.bancoId

        binding.btnCredito.setOnClickListener {
            val docRef = data
                .collection("usuarios")
                .document(user.uid.toString())
                .collection("bancos")
                .document(idDaConta)
                .get()

            docRef.addOnSuccessListener { dados ->
                if (dados != null && dados.exists()) {
                    val novoValor = binding.editTextValor.text.toString().replace(",", ".").toDouble()
                    val credito = dados.getDouble("credito")!!.toDouble()

                    val total = credito + novoValor

                    val dados = mapOf(
                        "credito" to total
                    )

                    val docRefs = data
                        .collection("usuarios")
                        .document(user.uid.toString())
                        .collection("bancos")
                        .document(idDaConta)
                        .update(dados)
                        .addOnSuccessListener {
                            val idBanco = args.bancoId
                            val action =
                                FragmentTranzacaoDirections.actionFragmentvoltarparabanco(idBanco)
                            findNavController().navigate(action)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Digite o valor da tranzação!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

        }

        binding.btnDebito.setOnClickListener {
            val docRef = data
                .collection("usuarios")
                .document(user.uid.toString())
                .collection("bancos")
                .document(idDaConta)
                .get()

            docRef.addOnSuccessListener { dados ->
                if (dados != null && dados.exists()) {
                    val novoValor = binding.editTextValor.text.toString().replace(",", ".").toDouble()
                    val credito = dados.getDouble("credito")!!.toDouble()
                    val debito = dados.getDouble("debito")!!.toDouble()

                    val total =  debito + novoValor

                    val dados = mapOf(
                        "debito" to total
                    )

                    val docRefs = data
                        .collection("usuarios")
                        .document(user.uid.toString())
                        .collection("bancos")
                        .document(idDaConta)
                        .update(dados)
                        .addOnSuccessListener {
                            val idBanco = args.bancoId
                            val action =
                                FragmentTranzacaoDirections.actionFragmentvoltarparabanco(idBanco)
                            findNavController().navigate(action)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Digite o valor da tranzação!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

        }



    }

}