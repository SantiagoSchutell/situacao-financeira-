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
import com.schutell.situaofinanceira.databinding.ActivityHomeBinding

class FragmentHome: Fragment(){

     private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!

     override fun onCreateView(
         inflater: LayoutInflater,
         container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View? {
         _binding = ActivityHomeBinding.inflate(inflater, container, false)
         return binding.root
     }
    private fun verificarLogin(){
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null){
            findNavController().navigate(R.id.action_homepage)
        } else{

            findNavController().navigate(R.id.fragmentLogin)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            verificarLogin()

        }
    }
}
