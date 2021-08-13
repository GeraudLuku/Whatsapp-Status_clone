package com.jibee.upwork01.features

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.models.postStory.PostStory
import kotlinx.android.synthetic.main.fragment_status.*


class TextStatusFragment : Fragment() {
    private lateinit var navController: NavController

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //hide keyboard if anywhere else is pressed
        view.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
        }

        //configure nav controller
        navController = findNavController()

        //subscribe to the view model
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        //listen to post story callback
        mainViewModel.postStoryResponse.observe(viewLifecycleOwner) {
            if (it.message != null) {
                println(it.message)
                progress_view.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Failed to Add Story", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Story Added", Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        }

        // show/hide post button if edittext is empty or not
        statusTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isEmpty()) {
                    postBtn.visibility = View.INVISIBLE
                } else {
                    postBtn.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        //close fragment when X is pressed
        closeBtn.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
            navController.popBackStack()
        }

        //get text when done is pressed
        postBtn.setOnClickListener {
            val postText = statusTxt.text.toString().trim()

            //push status to the database
            val story = PostStory(
                getCurrentDateTime().toString("yyyy-MM-dd HH:mm:ss"),
                mediaURL = postText,
                mimeType = ".txt", //set media first so we know if its just plain text or a file
                dateTimeForDb = ""
            )
            mainViewModel.setStoryInfo(story)

            Toast.makeText(requireContext(), "Adding Status....", Toast.LENGTH_LONG).show()
            progress_view.visibility = View.VISIBLE
            view.let { activity?.hideKeyboard(it) }
        }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}