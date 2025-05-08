package com.example.onshapeheightmap

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var btnConnect: Button
    private lateinit var btnSave: Button
    private lateinit var tvStatus: TextView
    private lateinit var ivHeightmap: ImageView
    
    private var accessToken: String? = null
    private var heightmapBitmap: Bitmap? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        btnConnect = findViewById(R.id.btnConnect)
        btnSave = findViewById(R.id.btnSave)
        tvStatus = findViewById(R.id.tvStatus)
        ivHeightmap = findViewById(R.id.ivHeightmap)
        
        btnConnect.setOnClickListener {
            val authUrl = "https://oauth.onshape.com/oauth/authorize?" +
                "response_type=code&" +
                "client_id=steph.andreoli.11@gmail.com" + // Replace with your client ID
                "redirect_uri=onshapeheightmap://oauth"
            
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)))
        }
        
        btnSave.setOnClickListener {
            heightmapBitmap?.let { bitmap ->
                saveBitmapToStorage(bitmap)
            }
        }
        
        handleOAuthCallback(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleOAuthCallback(intent)
    }
    
    private fun handleOAuthCallback(intent: Intent?) {
        if (intent?.data?.scheme == "onshapeheightmap") {
            val code = intent.data?.getQueryParameter("code")
            if (code != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        accessToken = getAccessToken(code)
                        withContext(Dispatchers.Main) {
                            tvStatus.text = "Connected to Onshape"
                            btnSave.isEnabled = true
                            // Generate a sample heightmap (replace with actual Onshape integration)
                            generateSampleHeightmap()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            tvStatus.text = "Connection failed: ${e.message}"
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun getAccessToken(code: String): String {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", "steph.andreoli.11@gmail.com") // Replace with your client ID
            .add("client_secret", "Tarace78") // Replace with your client secret
            .add("redirect_uri", "onshapeheightmap://oauth")
            .add("code", code)
            .build()
            
        val request = Request.Builder()
            .url("https://oauth.onshape.com/oauth/token")
            .post(requestBody)
            .build()
            
        val response = client.newCall(request).execute()
        val json = JSONObject(response.body?.string() ?: "")
        return json.getString("access_token")
    }
    
    private fun generateSampleHeightmap() {
        val width = 300
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                // Simple heightmap pattern (replace with actual height calculation)
                val value = ((x * y) % 256).toInt()
                bitmap.setPixel(x, y, Color.rgb(value, value, value))
            }
        }
        
        ivHeightmap.setImageBitmap(bitmap)
        heightmapBitmap = bitmap
    }
    
    private fun saveBitmapToStorage(bitmap: Bitmap) {
        try {
            val file = File(getExternalFilesDir(null), "heightmap_${System.currentTimeMillis()}.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
            Toast.makeText(this, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
