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
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityCorretoraBinding
import java.text.DecimalFormat
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

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }

    private val userID = user.currentUser?.uid.toString()

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
        inicializarMenuItems()


        ///Editar Ação
        binding.btnEditAcao.setOnClickListener {
            binding.btnEditAcao.visibility = INVISIBLE
            binding.editTextAcoes.visibility = VISIBLE
            binding.btnEdiAcaoOk.visibility = VISIBLE
            binding.textAcoes.visibility = INVISIBLE

        }
        binding.btnEdiAcaoOk.setOnClickListener {
            binding.btnEditAcao.visibility = VISIBLE
            binding.editTextAcoes.visibility = INVISIBLE
            binding.btnEdiAcaoOk.visibility = INVISIBLE
            binding.textAcoes.visibility = VISIBLE

            if (binding.editTextAcoes.text.isNotEmpty()) {
                val valor = binding.editTextAcoes.text.toString().replace(",", ".").toDouble()
                editarValor(valor, idDaConta, "acoes")
                carregarDadosConta(idDaConta)

            }
        }



        //Editar Fiis
        binding.btnEditFiis.setOnClickListener {
            binding.btnEditFiis.visibility = INVISIBLE
            binding.editTextFiis.visibility = VISIBLE
            binding.btnEdifiisOK.visibility = VISIBLE
            binding.textFiis.visibility = INVISIBLE

        }
        binding.btnEdifiisOK.setOnClickListener {
            binding.btnEditFiis.visibility = VISIBLE
            binding.editTextFiis.visibility = INVISIBLE
            binding.btnEdifiisOK.visibility = INVISIBLE
            binding.textFiis.visibility = VISIBLE

            if (binding.editTextFiis.text.isNotEmpty()) {
                val valor = binding.editTextFiis.text.toString().replace(",", ".").toDouble()
                editarValor(valor, idDaConta, "fiis")
                carregarDadosConta(idDaConta)

            }
        }


        //editar Renda Fixa
        binding.btnEdiRendFix.setOnClickListener {
            binding.btnEdiRendFix.visibility = INVISIBLE
            binding.editTextRendaFixa.visibility = VISIBLE
            binding.btnEditRendaFixOK.visibility = VISIBLE
            binding.textRendaFixa.visibility = INVISIBLE

        }
        binding.btnEditRendaFixOK.setOnClickListener {
            binding.btnEdiRendFix.visibility = VISIBLE
            binding.editTextRendaFixa.visibility = INVISIBLE
            binding.btnEditRendaFixOK.visibility = INVISIBLE
            binding.textRendaFixa.visibility = VISIBLE

            if (binding.editTextRendaFixa.text.isNotEmpty()) {
                val valor = binding.editTextRendaFixa.text.toString().replace(",", ".").toDouble()
                editarValor(valor, idDaConta, "rendaFixa")
                carregarDadosConta(idDaConta)

            }
        }



        //Editar Proventos
        binding.btnEdiProventos.setOnClickListener {
            binding.btnEdiProventos.visibility = INVISIBLE
            binding.editTextProventos.visibility = VISIBLE
            binding.btnEditProventosOK.visibility = VISIBLE
            binding.textProventos.visibility = INVISIBLE

        }
        binding.btnEditProventosOK.setOnClickListener {
            binding.btnEdiProventos.visibility = VISIBLE
            binding.editTextProventos.visibility = INVISIBLE
            binding.btnEditProventosOK.visibility = INVISIBLE
            binding.textProventos.visibility = VISIBLE

            if (binding.editTextProventos.text.isNotEmpty()) {
                val valor = binding.editTextProventos.text.toString().replace(",", ".").toDouble()
                editarValor(valor, idDaConta, "proventos")
                carregarDadosConta(idDaConta)

            }
        }

        //Editar Conta de investimentos
        binding.btnEdiContaDeInvestimentos.setOnClickListener {
            binding.btnEdiContaDeInvestimentos.visibility = INVISIBLE
            binding.editTextContaDeInvestimentos.visibility = VISIBLE
            binding.btnEditContaDeInvestmentosOK.visibility = VISIBLE
            binding.textContaDeInvestimentos.visibility = INVISIBLE

        }
        binding.btnEditContaDeInvestmentosOK.setOnClickListener {
            binding.btnEdiContaDeInvestimentos.visibility = VISIBLE
            binding.editTextContaDeInvestimentos.visibility = INVISIBLE
            binding.btnEditContaDeInvestmentosOK.visibility = INVISIBLE
            binding.textContaDeInvestimentos.visibility = VISIBLE

            if (binding.editTextContaDeInvestimentos.text.isNotEmpty()) {
                val valor = binding.editTextContaDeInvestimentos.text.toString().replace(",", ".").toDouble()
                editarValor(valor, idDaConta, "contaDeInvestimentos")
               carregarDadosConta(idDaConta)

            }
        }

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
                val acoes = dados.getDouble("acoes") ?: 0.0
                val fiis = dados.getDouble("fiis") ?: 0.0
                val rendaFixa = dados.getDouble("rendaFixa") ?: 0.0
                val proventos = dados.getDouble("proventos") ?: 0.0
                val contaDeInvestimentos = dados.getDouble("contaDeInvestimentos") ?: 0.0

                val totalValor =
                    acoes + fiis + rendaFixa+ proventos + contaDeInvestimentos

                binding.textSaldoTotalInvestido.text = formatarParaDinheiro(totalValor)
                binding.textAcoes.text = formatarParaDinheiro(acoes)
                binding.textFiis.text = formatarParaDinheiro(fiis)
                binding.textRendaFixa.text = formatarParaDinheiro(rendaFixa)
                binding.textProventos.text = formatarParaDinheiro(proventos)
                binding.textContaDeInvestimentos.text = formatarParaDinheiro(contaDeInvestimentos)



                val docRefUp = data
                    .collection("usuarios")
                    .document(user.uid.toString())
                    .collection("bancos")
                    .document(bancoId)

                docRefUp.update("valorTotal", totalValor)

                //Tool bar
                val toolBar = binding.toolbarCorretora.toolbarPrincipal
                toolBar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle)

                (activity as AppCompatActivity).setSupportActionBar(toolBar)
                (activity as AppCompatActivity).supportActionBar?.apply {
                    title = nome.toString()

                }
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

                            findNavController().navigate(FragmentCorretoraDirections.actionCorretoraParaEditar(args.bancoId))
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

}