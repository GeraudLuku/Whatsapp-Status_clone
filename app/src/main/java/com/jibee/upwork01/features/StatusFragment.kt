package com.jibee.upwork01.features

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.models.postStory.PostStory
import kotlinx.android.synthetic.main.fragment_status.*


class StatusFragment : Fragment() {
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


        //configure nav controller
        navController = findNavController()

        //subscribe to the view model
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        //close fragment when X is pressed
        closeBtn.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
            navController.popBackStack()
        }

        //get text when done is pressed
        postBtn.setOnClickListener {
            val postText = statusTxt.text.toString()

            //push status to the database
            val story = PostStory(
                getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss"),
                mediaURL = postText,
                mimeType = "text" //set media first so we know if its just plain text or a file
            )
            mainViewModel.setStoryInfo(story)

            //create a src object
//            val newStatus = Src(
//                FirebaseAuth.getInstance().currentUser?.uid!!,
//                "text",
//                postText,
//                "12:30",
//                "no source"
//            )
            //add the status
//            storiesViewModel.addStory(newStatus)
//            Toast.makeText(requireContext(), "Adding Status....", Toast.LENGTH_LONG).show()


            view.let { activity?.hideKeyboard(it) }
            navController.popBackStack()
        }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}