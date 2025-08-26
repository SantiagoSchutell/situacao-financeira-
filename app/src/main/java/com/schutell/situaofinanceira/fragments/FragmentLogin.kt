package com.schutell.situaofinanceira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityLoginBinding

class FragmentLogin: Fragment() {

    private val autentication by lazy {
        FirebaseAuth.getInstance()
    }

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private fun verificarLogin(){
        val usuario = autentication.currentUser

        if (usuario != null){
            findNavController().navigate(R.id.action_fragmentLogin_to_fragmentHome)
        } else{
            activity?.runOnUiThread {
                Snackbar.make(requireView(), "Faça login antes de continuar", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    //verifica se ta logado
    override fun onStart() {
        super.onStart()
        verificarLogin()

    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogIn.setOnClickListener {

            val email = binding.editTextEmail.text.toString()
            val senha = binding.editTextSenha.text.toString()


            if (email.isEmpty() || senha.isEmpty()){
                Snackbar.make(requireView(), "O email e senha não podem estar vazio!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            autentication.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener  { sucesso->
                    activity?.runOnUiThread {
                        if (sucesso.isSuccessful){
                            Snackbar.make(requireView(), "Sucesso ao entrar", Snackbar.LENGTH_SHORT).show()
                            verificarLogin()
                        }else{
                            Snackbar.make(requireView(), "Erro ao entrar! ${sucesso.exception?.message}", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
        }



        binding.btnCriarConta.setOnClickListener {
            findNavController().navigate(R.id.action_criarConta)
        }
    }

}