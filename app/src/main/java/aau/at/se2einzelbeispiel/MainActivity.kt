package aau.at.se2einzelbeispiel

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket

class MainActivity : ComponentActivity() {
    private lateinit var submitButton: Button
    private lateinit var matrikelNumberEditText: EditText
    private lateinit var serverResponseTextView: TextView

    private val serverAddress: String = "se2-submission.aau.at"
    private val serverPort: Int = 20080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        submitButton = findViewById(R.id.submitBtn)
        matrikelNumberEditText = findViewById(R.id.matrikelNumberEditText)
        serverResponseTextView = findViewById(R.id.serverResponseTextView)

        submitButton.setOnClickListener {
            val matrikelNumber = matrikelNumberEditText.text.toString()
            CoroutineScope(Dispatchers.IO).launch {
                sendTcpRequest(matrikelNumber)
            }
        }
    }

    private suspend fun sendTcpRequest(matriculationNumber: String) {
        try {
            val socket = Socket(InetAddress.getByName(serverAddress), serverPort)
            val out = PrintWriter(socket.getOutputStream(), true)
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))

            out.println(matriculationNumber)
            val response = input.readLine()

            withContext(Dispatchers.Main) {
                serverResponseTextView.text = response
            }

            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                serverResponseTextView.text = "Error: ${e.message}"
            }
        }
    }

}
