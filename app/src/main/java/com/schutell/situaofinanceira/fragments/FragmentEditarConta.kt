package com.schutell.situaofinanceira.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityEditarcontaBinding
import kotlin.getValue

class FragmentEditarConta : androidx.fragment.app.Fragment() {

    private var _binding: ActivityEditarcontaBinding? = null
    private val binding get() = _binding!!
    private val data by lazy {
        FirebaseFirestore.getInstance()
    }

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val user by lazy {
        FirebaseAuth.getInstance()
    }
    private val args: FragmentBancoArgs by navArgs()
    private lateinit var nomeDoBanco: String
    private var imagemUrl: Uri? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri != null) {
                imagemUrl = uri
                binding.imageContaLogo.setImageURI(imagemUrl)

            } else {
                val nomeDoPacote = requireContext().packageName
                val drawableId = R.drawable.bankimamge
                val uriPadrao = Uri.parse("android.resource://${nomeDoPacote}/$drawableId")
                imagemUrl = uriPadrao
                binding.imageContaLogo.setImageURI(imagemUrl)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityEditarcontaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idDaConta = args.bancoId

        carregarDadosConta(idDaConta)
        iniciarToolBar()

        binding.imageContaLogo.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.btnSalvarEdicao.setOnClickListener {

            if (binding.textInputEditNome.text != null){
                 nomeDoBanco = binding.textInputEditNome.text.toString()
            }
            salvarEdicoes(nomeDoBanco, imagemUrl.toString())
        }

    }

    private fun salvarEdicoes(nome: String, imagem: String) {
        val docRef = data
            .collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .document(args.bancoId)
            .get().addOnSuccessListener { dados->
                val tipo = dados.get("tipoDeConta")
                if (nome.isNotEmpty()){
                    val dados = mapOf(
                        "imageUrl" to imagem,
                        "nome" to nome
                    )

                    val docRef = data
                        .collection("usuarios")
                        .document(user.uid.toString())
                        .collection("bancos")
                        .document(args.bancoId)
                        .update(dados).addOnSuccessListener {
                           if (tipo == "CONTA_CORRENTE"){
                               findNavController().navigate(FragmentEditarContaDirections.actionEditarParaBanco(args.bancoId))
                           } else{
                               findNavController().navigate(FragmentEditarContaDirections.actionEditarParaCorretora(args.bancoId))
                           }
                        }
                }
                else{
                    binding.textInputNome.error = "Digite o nome da conta!"
                }
            }


    }


    private fun iniciarToolBar() {

        val toolBar = binding.toolbarEditar.toolbarPrincipal
        toolBar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "Editar Conta"

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
                val tipoDaConta = dados.getString("tipoDeConta")

                val nome = dados.get("nome")
                binding.textInputNome.hint = nome.toString()

            } else {
                Toast.makeText(requireContext(), "Erro ao buscar dados!", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }
}