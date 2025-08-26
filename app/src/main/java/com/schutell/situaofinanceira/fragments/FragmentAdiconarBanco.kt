package com.schutell.situaofinanceira.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.schutell.situaofinanceira.R
import com.schutell.situaofinanceira.databinding.ActivityAdicionarbancoBinding
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlin.getValue

class FragmentAdiconarBanco : Fragment() {
    private var _binding: ActivityAdicionarbancoBinding? = null
    private val binding get() = _binding!!
    private val data by lazy {
        FirebaseFirestore.getInstance()
    }
    private val autenticar by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val args: FragmentBancoArgs by navArgs()
    private var uriDaImagemSelecionada: Uri? = null

    private var uriIcoStorege: Uri? = null
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri != null) {
                uriDaImagemSelecionada = uri
                binding.imageViewLogo.setImageURI(uriDaImagemSelecionada)

            } else {
                val nomeDoPacote = requireContext().packageName
                val drawableId = R.drawable.bankimamge
                val uriPadrao = Uri.parse("android.resource://${nomeDoPacote}/$drawableId")
                uriDaImagemSelecionada = uriPadrao
                binding.imageViewLogo.setImageURI(uriDaImagemSelecionada)
            }
        }
    private var permissaoGaleria = false
    private var permissaoCamera = false




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityAdicionarbancoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        obterPermissoes()

        binding.imageViewLogo.setOnClickListener {
            selectImageLauncher.launch("image/*")

        }
        binding.btnAdicionar.setOnClickListener {
            adicionarNovaConta()
        }

    }

    private fun obterPermissoes() {

        permissaoCamera =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        permissaoGaleria = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_IMAGES
        )== PackageManager.PERMISSION_GRANTED

        val permissoesNegadas = mutableListOf<String>()
        if(!permissaoCamera)permissoesNegadas.add(Manifest.permission.CAMERA)
        if (!permissaoGaleria)permissoesNegadas.add(Manifest.permission.READ_MEDIA_IMAGES)

        if (permissoesNegadas.isNotEmpty()){
            val gerenciadorDePermissoes =registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ){permissoes->
                permissaoCamera = permissoes[Manifest.permission.CAMERA]?: permissaoCamera
                permissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]?: permissaoGaleria
            }
            gerenciadorDePermissoes.launch(permissoesNegadas.toTypedArray())
        }


    }

    private fun adicionarNovaConta() {


        if (binding.editTextNomeConta.text.isNullOrBlank()) {

            Snackbar.make(requireView(), "Por favor, insira o nome da conta.", Snackbar.LENGTH_SHORT).show()

            return
        }


        if (binding.radioGroup.checkedRadioButtonId == -1) {

            Snackbar.make(requireView(), "Por favor, selecione o tipo de conta.", Snackbar.LENGTH_SHORT).show()

            return
        }


        val userID = autenticar.currentUser?.uid

        if (userID == null) {

            Snackbar.make(requireView(), "Erro: Usuário não autenticado.", Snackbar.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_fragment_login)
            return
        }



        //GERA um ID aleatorio bara a conta
        val idBanco = data.collection("usuarios")
            .document(userID)
            .collection("bancos")
            .document().id

        val nomeDoBanco = binding.editTextNomeConta.text.toString()
        val checkedId = binding.radioGroup.checkedRadioButtonId
        val radioButtonSelecionado = binding.root.findViewById<android.widget.RadioButton>(checkedId)
        val tipoDeContaTag = radioButtonSelecionado.tag.toString()




        var icoUri = uriDaImagemSelecionada ?: run {
            val nomeDoPacote = requireContext().packageName
            val drawableId = R.drawable.bankimamge
            Uri.parse("android.resource://${nomeDoPacote}/$drawableId")
        }

        val refDoArquivo = storage.reference.child(userID).child("bank_logo_${idBanco.toString()}.jpg")

        if (uriDaImagemSelecionada != null) {
            refDoArquivo.putFile(uriDaImagemSelecionada!!).addOnSuccessListener { take ->
                take.storage.downloadUrl.addOnSuccessListener { urlDaIamgem ->
                    salvarNoFirebase(idBanco, nomeDoBanco, tipoDeContaTag, urlDaIamgem)
                }
            }
        } else {
            val imagemPadrao = compactarImagem(R.drawable.bankimamge)
            refDoArquivo.putBytes(imagemPadrao).addOnSuccessListener { take ->
                take.storage.downloadUrl.addOnSuccessListener { urlDaIamgem ->
                    salvarNoFirebase(idBanco, nomeDoBanco, tipoDeContaTag, urlDaIamgem)
                }
            }
        }
    }

    private fun salvarNoFirebase(idBanco: String, nomeDoBanco: String, tipoDeContaTag: String, imageUrl: Uri) {
        var userID = autenticar.currentUser?.uid.toString()

        val dados = when (tipoDeContaTag) {

            //banco
            "CONTA_CORRENTE" -> mapOf(
                "nome" to nomeDoBanco,
                "imageUrl" to imageUrl.toString(),
                "credito" to 0,
                "debito" to 0,
                "tipoDeConta" to tipoDeContaTag
            )


            //Corretora
            "CONTA_INVESTIMENTOS" -> {
                mapOf(
                    "nome" to nomeDoBanco,
                    "tipoDeConta" to tipoDeContaTag,
                    "imageUrl" to imageUrl.toString(),
                    "acoes" to 0,
                    "fiis" to 0,
                    "rendaFixa" to 0,
                    "proventos" to 0,
                    "contaDeInvestimentos" to 0,
                    "valorTotal" to 0

                )

            }

            else -> null
        }
        if (dados != null) {
            data.collection("usuarios")
                .document(userID)
                .collection("bancos")
                .document(idBanco)
                .set(dados)
                .addOnSuccessListener {

                    Snackbar.make(requireView(), "Sucesso ao Adicionar!", Snackbar.LENGTH_SHORT).show()

                    findNavController().navigate(R.id.action_fragmen_para_contasList)
                }
                .addOnFailureListener { erro ->
                    Snackbar.make(requireView(), "Erro ao salvar", Snackbar.LENGTH_SHORT).show()

                }
                .addOnFailureListener {
                    Log.i("app", "ERRO ao salvar dados")
                }
        }
    }

    private fun compactarImagem(drawableId: Int): ByteArray {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
            ?: throw IllegalArgumentException("Recurso de imagem não encontrado")

        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos)
        return baos.toByteArray()
    }
}


