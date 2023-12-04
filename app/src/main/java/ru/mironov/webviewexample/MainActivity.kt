package ru.mironov.webviewexample

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import ru.mironov.webviewexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(LOG_TAG, "create activity")
        binding = ActivityMainBinding.inflate(layoutInflater)
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.webViewClient = CustomWebViewClient()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl("http://www.google.com/")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.destroy()
    }

    class CustomWebViewClient() : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
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