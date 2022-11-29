package com.example.headsup

import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Surface
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var timerLayout: LinearLayout
    private lateinit var buttonLayout: LinearLayout
    private lateinit var celebLayout: LinearLayout

    private lateinit var tvTime: TextView
    private lateinit var tvName: TextView
    private lateinit var tvTaboo1: TextView
    private lateinit var tvTaboo2: TextView
    private lateinit var tvTaboo3: TextView
    private lateinit var tvMain: TextView

    private lateinit var btStart: Button

    private var gameOn = false
    private lateinit var celebrities: ArrayList<JSONObject>
    private var celeb = 0

    var rotationPortrait = Surface.ROTATION_0
    var rotateLandscapeRight = Surface.ROTATION_180
    var rotateLandscapeLeft = Surface.ROTATION_270

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerLayout = findViewById(R.id.llTop)
        buttonLayout = findViewById(R.id.llMain)
        celebLayout = findViewById(R.id.llCelebrity)

        tvTime = findViewById(R.id.tvTime)

        tvName = findViewById(R.id.tvName)
        tvTaboo1 = findViewById(R.id.tvTaboo1)
        tvTaboo2 = findViewById(R.id.tvTaboo2)
        tvTaboo3 = findViewById(R.id.tvTaboo3)
        tvMain = findViewById(R.id.tvMain)

        btStart = findViewById(R.id.btStart)
        btStart.setOnClickListener { requestAPI() }

        celebrities = arrayListOf()

        /////////////////////////////////////////////////////
        ////////////////ANIMATION CREATION//////////////////
        ////////////////////////////////////////////////////
        val main =AnimationUtils.loadAnimation(this,R.anim.title_anim)
        tvMain.startAnimation(main)
        /////////////////////////////////////////////////////
        ////////////////////////////////////////////////////
        ////////////////////////////////////////////////////
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Get the screen orientation
        val rotation = newConfig.orientation
        Log.d("test", "onConfigurationChanged:$rotation ")

        if(rotation == rotationPortrait || rotation == rotateLandscapeRight || rotation == rotateLandscapeLeft){

                if (gameOn) {
                    updateLayoutVisibility(true)
                } else {
                    updateLayoutVisibility(false)
                }
        }else{
            if(gameOn){
                celeb++
                newCelebrity(celeb)
                updateLayoutVisibility(false)
            }else{
                updateLayoutVisibility(false)
            }
        }
    }

    private fun newTimer(){
        if(!gameOn){
            gameOn = true
            tvMain.text = "Please Rotate Device"
            btStart.isVisible = false
            val rotation = windowManager.defaultDisplay.rotation
            if(rotation == rotationPortrait || rotation == rotateLandscapeRight || rotation == rotateLandscapeLeft){
                updateLayoutVisibility(false)
            }else{
                updateLayoutVisibility(true)
            }

            // Show 60 second countdown in a text field
            object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                        tvTime.text = "Time: ${millisUntilFinished / 1000}"

                    //make timer red when its less than 10 seconds
                        if (millisUntilFinished < 10000) {
                            tvTime.setTextColor(Color.parseColor("#AF0000"))
                        }
                    }

                override fun onFinish() {
                    gameOn = false
                    tvTime.text = "Time: --"
                    tvMain.text = "Heads Up!"
                    btStart.isVisible = true
                    updateLayoutVisibility(false)
                }
            }.start()
        }
    }

    private fun newCelebrity(id: Int){
        if(id < celebrities.size){
            tvName.text = celebrities[id].getString("name")
            tvTaboo1.text = celebrities[id].getString("taboo1")
            tvTaboo2.text = celebrities[id].getString("taboo2")
            tvTaboo3.text = celebrities[id].getString("taboo3")
        }
    }

    // Create coroutine
    private fun requestAPI(){
        CoroutineScope(Dispatchers.IO).launch {
            val data = async {
                getCelebrities()
            }.await()
            if(data.isNotEmpty()){

                // bringing the background thread to the main thread
                withContext(Main){
                    parseJSON(data)
                    celebrities.shuffle()
                    newCelebrity(0)
                    newTimer()
                }
            }else{
                Toast.makeText(this@MainActivity, "something went wrong!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun parseJSON(result: String){
        withContext(Dispatchers.Main){
            celebrities.clear()
            val celebArray = JSONArray(result)
            for(i in 0 until celebArray.length()){
                celebrities.add(celebArray.getJSONObject(i))
            }
        }
    }

    private fun getCelebrities(): String{
        var response = ""

        try {
            response = URL("https://apidojo.pythonanywhere.com/celebrities/")
                .readText(Charsets.UTF_8)

        // Catch and update the UI if an exception is thrown
        }catch (e: Exception){
            println("Error: $e")
        }
        return response
    }

    private fun updateLayoutVisibility(showCelebrity: Boolean) {
        if (showCelebrity) {
            celebLayout.isVisible = true
            buttonLayout.isVisible = false
        } else {
            celebLayout.isVisible = false
            buttonLayout.isVisible = true
        }
    }
}