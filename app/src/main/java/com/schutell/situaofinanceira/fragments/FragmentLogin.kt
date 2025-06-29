package com.schutell.situaofinanceira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
            findNavController().navigate(R.id.action_homepage)
        } else{
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Faça login antes de continuar", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "O email e senha não podem estar vazio!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            autentication.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener  { sucesso->
                    activity?.runOnUiThread {
                        if (sucesso.isSuccessful){
                            Toast.makeText(requireContext(), "Sucesso ao entrar", Toast.LENGTH_SHORT).show()
                            verificarLogin()
                        }else{
                            Toast.makeText(requireContext(), "Erro ao entrar! ${sucesso.exception?.message}", Toast.LENGTH_SHORT).show()

                        }
                    }
                }
        }



        binding.btnCriarConta.setOnClickListener {
            findNavController().navigate(R.id.action_criarConta)
        }
    }

}