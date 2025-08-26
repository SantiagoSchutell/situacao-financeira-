package com.schutell.situaofinanceira.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.schutell.situaofinanceira.Banco
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
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val user by lazy {
        FirebaseAuth.getInstance()
    }

   private val userID = user.currentUser?.uid.toString()


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
                    menu.clear()
                    menuInflater.inflate(R.menu.menucontas, menu)

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.item_editar -> {

                            findNavController().navigate(FragmentBancoDirections.actionBancoParaEditar(args.bancoId))
                            return true
                        }

                        R.id.item_excluir -> {
                            val caixa = AlertDialog.Builder(requireContext())
                            caixa.setMessage("Você realmente deseja excluir essa conta?")
                                .setPositiveButton("Sim"){dialog, id ->
                                    val imageStorage = storage.reference.child("$userID/bank_logo_${args.bancoId}.jpg")
                                    imageStorage.delete()
                                        .addOnSuccessListener {
                                            data.collection("usuarios")
                                                .document(user.uid.toString())
                                                .collection("bancos")
                                                .document(args.bancoId)
                                                .delete().addOnSuccessListener {ok->
                                                    findNavController().navigate(FragmentBancoDirections.actionFragmentvoltarLista())
                                                    Snackbar.make(requireView(), "Sucesso ao remover conta!", Snackbar.LENGTH_SHORT).show()

                                                }
                                        }
                                }.setNegativeButton("Cancelar"){dialog, id -> }
                            caixa.create()
                            caixa.show()
                            return true
                        }
                    }
                    return false
                }

            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
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
                Snackbar.make(requireView(), "Erro ao buscar dados!", Snackbar.LENGTH_SHORT).show()
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