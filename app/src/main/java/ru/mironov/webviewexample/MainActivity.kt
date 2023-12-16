package ru.mironov.webviewexample

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.mironov.webviewexample.databinding.ActivityMainBinding
import java.lang.Exception
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    private val scope = CoroutineScope(IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.webViewClient = CustomWebViewClient()
        binding.webView.webChromeClient = ChromeClient(scope)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.loadUrl("https://catfact.ninja/fact")
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

    class CustomWebViewClient() : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            // Show a loading indicator or perform any necessary actions
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
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