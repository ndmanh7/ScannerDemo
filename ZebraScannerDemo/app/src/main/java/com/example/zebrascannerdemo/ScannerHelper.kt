package com.example.zebrascannerdemo

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsScannerEventsOnReLaunch
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.SDKHandler


class ScannerHelper(
    private val context: Context
) {

    var sdkHandler: SDKHandler? = null

    private val iDcsSdkApiDelegate = object : IDcsSdkApiDelegate {
        override fun dcssdkEventScannerAppeared(p0: DCSScannerInfo?) {

        }

        override fun dcssdkEventScannerDisappeared(p0: Int) {

        }

        override fun dcssdkEventCommunicationSessionEstablished(p0: DCSScannerInfo?) {

        }

        override fun dcssdkEventCommunicationSessionTerminated(p0: Int) {

        }

        override fun dcssdkEventBarcode(p0: ByteArray?, p1: Int, p2: Int) {

        }

        override fun dcssdkEventImage(p0: ByteArray?, p1: Int) {

        }

        override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) {

        }

        override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) {

        }

        override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) {

        }

        override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) {

        }

        override fun dcssdkEventConfigurationUpdate(p0: ConfigurationUpdateEvent?) {

        }

    }

    private val iDcsScannerEventsOnReLaunch = object : IDcsScannerEventsOnReLaunch {
        override fun onLastConnectedScannerDetect(p0: BluetoothDevice?): Boolean {
            TODO("Not yet implemented")
        }

        override fun onConnectingToLastConnectedScanner(p0: BluetoothDevice?) {
            TODO("Not yet implemented")
        }

        override fun onScannerDisconnect() {
            TODO("Not yet implemented")
        }

    }

    fun initializeDcsSdk() {
        if (sdkHandler == null) {
            sdkHandler = SDKHandler(context, true)
        }

        sdkHandler!!.dcssdkSetDelegate(iDcsSdkApiDelegate)
        sdkHandler!!.dcssdkEnableAvailableScannersDetection(true)


        // Bluetooth low energy mode.
        sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)


        // Bluetooth classic mode.
        sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)


        // SNAPI mode.
        sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI)

        var notificationsMask = 0


        // We would like to subscribe to all scanner available/not-available events.
//        notificationsMask =
//            notificationsMask or (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value)


        // We would like to subscribe to all scanner connection events.
        notificationsMask =
            notificationsMask or (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value)


        // We would like to subscribe to all barcode events.
        notificationsMask =
            notificationsMask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value


        // Subscribe to events set in notification mask.
        sdkHandler!!.dcssdkSubsribeForEvents(notificationsMask)


        //Subscribe to events set on Scanner Relaunch.
        sdkHandler!!.setiDcsScannerEventsOnReLaunch(iDcsScannerEventsOnReLaunch)
    }

    fun closeScanner() {
        if (sdkHandler != null) {
            sdkHandler!!.dcssdkClose()
            sdkHandler = null
        }
    }

}