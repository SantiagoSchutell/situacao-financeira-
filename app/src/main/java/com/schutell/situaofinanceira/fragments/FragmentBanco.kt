package com.schutell.situaofinanceira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityBancoBinding
import java.text.NumberFormat
import java.util.Locale

class FragmentBanco : Fragment() {

    private var _binding: ActivityBancoBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentBancoArgs by navArgs()

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
        _binding = ActivityBancoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idDaConta = args.bancoId

        carregarDadosConta(idDaConta)
        inicializarMenuItems()

        binding.fabAddTranzacao.setOnClickListener {
            val idDaContaParaEnviar = args.bancoId

            val action = FragmentBancoDirections.actionFragmentAdTranzacao(idDaContaParaEnviar)

            findNavController().navigate(action)

        }
        binding.btnEditCredito.setOnClickListener {
            binding.btnEditCredito.visibility = INVISIBLE
            binding.editTextCredito.visibility = VISIBLE
            binding.btnEditCreditoOk.visibility = VISIBLE
            binding.textSaldo.visibility = INVISIBLE

        }
        binding.btnEditCreditoOk.setOnClickListener {
            binding.btnEditCredito.visibility = VISIBLE
            binding.editTextCredito.visibility = INVISIBLE
            binding.btnEditCreditoOk.visibility = INVISIBLE
            binding.textSaldo.visibility = VISIBLE

            if (binding.editTextCredito.text.isNotEmpty()) {
                val valor = binding.editTextCredito.text.toString().replace(",", ".").toDouble()
                editarValor(valor, idDaConta, "credito")
                carregarDadosConta(idDaConta)

            }
        }


        binding.btnEditDebito.setOnClickListener {
            binding.btnEditDebito.visibility = INVISIBLE
            binding.editTextdebito.visibility = VISIBLE
            binding.btnEditDebitoOk.visibility = VISIBLE
            binding.textDebitos.visibility = INVISIBLE

        }
        binding.btnEditDebitoOk.setOnClickListener {
            binding.btnEditDebito.visibility = VISIBLE
            binding.editTextdebito.visibility = INVISIBLE
            binding.btnEditDebitoOk.visibility = INVISIBLE
            binding.textDebitos.visibility = VISIBLE

            if (binding.editTextdebito.text.isNotEmpty()) {
                val valor = binding.editTextdebito.text.toString().replace(",", ".").toDouble()
                editarValor(valor, idDaConta, "debito")
                carregarDadosConta(idDaConta)

            }
        }


    }

    private fun inicializarMenuItems() {

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menucontas, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.item_editar -> {
                            Toast.makeText(
                                requireContext(),
                                "Clicado em editar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        R.id.item_excluir -> {
                            Toast.makeText(
                                requireContext(),
                                "Clicado em Excluir",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    return true
                }

            }
        )
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
                val credito = dados.getDouble("credito")
                val debito = dados.getDouble("debito")

                binding.textSaldo.text = formatarParaDinheiro(credito).toString();
                binding.textDebitos.text = formatarParaDinheiro(debito).toString();

                //calcular a saldo liquido da conta
                if (credito != null && debito != null) {
                    val saldoLiq = credito.toDouble() - debito.toDouble()
                    binding.textSaldoLiquido.text = formatarParaDinheiro(saldoLiq).toString()
                }

                //Tool bar
                val toolBar = binding.toolBar.toolbarPrincipal
                toolBar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle)

                (activity as AppCompatActivity).setSupportActionBar(toolBar)
                (activity as AppCompatActivity).supportActionBar?.apply {
                    title = nome.toString()

                }
            } else {
                Toast.makeText(requireContext(), "Erro ao buscar dados!", Toast.LENGTH_SHORT)
                    .show()
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