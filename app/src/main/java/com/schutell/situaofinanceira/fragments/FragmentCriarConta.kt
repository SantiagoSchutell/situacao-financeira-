package com.schutell.situaofinanceira.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.schutell.situaofinanceira.R
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.schutell.situaofinanceira.databinding.ActivityCriarcontaBinding

class FragmentCriarConta: Fragment() {

    private var _binding: ActivityCriarcontaBinding? = null
    private val binding get() = _binding!!

    private val autenticar by lazy {
        FirebaseAuth.getInstance()
    }

    private val database by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityCriarcontaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCriar.setOnClickListener {
            val email = binding.editTextCriarEmail.text.toString()
            val senha = binding.editTextCriarSenha.text.toString()



            if (email.isEmpty() || senha.isEmpty()){
                Toast.makeText(requireContext(), "Você precisa digitar um email ou senha", Toast.LENGTH_SHORT).show()
            } else{
                autenticar.createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener { sucesso->
                        Toast.makeText(requireContext(), "Sucesso ao criar sua conta!", Toast.LENGTH_SHORT).show()
                        val userId = sucesso.user?.uid
                        if (userId != null) {
                            val dadosUsuario = hashMapOf(
                                "email" to email
                            )
                            database.collection("usuarios").document(userId)
                                .set(dadosUsuario)
                                .addOnSuccessListener {
                                }
                        }
                        autenticar.signInWithEmailAndPassword(email, senha).addOnSuccessListener {
                            findNavController().navigate(R.id.action_fragmentCriarConta_to_fragmentHome)
                        }
                    }
            }
        }
    }

}