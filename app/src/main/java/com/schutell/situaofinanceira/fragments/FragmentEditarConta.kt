package com.schutell.situaofinanceira.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityEditarcontaBinding
import java.io.ByteArrayOutputStream
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
    private lateinit var novoNome: String
    private lateinit var nomeDoBanco: String
    private lateinit var tipoDaConta: String
    private lateinit var idBanco: String
    private var imagemUrl: Uri? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imagemUrl = uri
                binding.imageContaLogo.setImageURI(imagemUrl)

            } else {
                val uriPadrao = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.bankimamge}")
                imagemUrl = uriPadrao
                binding.imageContaLogo.setImageURI(imagemUrl)
            }
        }

    override fun onStart() {
        super.onStart()
        if (user.currentUser == null){
            Snackbar.make(requireView(), "Faça login antes de continuar.", Snackbar.LENGTH_SHORT).show()
            findNavController().navigate(FragmentEditarContaDirections.actionEditarParaLogin())
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
                 novoNome = binding.textInputEditNome.text.toString()
            }
            salvarEdicoes(novoNome)
        }

    }

    private fun salvarEdicoes(nome: String) {
        val nomeParaSalvar = if (nome.isNotBlank()) nome else nomeDoBanco
        val dados = mapOf(
            "nome" to nomeParaSalvar
        )

        data.collection("usuarios")
            .document(user.uid.toString())
            .collection("bancos")
            .document(args.bancoId)
            .update(dados).addOnSuccessListener {
                salvarImagem(idBanco){sucesso ->
                    if (sucesso){
                        Snackbar.make(requireView(), "Conta atualizada com sucesso!", Snackbar.LENGTH_SHORT).show()
                        if (tipoDaConta == "CONTA_CORRENTE"){
                            findNavController().navigate(FragmentEditarContaDirections.actionEditarParaBanco(args.bancoId))
                        }else{
                            findNavController().navigate(FragmentEditarContaDirections.actionEditarParaCorretora(args.bancoId))
                        }
                    }
                }
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Erro ao salvar o nome.", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun salvarImagem(idBanco: String, onComplete: (sucesso: Boolean) -> Unit) {
        binding.progressBarHome.visibility = VISIBLE
        binding.btnSalvarEdicao.visibility = INVISIBLE

        val userId = user.currentUser?.uid
        if (imagemUrl == null || userId == null) {
            onComplete(true)
            return
        }

        val storageRef = storage.reference.child("$userId/bank_logo_$idBanco.jpg")
        storageRef.putFile(imagemUrl!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val dadosImagem = mapOf("imageUrl" to downloadUrl.toString())

                    binding.progressBarHome.visibility = INVISIBLE
                    binding.btnSalvarEdicao.visibility = VISIBLE

                    data.collection("usuarios").document(userId)
                        .collection("bancos").document(idBanco)
                        .update(dadosImagem)
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                } else {
                    onComplete(false)
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
                tipoDaConta = dados.getString("tipoDeConta").toString()
                nomeDoBanco = dados.get("nome").toString()
                idBanco = dados.id

                binding.textInputNome.hint = nomeDoBanco

            } else {
                Snackbar.make(requireView(), "Erro ao buscar dados!", Snackbar.LENGTH_SHORT).show()
            }

        }
    }
}