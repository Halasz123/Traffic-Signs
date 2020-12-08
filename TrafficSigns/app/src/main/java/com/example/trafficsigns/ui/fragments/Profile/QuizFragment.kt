package com.example.trafficsigns.ui.fragments.Profile

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.trafficsigns.R
import com.example.trafficsigns.data.MyProfileViewModel
import com.example.trafficsigns.databinding.FragmentQuizBinding
import kotlinx.android.synthetic.main.sample_list_item.view.*

class QuizFragment : Fragment() {

    private lateinit var binding: FragmentQuizBinding
    private lateinit var mProfileViewModel: MyProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
                .inflate(
                        inflater,
                        R.layout.fragment_quiz,
                        container,
                        false
                )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mProfileViewModel = ViewModelProvider(this).get(MyProfileViewModel::class.java)
        mProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, {
            if (it.knownTrafficSigns?.count()!! < 5) {
                Toast.makeText(requireContext(), "You have to learn minimum 5 traffic sign!", Toast.LENGTH_LONG).show()
            }
            else {
                binding.start.isEnabled = true
            }
        })

        binding.start.setOnClickListener {
            startTest()
        }
    }

    private fun startTest() {
        var score = 0
        binding.quiz.visibility = View.VISIBLE
        mProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, {
            var signs = it.knownTrafficSigns?.shuffled()

            for ( i in 0 until 5) {
                Glide.with(binding.root).load(signs?.get(i)?.image)
                        .override(binding.quizImage.width,binding.quizImage.height)
                        .into(binding.quizImage)
            }

        })

    }

}