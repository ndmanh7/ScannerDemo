package com.example.zebrascannerdemo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKManager.EMDKListener
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.BarcodeManager
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.ScanDataCollection.ScanData
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.ScannerException
import com.symbol.emdk.barcode.ScannerResults
import com.symbol.emdk.barcode.StatusData


class MainActivity : AppCompatActivity(), EMDKListener, Scanner.StatusListener, Scanner.DataListener {

    private var emdkManager: EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var scanner: Scanner? = null

    private lateinit var txtStatus: TextView
    private lateinit var edtData: TextView
    private lateinit var btnScan: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // view
        txtStatus = findViewById(R.id.txt_status)
        edtData = findViewById(R.id.edt_data)
        btnScan = findViewById(R.id.btn_scan)

        btnScan.setOnClickListener {
            try {
                if (scanner != null) {
                    if (scanner!!.isReadPending) {
                        scanner!!.cancelRead()
                    }
                    scanner!!.triggerType = Scanner.TriggerType.SOFT_ONCE;
                    scanner!!.read();
                }
            } catch (e: ScannerException) {
                updateStatus(e.message ?: "")
            }
        }

        val result = EMDKManager.getEMDKManager(applicationContext, this)
        if (result.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            updateStatus("EMDKManager object request failed!")
            return
        } else {
            updateStatus("EMDKManager object initialization is   in   progress.......")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager= null
        }
    }

    override fun onOpened(p0: EMDKManager?) {
        this.emdkManager = p0
        initBarcodeManager()
        initScanner()
    }

    override fun onClosed() {
        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager= null
        }
        updateStatus("EMDK closed unexpectedly! Please close and restart the application.");
    }

    override fun onStatus(p0: StatusData?) {
        val state = p0?.state
        var statusString = ""
        when (state) {
            StatusData.ScannerStates.IDLE -> {
                statusString = p0.friendlyName + " is   enabled and idle..."
                setConfig()
                try {
                    if (scanner != null) {
                        scanner!!.read()
                    }
                } catch (e: ScannerException) {
                    updateStatus(e.message ?: "")
                }
            }

            StatusData.ScannerStates.WAITING -> {
                statusString = "Scanner is waiting for trigger press..."
            }

            StatusData.ScannerStates.SCANNING -> {
                statusString = "Scanning..."
            }

            StatusData.ScannerStates.DISABLED -> {

            }

            StatusData.ScannerStates.ERROR -> {
                statusString = "An error has occurred."
            }

            else -> {

            }
        }
        updateStatus(statusString)
    }

    override fun onData(p0: ScanDataCollection?) {
        var dataStr = ""
        if ((p0 != null) && (p0.result === ScannerResults.SUCCESS)) {
            val scanData: ArrayList<ScanData> = p0.scanData
            // Iterate through scanned data and prepare the data.
            for (data in scanData) {
                // Get the scanned data
                val barcodeData = data.data
                // Get the type of label being scanned
                val labelType = data.labelType
                // Concatenate barcode data and label type
                dataStr = "$barcodeData  $labelType"
            }
            // Update EditText with scanned data and type of label on UI thread.
            Log.d("TAG", "onData: $dataStr")
            edtData.text = dataStr
//            updateData(dataStr)
        }
    }

    private fun setConfig() {
        if (scanner != null) {
            try {
                // Get scanner config
                val config = scanner!!.config;
                // Enable haptic feedback
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true;
                }
                // Set scanner config
                scanner!!.config = config;
            } catch (e: ScannerException) {
                updateStatus(e.message ?: "")
            }
        }
    }

    private var dataLength = 0
    private fun updateData(data: String) {
        runOnUiThread {
            edtData.text = data
        }
    }

    private fun updateStatus(status: String) {
        txtStatus.text = status
    }

    private fun initBarcodeManager() {
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager =
            emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
        // Add external scanner connection listener.
        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private fun initScanner() {
        if (scanner == null) {
            // Get default scanner defined on the device
            scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            if (scanner != null) {
                // Implement the DataListener interface and pass the pointer of this object to get the data callbacks.
                scanner!!.addDataListener(this)

                // Implement the StatusListener interface and pass the pointer of this object to get the status callbacks.
                scanner!!.addStatusListener(this)

                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                // NOTE: For devices without a hard trigger, use TriggerType.SOFT_ALWAYS.
                scanner!!.triggerType = Scanner.TriggerType.HARD

                try {
                    // Enable the scanner
                    // NOTE: After calling enable(), wait for IDLE status before calling other scanner APIs
                    // such as setConfig() or read().
                    scanner!!.enable()
                } catch (e: ScannerException) {
                    updateStatus(e.message!!)
                    deInitScanner()
                }
            } else {
                updateStatus("Failed to   initialize the scanner device.")
            }
        }
    }

    private fun deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner!!.release()
            } catch (e: Exception) {
                updateStatus(e.message!!)
            }
            scanner = null
        }
    }


}