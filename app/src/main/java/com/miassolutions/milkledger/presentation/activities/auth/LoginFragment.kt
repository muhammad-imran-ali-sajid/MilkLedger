package com.miassolutions.milkledger.presentation.activities.auth



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
//    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            loginUser(email, password)
        }

        binding.tvGoToSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        return binding.root
    }

    private fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Enter email & password", Toast.LENGTH_SHORT).show()
            return
        }
//
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnSuccessListener {
//                startActivity(Intent(requireContext(), MainActivity::class.java))
//                requireActivity().finish()
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
//            }
    }
}
