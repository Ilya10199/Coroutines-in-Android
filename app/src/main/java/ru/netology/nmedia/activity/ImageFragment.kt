package ru.netology.nmedia.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.view.load

class ImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentImageBinding.inflate(
            inflater,
            container,
            false
        )

        binding.apply {
            attachmentAll.setBackgroundColor(Color.BLACK)
            attachmentAll.visibility = View.GONE
            attachmentAll.load("${BASE_URL}/media/${arguments?.getString("image")}")
            attachmentAll.visibility = View.VISIBLE
            back.setOnClickListener {
                findNavController().navigate(R.id.action_imageFragment_to_feedFragment)
            }
        }


        return binding.root
    }
}