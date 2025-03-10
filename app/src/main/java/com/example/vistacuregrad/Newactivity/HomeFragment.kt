package com.example.vistacuregrad

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.databinding.FragmentHomeBinding
import com.example.vistacuregrad.network.RetrofitClient
import com.example.vistacuregrad.viewmodel.HomeViewModel
import com.example.vistacuregrad.viewmodel.HomeViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val repository = AuthRepository(RetrofitClient.apiService)
        val factory = HomeViewModelFactory(repository)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        setupUI()

        // Restore detection result if navigating between fragments
        if (savedInstanceState != null) {
            savedInstanceState.getString("detection_result")?.let {
                binding.detectionResult.text = it
            }
        } else {
            // Use default message when the app is restarted
            binding.detectionResult.text = getString(R.string.default_detection_message)
        }
    }

    private fun setupUI() {
        binding.bottomNavigationView.itemIconTintList = null
        binding.materialButtondrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navigationView.setNavigationItemSelectedListener { handleNavigation(it) }
        binding.bottomNavigationView.setOnItemSelectedListener { handleBottomNavigation(it) }
        binding.browse.setOnClickListener { openImagePicker() }
    }

    private fun handleNavigation(menuItem: MenuItem): Boolean {
        findNavController().navigate(menuItem.itemId)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleBottomNavigation(item: MenuItem): Boolean {
        findNavController().navigate(item.itemId)
        return true
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data!!.data
                uri?.let {
                    val file = getFileFromUri(requireContext(), it)
                    file?.let { uploadImage(it) }
                }
            } else {
                Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun uploadImage(file: File) {
        val imagePart = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
        )

        homeViewModel.uploadImage(imagePart)
        homeViewModel.uploadResponse.observe(viewLifecycleOwner) { response ->
            val message = if (response != null && response.isSuccessful) {
                val results = response.body()?.results
                if (!results.isNullOrEmpty()) {
                    "Disease: ${results[0].diseaseName}\nProbability: ${results[0].probability}"
                } else "No diseases detected"
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.e("UploadImage", "Error: $errorBody")
                "Image upload failed: ${errorBody ?: "Unknown error"}"
            }

            binding.detectionResult.text = message

            saveDetectionResult(message)
        }
    }

    private fun saveDetectionResult(result: String) {
        val sharedPreferences = requireContext().getSharedPreferences("vistacure_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("detection_result", result).apply()
    }

    private fun getSavedDetectionResult(): String {
        val sharedPreferences = requireContext().getSharedPreferences("vistacure_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("detection_result", getString(R.string.default_detection_message))
            ?: getString(R.string.default_detection_message)
    }

    private fun showDetectionResultsDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val title = dialogView.findViewById<TextView>(R.id.alert_title)
        val messageText = dialogView.findViewById<TextView>(R.id.alert_message)
        val closeButton = dialogView.findViewById<Button>(R.id.alert_close)

        title.text = "Result"
        messageText.text = message

        closeButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("FileCreation", "Error creating file from URI: ${e.message}")
            null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let {
            outState.putString("detection_result", it.detectionResult.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}