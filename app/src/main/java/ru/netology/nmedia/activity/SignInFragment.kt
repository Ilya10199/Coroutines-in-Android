package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSingInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel

class SignInFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSingInBinding.inflate(
            inflater,
            container,
            false
        )

        val viewModel: SignInViewModel by viewModels()

        viewModel.data.observe(viewLifecycleOwner) {
            AppAuth.getInstance().setAuth(it.id, it.token)
            findNavController().navigateUp()
        }

        with(binding) {
            login.requestFocus()
            enter.setOnClickListener {
                viewModel.loginAttempt(login.text.toString(), password.text.toString())


                viewModel.dataState.observe(viewLifecycleOwner) { state ->
                    if (state.loginError) {
                        binding.login.error = getString(R.string.wrong_login)
                    }
                    if (state.passwordError) {
                        binding.password.error = getString(R.string.wrong_password)
                    }
                    if (!state.loginError && !state.passwordError) {
                        findNavController().navigateUp()
                    }
                }
            }
        }

        return binding.root
    }

}