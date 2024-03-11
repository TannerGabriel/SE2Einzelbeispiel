package aau.at.se2einzelbeispiel

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class MainActivity : ComponentActivity() {
    private lateinit var submitButton: Button
    private lateinit var matrikelNumberEditText: EditText
    private lateinit var serverResponseTextView: TextView
    private lateinit var calculateButton: Button

    private val serverAddress: String = "se2-submission.aau.at"
    private val serverPort: Int = 20080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        submitButton = findViewById(R.id.submitBtn)
        matrikelNumberEditText = findViewById(R.id.matrikelNumberEditText)
        serverResponseTextView = findViewById(R.id.serverResponseTextView)
        calculateButton = findViewById(R.id.calculateBtn)

        submitButton.setOnClickListener {
            val matrikelNumber = matrikelNumberEditText.text.toString()
            if (isValidMatrikelnumber(matrikelNumber)) {
                CoroutineScope(Dispatchers.IO).launch {
                    sendTcpRequest(matrikelNumber)
                }
            } else {
                serverResponseTextView.text = getString(R.string.enter_matrikel_number)
            }
        }

        calculateButton.setOnClickListener {
            val matrikelNumber: String = matrikelNumberEditText.text.toString()
            if (isValidMatrikelnumber(matrikelNumber)) {
                val result = sortMatrikelNumber(matrikelNumber)
                serverResponseTextView.text = result
            } else {
                serverResponseTextView.text = getString(R.string.enter_matrikel_number)
            }
        }
    }

    private suspend fun sendTcpRequest(matrikelNumber: String) {
        setUILoading(true);
        try {
            val result = withTimeoutOrNull(10000) {
                val socket: Socket = Socket()
                try {
                    socket.connect(InetSocketAddress(serverAddress, serverPort), 10000)
                    val out = PrintWriter(socket.getOutputStream(), true)
                    val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                    out.println(matrikelNumber)
                    val response = input.readLine()

                    // Return response
                    response
                } finally {
                    socket.close()
                }
            }

            withContext(Dispatchers.Main) {
                serverResponseTextView.text = result ?: "Connection timeout: Server did not respond in time."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                serverResponseTextView.text = "Error: ${e.message}"
            }
        } finally {
            setUILoading(false);
        }
    }

    private fun sortMatrikelNumber(number: String): String {
        // Convert to a list of its digits
        val digits = number.map(Character::getNumericValue)

        // Separate even and odd digits, then sort them
        val evenDigitsSorted = digits.filter { it % 2 == 0 }.sorted()
        val oddDigitsSorted = digits.filter { it % 2 != 0 }.sorted()

        // Combine sorted digits back into a single number
        val sortedDigits = evenDigitsSorted + oddDigitsSorted
        return sortedDigits.joinToString("")
    }

    private fun isValidMatrikelnumber(matrikelNumber: String): Boolean {
        // Check if the length is 8
        if (matrikelNumber.length != 8) return false

        // Check that all characters are digits
        if (!matrikelNumber.isDigitsOnly()) return false

        return true
    }

    private fun setUILoading(isLoading: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            submitButton.isEnabled = !isLoading
            calculateButton.isEnabled = !isLoading
            if (isLoading) {
                serverResponseTextView.text = "Loading..."
            }
        }
    }
}
