package com.schutell.situaofinanceira.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityHomeBinding
import java.text.NumberFormat
import java.util.Locale

class FragmentHome : Fragment() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!

    private val data by lazy {
        FirebaseFirestore.getInstance()
    }

    private val useriD = FirebaseAuth.getInstance().currentUser


    override fun onStart() {
        super.onStart()
        if (useriD == null){
            findNavController().navigate(FragmentHomeDirections.actionFhomeToLogin())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun verificarLogin() {
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {
            findNavController().navigate(R.id.action_fragmentLogin_to_fragmentHome)
        } else {

            findNavController().navigate(R.id.fragmentLogin)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


     /*   binding.btnSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            verificarLogin()
        }*/
        inicializarMenuItems()
        calcularSaldosBancos()

        binding.btnContas.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_ContasList)
        }

    }

    private fun calcularSaldosBancos() {
        val docRef = data
            .collection("usuarios")
            .document(useriD?.uid.toString())
            .collection("bancos")
            .get()
        docRef.addOnSuccessListener { dados ->
            if (dados != null && !dados.isEmpty) {
                var creditoTotal = 0.0
                var debitoTotal = 0.0
                var investimentoTotal = 0.0

                for (document in dados) {
                    val valorCredito = document.getDouble("credito") ?: 0.0
                    val valorDebito = document.getDouble("debito") ?: 0.0
                    val totalInvestido = document.getDouble("valorTotal") ?: 0.0
                    creditoTotal += valorCredito
                    debitoTotal += valorDebito
                    investimentoTotal += totalInvestido

                    var valorLiquido = creditoTotal - debitoTotal
                    var total = valorLiquido + totalInvestido
                    binding.textSaldoTotalEmConta.text = formatarParaDinheiro(valorLiquido)
                    binding.textSaldoInvestido.text = formatarParaDinheiro(investimentoTotal)
                    binding.textSaldoTotal.text = formatarParaDinheiro(total)
                }

            } else {
                Snackbar.make(requireView(), "Nenhum banco encontrado!", Snackbar.LENGTH_SHORT).show()

            }

        }
    }


    private fun formatarParaDinheiro(valor: Double?): String {
        val formatar = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatar.format(valor).toString()
    }

    private fun inicializarMenuItems() {
        //Tool bar
        val toolBar = binding.toolBarHome.toolbarPrincipal
        toolBar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle)

        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        (activity as AppCompatActivity).supportActionBar?.apply {

        }
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menu.clear()
                    menuInflater.inflate(R.menu.menu_home, menu)

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.item_deslogar ->{
                            val caixa = AlertDialog.Builder(requireContext())
                            caixa.setMessage("Você realmente deseja sair da sua conta?")
                                .setPositiveButton("Sim"){dialog, id ->
                                    FirebaseAuth.getInstance().signOut()
                                    findNavController().navigate(FragmentHomeDirections.actionFhomeToLogin())
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
