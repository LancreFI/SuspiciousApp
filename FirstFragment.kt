package com.example.suspiciousapp

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.suspiciousapp.netSock.netSock
import com.example.suspiciousapp.snoopLocation.LocationHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */

class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //You can parse the date below, if needed
        //val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        //val currentDateAndTime: String = simpleDateFormat.format(Date())


        view.findViewById<Button>(R.id.loc_button).setOnClickListener {
            val latitudeTextView = view.findViewById<TextView>(R.id.textview_first_content)
            val longitudeTextView = view.findViewById<TextView>(R.id.textview_second_content)
            val toasterMesg = "Got location!"

           LocationHelper().startListeningUserLocation(
                requireActivity(),
                object : LocationHelper.MyLocationListener {
                    override fun onLocationChanged(location: Location) {
                        // Here you get user location data, loct contains all possible data, if needed
                        //val loct = "$location"
                        latitudeTextView.text = ("${location.latitude}").toString()
                        longitudeTextView.text = ("${location.longitude}").toString()
                        val toaster = Toast.makeText(context, toasterMesg, Toast.LENGTH_SHORT)
                        toaster.show();
                        //You could also send this location using coRoutine()
                        //coRoutine(loct)
                    }
                })

        }
    }

    //Exception handler for coRoutine() so that the app won't crash on unhandled exceptions
    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, _ ->
        //Handle error here
    }

    //Coroutine for starting the socket as a background service
    /**
     * coroutine to call for the sending socket
     * needed to run the network connection outside of main
     * otherwise fails
     */
    private fun coRoutine(msg: String){
            GlobalScope.launch(coroutineExceptionHandler) {
                val retVal = netSock.ncSend(msg)
        }
    }
}
