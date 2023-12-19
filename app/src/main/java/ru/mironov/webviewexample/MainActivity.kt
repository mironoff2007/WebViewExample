package ru.mironov.webviewexample

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.mironov.webviewexample.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    private val scope = CoroutineScope(IO)

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val customClient = CustomWebViewClient()
        //subscribe(customClient)
        binding.webView.webViewClient = customClient
        //binding.webView.webChromeClient = ChromeClient(scope)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true

        binding.webView.addJavascriptInterface(MyJavaScriptInterface(), "HTMLOUT")

        binding.webView.loadUrl("https://catfact.ninja/fact")
    }

    private fun subscribe(customClient: CustomWebViewClient) {
        if (job?.isActive == true) {
            job?.cancel()
        }
        job = scope.launch {
            customClient.collback.collectLatest { state ->
                when (state) {
                    CustomWebViewClient.State.PAGE_FINISHED -> {
                        scope.launch(Main) {
                            binding.webView.evaluateJavascript(
                                "javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');",
                                null
                            );
                        }
                    }

                    else -> {}
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.destroy()
        scope.cancel()
    }

    class ChromeClient(private val scope: CoroutineScope) : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String?) {
            super.onReceivedTitle(view, title)
            val url = URL(view.url)
            scope.launch {
                try {
                    val conn = url.openConnection()
                    conn.connect()
                    val stream = conn.getInputStream()
                    conn.headerFields.forEach { headerName, headerValue ->
                        Log.d(LOG_TAG, "$headerName - $headerValue")
                    }
                    val stringBuilder = StringBuilder()
                    stream.bufferedReader().use { reader ->
                        stringBuilder.append(reader.readText())
                    }
                    val result = stringBuilder.toString()
                    Log.d(LOG_TAG, result)
                }
               catch (e: Exception) {
                   Log.e(LOG_TAG, e.stackTraceToString())
               }
            }
        }
    }

    // JavaScript interface class
    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        fun processHTML(html: String) {
            // Parse JSON data from the HTML string
            try {
                // Access JSON data as needed
                Log.d(LOG_TAG, html)
            } catch (e:Exception) {
                Log.e(LOG_TAG,e.stackTraceToString())
            }
        }
    }
    class CustomWebViewClient() : WebViewClient() {

        enum class State {
            PAGE_FINISHED;
        }

        private val _collback: MutableStateFlow<State?> = MutableStateFlow(null)
        val collback: StateFlow<State?> = _collback.asStateFlow()

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest
        ): WebResourceResponse? {
            super.shouldInterceptRequest(view, request)
            // Check if the requested URL is the JSON endpoint you want to intercept
            val isMainThread = Looper.myLooper() == Looper.getMainLooper()
            Log.d(LOG_TAG, "shouldInterceptRequest -> is main thread - $isMainThread")
            if (true) {
                // Load the URL and get the JSON response
                try {
                    val connection =
                        URL(request.url.toString()).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    // Read the response and convert it to a String
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = java.lang.StringBuilder()
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    bufferedReader.close()
                    inputStream.close()

                    // Convert the JSON response to a WebResourceResponse
                    val json = stringBuilder.toString()
                    Log.e(LOG_TAG,json)
                    val byteArrayInputStream = ByteArrayInputStream("ok".toByteArray())
                    return WebResourceResponse("application/json", "UTF-8", byteArrayInputStream)
                } catch (e: IOException) {
                    Log.e(LOG_TAG,e.stackTraceToString())
                }
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            // Show a loading indicator or perform any necessary actions
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            _collback.tryEmit(State.PAGE_FINISHED)
            // Page has finished loading, perform any necessary actions
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            // Log or handle the error
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            // Log or handle the error
        }

    }

    companion object {
        private const val LOG_TAG = "WebViewActivity"
    }
}