# SuspiciousApp
Kotlin app for Android phones to have a persistent app running even while the screen is locked and sending the device's location data, id and timestamp to your server.

I have no experience in App dev, so the code might be messy and illogical at times. Mostly reusing code scoured from the web and thanking the individuals accordingly in code comments.

Done for my thesis to demonstrate as a somewhat of an Android malware.

java/com.example.suspiciousapp/netSock contains netSock Kotlin object<br>
java/com.example.suspiciousapp/snoopLocation contains snoopLocation.kt class and snoopLocationService.kt service<br>
java/com.example.suspiciousapp/ contains in addition to above mentioned also the FirstFragment.kt and MainActivity.kt

Rest of the files are in their default locations.

Just run the app, give location permissions (the "always allow") and the app starts running. If you actually open the app you can get your current location by pushing the button and display the coordinates, though the app is already unbeknownst to the user acquiring the location coordinates every minute and sending them to the server you can define in netSock.kt. The app is sticky and should be quit resilient to die. 

The data can be received for example with netcat, for example "ncat -kl -p 9999 -o ncatout --recv-only" ncatout being the file to which the data will be written and remember to adjust the port accordingly in the app netSock.kt script before deployment.
