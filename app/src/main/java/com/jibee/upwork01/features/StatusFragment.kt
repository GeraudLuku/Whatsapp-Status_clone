package com.jibee.upwork01.features

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.jibee.upwork01.R
import kotlinx.android.synthetic.main.fragment_status.*


class StatusFragment : Fragment() {
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //configure nav controller
        navController = findNavController()

        //close fragment when X is pressed
        closeBtn.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
            navController.popBackStack()
        }

        //get text when done is pressed
        postBtn.setOnClickListener {
            var postText = statusTxt.text
            Toast.makeText(activity, "status created: $postText", Toast.LENGTH_LONG).show()
            view.let { activity?.hideKeyboard(it) }
            navController.popBackStack()
        }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}