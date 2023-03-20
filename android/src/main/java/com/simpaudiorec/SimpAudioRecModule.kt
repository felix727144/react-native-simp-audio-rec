package com.simpaudiorec

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.AudioRecord
import android.media.AudioFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.common.logging.FLog
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.modules.core.PermissionListener
import java.io.IOException
import java.util.*
import kotlin.math.log10
var SAMPLE_RATE_IN_HZ=16000


fun ByteArray.toWritableArray(): WritableArray {
  val array: WritableArray = WritableNativeArray()

  this.forEach {
      array.pushInt(it.toInt())
  }

  return array
}

fun ReadableArray.toBytesArray(): ByteArray {
  val length = this.size()
  val result = ByteArray(length)

  for (i in 0 until length) {
      result[i] = this.getInt(i).toByte()
  }

  return result
}

class SimpAudioRecModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

  private var ar:AudioRecord?=null
  private var bs=SAMPLE_RATE_IN_HZ*2*160/1000;  
  private var recorderRunnable: Runnable? = null
  private var recordHandler: Handler? = Handler(Looper.getMainLooper())
  
  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    FLog.setMinimumLoggingLevel(FLog.VERBOSE)
    FLog.e(tag,"SampAudioRecModule,multiply")
    promise.resolve(a * b)
  }
  @ReactMethod
  fun checkPermission(promise: Promise) {
    
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // TIRAMISU (33)
        // https://github.com/hyochan/react-native-audio-recorder-player/issues/503
        if (Build.VERSION.SDK_INT < 33 &&
                (ActivityCompat.checkSelfPermission(
                    reactContext,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        reactContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED)
        ) {
          ActivityCompat.requestPermissions(
              (currentActivity)!!,
              arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
              0
          )
          promise.reject("No permission granted.", "Try again after adding permission.")
          return
        } else if (ActivityCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
          ActivityCompat.requestPermissions(
              (currentActivity)!!,
              arrayOf(Manifest.permission.RECORD_AUDIO),
              0
          )
          promise.reject("No permission granted.", "Try again after adding permission.")
          return
        }
      }
      promise.resolve("OK")
    } catch (ne: NullPointerException) {
      Log.w(tag, ne.toString())
      promise.reject("No permission granted.", "Try again after adding permission.")
      return
    }
  }
  @ReactMethod
  fun stop(promise: Promise) {
    Log.w(tag, "SimpAudioRecModule stop........")
    recorderRunnable?.let { 
      recordHandler!!.removeCallbacks(it) 
    };
    ar?.stop()
    ar=null
    promise.resolve("OK")
  }
  @ReactMethod
  fun start(promise: Promise) {
    
    ar=AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,  
          AudioFormat.CHANNEL_CONFIGURATION_MONO,  
          AudioFormat.ENCODING_PCM_16BIT, this.bs)
    val systemTime = SystemClock.elapsedRealtime()
    ar!!.startRecording()
    recorderRunnable=object:Runnable{
      override fun run() {
        if(ar==null){
          recordHandler!!.removeCallbacks(this) 
          return
        }
        val time = SystemClock.elapsedRealtime() - systemTime
        var buffer = ByteArray(bs)
        var rdsize=ar!!.read(buffer,0,bs)  
        //Log.w(tag, "recorderRunnable....")
        val obj = Arguments.createMap()
        obj.putArray("data",buffer.toWritableArray())
        obj.putInt("size",rdsize)
        obj.putDouble("currentPosition", time.toDouble())

        sendEvent(reactContext, "sar-back", obj)

        recordHandler!!.postDelayed(this, 10)
      }
    }
    (recorderRunnable as Runnable).run()
    promise.resolve("true")
  }

  private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
    reactContext
        .getJSModule<RCTDeviceEventEmitter>(RCTDeviceEventEmitter::class.java)
        .emit(eventName, params)
  }
  companion object {
    const val NAME = "SimpAudioRec"
    const val tag="SimpAudRecT"
  }
}
