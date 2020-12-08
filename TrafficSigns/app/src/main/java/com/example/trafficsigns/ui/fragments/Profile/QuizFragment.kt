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
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.databinding.FragmentQuizBinding
import kotlinx.android.synthetic.main.sample_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList

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
        binding.start.isEnabled = false
        binding.start.visibility = View.INVISIBLE
        binding.quiz.visibility = View.VISIBLE
        binding.nextButton.visibility = View.VISIBLE
        binding.nextButton.isEnabled = true

        mProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, {
            val signs = it.knownTrafficSigns?.shuffled()
            var answers = arrayListOf<String>()

            var i = 0
                binding.nextButton.setOnClickListener {
                    Glide.with(binding.root).load(signs?.get(i)?.image)
                        .override(binding.quizImage.width,binding.quizImage.height)
                        .into(binding.quizImage)

                    answers.add(signs?.get(i)?.name.toString())
                    answers.add(getFalseAnswer(answers,signs))
                    answers.add(getFalseAnswer(answers,signs))
                    answers.shuffle()

                    binding.radioButton.text = answers[0]
                    binding.radioButton2.text = answers[1]
                    binding.radioButton3.text = answers[2]
                    i+=1
                }


        })


    }
    fun getFalseAnswer(answers: ArrayList<String>, signs: List<TrafficSign>?): String {
        var rand = Random().nextInt(signs?.count() as Int)
        while(answers.contains(signs[rand].name)){
            rand = Random().nextInt(signs.count())
        }
        return signs[rand].name.toString()
    }

}