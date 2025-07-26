package com.schutell.situaofinanceira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.databinding.ActivityCorretoraBinding
import java.text.NumberFormat
import java.util.Locale
import kotlin.getValue

class FragmentCorretora :  Fragment() {

    private var _binding: ActivityCorretoraBinding? = null
    private val binding get() = _binding!!

    private val args: FragmentCorretoraArgs by navArgs()

    private val data by lazy {
        FirebaseFirestore.getInstance()
    }
    private val user by lazy {
        FirebaseAuth.getInstance()
    }

    private val ContaName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityCorretoraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idDaConta = args.bancoId

        carregarDadosConta(idDaConta)

    }

    private fun carregarDadosConta(bancoId: String) {
        val docRef = data
            .collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .document(bancoId)
            .get()

        docRef.addOnSuccessListener { dados ->
            if (dados != null && dados.exists()) {
                val tipoDaConta = dados.getDouble("tipoDeConta")


                val nome = dados.get("nome")
                val acoes = dados.getDouble("acoes")
                val fiis = dados.getDouble("fiis")
                val rendaFixa = dados.getDouble("rendaFixa")
                val proventos = dados.getDouble("proventos")
                val contaDeInvestimentos = dados.getDouble("contaDeInvestimentos")

                val totalValor =
                    acoes!! + fiis!! + rendaFixa!! + proventos!! + contaDeInvestimentos!!


                binding.textBancoNome.text = nome.toString()
                binding.textSaldoTotalInvestido.text = formatarParaDinheiro(totalValor)
                binding.textAcoes.text = formatarParaDinheiro(acoes)
                binding.textFiis.text = formatarParaDinheiro(fiis)
                binding.textRendaFixa.text = formatarParaDinheiro(rendaFixa)
                binding.textProventos.text = formatarParaDinheiro(proventos)
                binding.textContaDeInvestimentos.text = formatarParaDinheiro(contaDeInvestimentos)


            }
        }

    }

    private fun formatarParaDinheiro(valor: Double?): String {
        val formatar = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatar.format(valor).toString()
    }

    private fun editarValor(valor: Double, bancoId: String, tipo: String) {
        val docRef = data
            .collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .document(bancoId)

        docRef.update(tipo, valor)
    }

}