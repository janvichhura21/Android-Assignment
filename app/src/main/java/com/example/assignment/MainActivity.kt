package com.example.assignment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.assignment.model.Phone
import com.example.assignment.network.ApiService
import com.example.assignment.network.ApiService.Companion.BASE_URL
import com.example.assignment.network.NetworkConnection
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0
    @SuppressLint("SuspiciousIndentation")
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var internet:Boolean = false
    var batteryLife:String=""
    var battery:Boolean=false
    var Location:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        save.setOnClickListener {
            getLocation()
            getNetwork()
            getBatteryPercent()
          //  loadIMEI()
            postDetails()
        }

    }

    private fun postDetails() {
        val retrofit=Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        val api=retrofit.create(ApiService::class.java)
      //  val phone=Phone(internet,batteryStatus,batteryLife,Location)
        val call=api.setPhone(internet,battery,batteryLife,Location)
        call.enqueue(object :Callback<Phone>{
            override fun onResponse(call: Call<Phone>, response: Response<Phone>) {
                Log.d("janvi",response.body().toString())
            }

            override fun onFailure(call: Call<Phone>, t: Throwable) {
                Log.d("janvi","error")
            }

        })
    }

    private fun getBatteryPercent() {
        val bm = this.getSystemService(BATTERY_SERVICE) as BatteryManager
        bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)


        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = this.registerReceiver(null, iFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level?.div(scale!!.toDouble())
        val b=(batteryPct!!*100).toInt()
        val order=batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1)
        when(order){
            BatteryManager.BATTERY_PLUGGED_USB->{
                battery=true
                charging_status.setText("true")
            }
            else -> {
                battery=false
                charging_status.setText("false")
            }
        }

        if (b in 71..95){
            bf.visibility=View.GONE
            b5.visibility=View.VISIBLE
            b3.visibility=View.GONE
            b1.visibility=View.GONE
            b0.visibility=View.GONE
        }
        else if(b in 41..69){
            bf.visibility=View.GONE
            b5.visibility=View.GONE
            b3.visibility=View.VISIBLE
            b1.visibility=View.GONE
            b0.visibility=View.GONE
        }
        else if(b in 21..39){
            bf.visibility=View.GONE
            b5.visibility=View.GONE
            b3.visibility=View.GONE
            b1.visibility=View.VISIBLE
            b0.visibility=View.GONE
        }
        else if(b in 11..19){
            bf.visibility=View.GONE
            b5.visibility=View.GONE
            b3.visibility=View.GONE
            b1.visibility=View.GONE
            b0.visibility=View.VISIBLE
        }
        else{
            bf.visibility=View.VISIBLE
            b5.visibility=View.GONE
            b3.visibility=View.GONE
            b1.visibility=View.GONE
            b0.visibility=View.GONE
        }
        batteryLife=b.toString()
        battery_level.setText(b.toString()+"%")
    }

    private fun getNetwork() {
        val networkConnection =NetworkConnection(applicationContext)
        networkConnection.observe(this, Observer { isConnected->

            if(isConnected){
                disconnected.visibility= View.GONE
                connected.visibility=View.VISIBLE
                internet=true
                txt.setText("Connected")
            }
            else{
                disconnected.visibility= View.VISIBLE
                connected.visibility=View.GONE
                internet=false
                txt.setText("Disconnected")
            }
        })
    }

    private fun getLocation(){
         if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
                 PackageManager.PERMISSION_GRANTED &&
                 ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
                 PackageManager.PERMISSION_GRANTED){
             ActivityCompat.requestPermissions(this,
             arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),100
             )

             return
         }

        val location = fusedLocationProviderClient.lastLocation
        location.addOnSuccessListener {
            if (it!=null){
                val textLatitude= "Latitude: " + it.latitude.toString()
                val textLongitude="Longitude: " + it.longitude.toString()
                Location= it.latitude.toString()
                sub_location.setText(textLatitude +"\n"+ textLongitude)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission()
        } else {
            // READ_PHONE_STATE permission is already been granted.
            doPermissionGrantedStuffs()
        }
    }
    private fun requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_PHONE_STATE
            )
        ) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            AlertDialog.Builder(this)
                .setTitle("Permission Request")
                .setCancelable(false)
                .setPositiveButton(
                    "Yes",
                    DialogInterface.OnClickListener { dialog, which -> //re-request
                        ActivityCompat.requestPermissions(
                            this, arrayOf(Manifest.permission.READ_PHONE_STATE),
                            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
                        )
                    })
                .show()
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_PHONE_STATE),
                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun doPermissionGrantedStuffs() {
        //Have an  object of TelephonyManager
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Get IMEI Number of Phone  //////////////// for this example i only need the IMEI
     //   val IMEINumber = tm.deviceId
     //   imei.setText(IMEINumber)
    }
}