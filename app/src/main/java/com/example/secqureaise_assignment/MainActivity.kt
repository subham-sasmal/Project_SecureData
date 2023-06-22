package com.example.secqureaise_assignment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    val fireStoreCollection = Firebase.firestore.collection("SecquirAiseData")
    private val datarefernce = Firebase.storage.reference

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var networkConnection: NetworkConnection

    private lateinit var currentDate: TextView
    private lateinit var currentTime: TextView
    private lateinit var frequencyNumberChange: TextView
    private lateinit var statusConnectivity: TextView
    private lateinit var chargingStatus: TextView
    private lateinit var batteryCharge: TextView
    private lateinit var locationLatitude: TextView
    private lateinit var locationLongitude: TextView
    private lateinit var openCamera: TextView
    private lateinit var imageviewPhoto: ImageView

    private var currentDateValue: String? = null
    private var currentTimeValue: String? = null
    private var captureCountNumber: Int? = null
    private var frequencyCountNumber: Int? = null
    private var connectivityStatusValue: String? = null
    private var chargingStatusValue: String? = null
    private var batteryPercentageNumber: Int? = null
    private var locationLongitudeNumber: String? = null
    private var locationLatitudeNumber: String? = null

    private lateinit var imageUri: Uri

    private var currentFile: Uri? = null
    private var file_Name = ""

    private lateinit var captureCount: TextView

    private lateinit var manualCheck: TextView

    private var count = 0

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        manualCheck = findViewById(R.id.btn_manual_refresh)
        currentDate = findViewById(R.id.tv_date_stamp)
        currentTime = findViewById(R.id.tv_time_stamp)
        frequencyNumberChange = findViewById(R.id.number_frequency)
        captureCount = findViewById(R.id.number_capture_count)
        statusConnectivity = findViewById(R.id.status_connectivity)
        chargingStatus = findViewById(R.id.status_battery_charging)
        batteryCharge = findViewById(R.id.number_battery_charge)
        locationLatitude = findViewById(R.id.number_location_latitude)
        locationLongitude = findViewById(R.id.number_location_longitude)
        openCamera = findViewById(R.id.btn_capture_image)
        imageviewPhoto = findViewById(R.id.imgv_captured_photo)


        frequencyNumberChange.setOnClickListener() {
            showDialogToChangeFrequency()
        }


        val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            imageviewPhoto.setImageURI(imageUri)
        }


        networkConnection = NetworkConnection(applicationContext)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //showDialogToChangeFrequency()


        val calendar = Calendar.getInstance().time
        val dateFormat = DateFormat.getDateInstance(DateFormat.FULL).format(calendar)
        val timeFormat = DateFormat.getTimeInstance().format(calendar)


        currentDate.text = dateFormat
        currentTime.text = timeFormat


        imageUri = createImageUri()!!
        openCamera.setOnClickListener() {
            contract.launch(imageUri)
        }

        findViewById<TextView>(R.id.btn_select_image).setOnClickListener() {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                resultLauncher.launch(it)
            }
        }


        networkConnection.observe(this, Observer { isAvailable ->
            when (isAvailable) {
                true -> findViewById<TextView>(R.id.status_connectivity).text = "ON"
                false -> findViewById<TextView>(R.id.status_connectivity).text = "OFF"
            }
        })

        registerReceiver(
            this.BatteryPercentageReciever,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        registerReceiver(
            this.BatteryChargingStatus,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )


        checkPermissionForLocationTracking()


        /*
            Data will be updated when the user manually presses the "Manual Data Refresh".
            For this we update on button click
         */
        manualCheck.setOnClickListener() {
            if (currentFile != null) {
                count++                             // count to keep the track of how many times the data is updated in a single session

                captureCount.text =
                    count.toString()            // setting the number of refresh in a single session


                /*
                    Using the Calendar and DateFormat classes to get the current time and date
                 */
                val calendar = Calendar.getInstance().time
                val dateFormat = DateFormat.getDateInstance(DateFormat.FULL).format(calendar)
                val timeFormat = DateFormat.getTimeInstance().format(calendar)


                currentDate.text = dateFormat
                currentTime.text = timeFormat


                /*
                    checking the networking availability and setting the values accordingly
                    In this we only check for wifi and cellular data
                 */
                networkConnection.observe(this, Observer { isAvailable ->
                    when (isAvailable) {
                        true -> findViewById<TextView>(R.id.status_connectivity).text = "ON"
                        false -> findViewById<TextView>(R.id.status_connectivity).text = "OFF"
                    }

                })

                registerReceiver(
                    this.BatteryPercentageReciever,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )

                registerReceiver(
                    this.BatteryChargingStatus,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )


                checkPermissionForLocationTracking()


                currentDateValue =
                    currentDate.text.toString()
                currentTimeValue =
                    currentTime.text.toString()
                captureCountNumber =
                    captureCount.text.toString().toInt()
                frequencyCountNumber =
                    frequencyNumberChange.text.toString().toInt()
                connectivityStatusValue =
                    statusConnectivity.text.toString()
                chargingStatusValue =
                    chargingStatus.text.toString()
                batteryPercentageNumber =
                    batteryCharge.text.toString().toInt()
                locationLongitudeNumber =
                    locationLongitude.text.toString()
                locationLatitudeNumber =
                    locationLatitude.text.toString()


                saveData(
                    SecqureaiseDetails(
                        currentDateValue!!,
                        currentTimeValue!!,
                        captureCountNumber!!,
                        frequencyCountNumber!!,
                        connectivityStatusValue!!,
                        chargingStatusValue!!,
                        batteryPercentageNumber!!,
                        locationLongitudeNumber!!,
                        locationLatitudeNumber!!
                    )
                )

                uploadImageToFirebase(file_Name)


                if (statusConnectivity.text == "OFF") {
                    Toast.makeText(
                        this@MainActivity,
                        "Data saved in the local database!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "Please select an image", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun createImageUri(): Uri? {
        val image = File(applicationContext.filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(
            applicationContext,
            "com.example.secqureaise_assignment.fileProvider", image
        )
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result?.data?.data?.let {
                    currentFile = it
                    file_Name = File(it.toString()).name
                    findViewById<ImageView>(R.id.imgv_captured_photo).setImageURI(it)
                }
            }
        }


    private fun uploadImageToFirebase(fileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (currentFile != null) {
                    datarefernce.child("Image/$fileName").putFile(currentFile!!).await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Successfully uploaded the image to the firebase storage!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please select an image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun checkPermissionForLocationTracking() {
        /*
            Here we check the location, if it is granted then we will proceed further or else we will have to grant the permission manually
         */
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )

            /*
                In this section we get the longitude and the latitude of the current location and then set it to their
                respective fields, i.e: latitude in the latitude field and the longitude in the longitude field
            */
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                val location = fusedLocationProviderClient.lastLocation
                location.addOnSuccessListener {
                    if (it != null) {
                        findViewById<TextView>(R.id.number_location_latitude).text =
                            it.latitude.toString()
                        findViewById<TextView>(R.id.number_location_longitude).text =
                            it.longitude.toString()
                    }
                }
            }

            return
        }
    }


    private fun showDialogToChangeFrequency() {
        val customDialogToChangeFrequency = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.frequency_change_layout, null, false)

        val builder = MaterialAlertDialogBuilder(this@MainActivity)
        builder.setView(customDialogToChangeFrequency)
            .setTitle("Enter the frequency for updating data (min)")
            .setPositiveButton("Set") {
                _, _ ->
            }
    }

    /*
        This function checks the battery percentage of the phone
     */
    private val BatteryPercentageReciever: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val batteryStatus: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                    context?.registerReceiver(null, ifilter)
                }

            val batteryLevel: Int? = batteryStatus?.let { intent ->
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

                level * 100 / scale
            }

            findViewById<TextView>(R.id.number_battery_charge).text = batteryLevel.toString()
        }
    }


    /*
        This function shows the charging status of the phone
     */
    private val BatteryChargingStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val batteryStatus: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                    baseContext.registerReceiver(null, ifilter)
                }

            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
                findViewById<TextView>(R.id.status_battery_charging).text = "ON"
            } else
                findViewById<TextView>(R.id.status_battery_charging).text = "OFF"
        }
    }


    /*
        This section of the code is used to add the values to the firebase by passing the object of the data class
        that contains all the data we need to store in the firebase fireStore
        We run the process of storing the data in the IO thread so that the UI does not get blocked'

        We also check if the data has been successfully stored in the firebase fireStore and if not then we will save the data
        in the local database.
     */
    private fun saveData(dataobj: SecqureaiseDetails) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                fireStoreCollection.add(dataobj)
                    /*
                        In this section if the data was successfully added to the firebase fireStore and then display a toast
                        message that let the users know that the data has been successfully uploaded to the firebase
                     */
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                this@MainActivity,
                                "Data saved to the firebase!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    /*
                        In this section we check if due to some reason the data is not uploaded to the firebase
                        and in that case we will save the data in the ROOM database (our local database)
                        And then we will display a toast message to the users that due to the network issues the data was not
                        been able to uploaded to the firebase firestore and instead it has been stored in the local database
                     */
                    .addOnFailureListener() {
                        val database = SecqurAIseDB.getDB(this@MainActivity)
                        GlobalScope.launch {
                            database.secqurAisedao().insertDetails(
                                SecqurAIseDatabaseDetails(
                                    captureCountNumber!!,
                                    frequencyCountNumber!!,
                                    connectivityStatusValue!!,
                                    chargingStatusValue!!,
                                    batteryPercentageNumber!!,
                                    locationLongitudeNumber!!,
                                    locationLatitudeNumber!!
                                )
                            )
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                this@MainActivity,
                                "Data saved in the local database!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}