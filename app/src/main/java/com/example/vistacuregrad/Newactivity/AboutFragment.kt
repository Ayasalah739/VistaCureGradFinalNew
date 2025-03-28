package com.example.vistacuregrad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistacuregrad.Newactivity.Model.AboutItem

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())



        val itemList = listOf(
            AboutItem("How it Works?", isHeader = true),
            AboutItem("Take a Clear Photo:", "Ensure the photo is well-lit and in focus."),
            AboutItem("Upload Image:", "Select the 'Upload Image' button and choose the photo."),
            AboutItem("Prediction:", "The app will analyze the uploaded image using AI."),
            AboutItem("Results:", "The app will display a list of potential eye diseases, ranked by probability."),

            AboutItem("Disclaimer!", "This app provides a preliminary assessment. It is not a substitute for professional medical advice.", isDisclaimer = true),

            AboutItem("Important Notes:", isHeader = true),
            AboutItem("Accuracy:", "The accuracy of the predictions depends on the quality of the image and the complexity of the eye condition."),
            AboutItem("Medical Consultation:", "The results provided by this app are for informational purposes only. Always consult with a qualified ophthalmologist for diagnosis and treatment."),
            AboutItem("Data Privacy:", "We are committed to protecting your privacy. Uploaded images may be used to improve the accuracy of the app’s algorithms, but all personal information will be kept confidential."),

            AboutItem("Tips for Optimal Image Capture:", isHeader = true),
            AboutItem("Good Lighting:", "Use natural light or bright, even artificial lighting. Avoid shadows."),
            AboutItem("Clear Focus:", "Ensure the entire eye is in focus, including the pupil and surrounding areas."),
            AboutItem("Straight Position:", "Keep the camera level with the eye to avoid distortion."),
            AboutItem("Close-up:", "Take a close-up photo of the eye, but ensure the entire eye is visible."),

            AboutItem("Disclaimer!", "This app is intended for informational purposes only and should not be considered a substitute for professional medical advice. Always consult with a qualified healthcare provider for any health concerns.", isDisclaimer = true)
        )

        val adapter = AboutAdapter(itemList)
        recyclerView.adapter = adapter


        val backButton: View = view.findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        return view
    }

}
