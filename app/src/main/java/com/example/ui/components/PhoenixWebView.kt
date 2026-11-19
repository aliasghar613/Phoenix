package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.R
import com.example.util.UrlPolicy

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

class PhoenixWebController {
    internal var webView: WebView? = null
    internal var defaultUserAgent: String? = null

    fun goBack() {
        webView?.let { if (it.canGoBack()) it.goBack() }
    }

    fun goForward() {
        webView?.let { if (it.canGoForward()) it.goForward() }
    }

    fun reload() {
        webView?.reload()
    }

    fun loadHome() {
        webView?.loadUrl(UrlPolicy.BASE_URL)
    }

    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    fun clearCache() {
        webView?.clearCache(true)
        webView?.reload()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PhoenixWebView(
    controller: PhoenixWebController,
    desktopMode: Boolean,
    openExternalLinksOutside: Boolean,
    isLoading: Boolean,
    onPageStarted: (url: String, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageFinished: (url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onTitleReceived: (String?) -> Unit,
    onWebError: (failedUrl: String, errorCode: Int, description: String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryArgb = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceArgb = MaterialTheme.colorScheme.surface.toArgb()

    val currentOpenExternal by rememberUpdatedState(openExternalLinksOutside)
    val currentOnPageStarted by rememberUpdatedState(onPageStarted)
    val currentOnProgressChanged by rememberUpdatedState(onProgressChanged)
    val currentOnPageFinished by rememberUpdatedState(onPageFinished)
    val currentOnTitleReceived by rememberUpdatedState(onTitleReceived)
    val currentOnWebError by rememberUpdatedState(onWebError)
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)

    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var allowMultipleFiles by remember { mutableStateOf(false) }
    var isPullRefreshing by remember { mutableStateOf(false) }
    var lastAppliedDesktopMode by remember { mutableStateOf<Boolean?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val callback = pendingFileCallback
        pendingFileCallback = null
        if (callback != null) {
            if (uris.isEmpty()) {
                callback.onReceiveValue(null)
            } else {
                val selected = if (allowMultipleFiles) uris else uris.take(1)
                callback.onReceiveValue(selected.toTypedArray())
            }
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            isPullRefreshing = false
        }
    }

    LaunchedEffect(desktopMode) {
        val webView = controller.webView ?: return@LaunchedEffect
        if (lastAppliedDesktopMode != null && lastAppliedDesktopMode != desktopMode) {
            webView.settings.userAgentString = if (desktopMode) {
                DESKTOP_USER_AGENT
            } else {
                controller.defaultUserAgent
            }
            webView.reload()
        }
        lastAppliedDesktopMode = desktopMode
    }

    DisposableEffect(Unit) {
        onDispose {
            pendingFileCallback?.onReceiveValue(null)
            pendingFileCallback = null
            controller.webView?.destroy()
            controller.webView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val swipeRefresh = SwipeRefreshLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setColorSchemeColors(primaryArgb)
                setProgressBackgroundColorSchemeColor(surfaceArgb)
            }

            val webView = WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(surfaceArgb)

                controller.defaultUserAgent = settings.userAgentString
                lastAppliedDesktopMode = desktopMode

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    setSupportMultipleWindows(true)
                    javaScriptCanOpenWindowsAutomatically = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    allowFileAccess = false
                    allowContentAccess = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    if (desktopMode) {
                        userAgentString = DESKTOP_USER_AGENT
                    }
                }

                val currentWebView = this
                CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(currentWebView, true)
                }

                setDownloadListener { url, _, _, _, _ ->
                    if (!url.isNullOrBlank()) {
                        val launched = launchExternalUri(ctx, url)
                        currentOnShowMessage(
                            ctx.getString(
                                if (launched) R.string.msg_download_started
                                else R.string.msg_external_app_unavailable
                            )
                        )
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val uri = request?.url ?: return false
                        val url = uri.toString()

                        if (UrlPolicy.isExternalActionScheme(url)) {
                            val handled = handleSpecialScheme(ctx, url)
                            if (!handled) {
                                currentOnShowMessage(ctx.getString(R.string.msg_external_app_unavailable))
                            }
                            return true
                        }

                        if (currentOpenExternal && !UrlPolicy.isInternalPhoenixUrl(url)) {
                            val opened = launchExternalUri(ctx, url)
                            if (!opened) {
                                currentOnShowMessage(ctx.getString(R.string.msg_external_app_unavailable))
                            }
                            return true
                        }

                        return false
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        val resolvedUrl = url ?: UrlPolicy.BASE_URL
                        currentOnPageStarted(
                            resolvedUrl,
                            view?.canGoBack() == true,
                            view?.canGoForward() == true
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isPullRefreshing = false
                        val resolvedUrl = url ?: UrlPolicy.BASE_URL
                        currentOnPageFinished(
                            resolvedUrl,
                            view?.title,
                            view?.canGoBack() == true,
                            view?.canGoForward() == true
                        )
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            val desc = error?.description?.toString().orEmpty()
                            if (desc.contains("ERR_ABORTED", ignoreCase = true)) {
                                return
                            }
                            isPullRefreshing = false
                            currentOnWebError(
                                request.url?.toString() ?: UrlPolicy.BASE_URL,
                                error?.errorCode ?: -1,
                                desc.ifBlank { "Connection error" }
                            )
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        currentOnProgressChanged(newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        currentOnTitleReceived(title)
                    }

                    override fun onCreateWindow(
                        view: WebView?,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: Message?
                    ): Boolean {
                        val result = view?.hitTestResult
                        val targetUrl = result?.extra
                        if (!targetUrl.isNullOrBlank()) {
                            if (currentOpenExternal && !UrlPolicy.isInternalPhoenixUrl(targetUrl)) {
                                launchExternalUri(ctx, targetUrl)
                            } else {
                                view.loadUrl(targetUrl)
                            }
                            return true
                        }
                        return false
                    }

                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        pendingFileCallback?.onReceiveValue(null)
                        pendingFileCallback = filePathCallback
                        allowMultipleFiles =
                            fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE

                        val acceptTypes = fileChooserParams?.acceptTypes
                            ?.filter { it.isNotBlank() }
                            ?.toTypedArray()
                        val mimeTypes = if (acceptTypes.isNullOrEmpty()) {
                            arrayOf("*/*")
                        } else {
                            acceptTypes
                        }

                        return try {
                            filePickerLauncher.launch(mimeTypes)
                            true
                        } catch (_: ActivityNotFoundException) {
                            pendingFileCallback?.onReceiveValue(null)
                            pendingFileCallback = null
                            currentOnShowMessage(ctx.getString(R.string.msg_external_app_unavailable))
                            false
                        }
                    }
                }

                loadUrl(UrlPolicy.BASE_URL)
            }

            controller.webView = webView

            swipeRefresh.addView(webView)
            swipeRefresh.setOnChildScrollUpCallback { _, _ -> webView.scrollY > 0 }
            swipeRefresh.setOnRefreshListener {
                isPullRefreshing = true
                webView.reload()
            }

            swipeRefresh
        },
        update = { swipeRefresh ->
            swipeRefresh.setColorSchemeColors(primaryArgb)
            swipeRefresh.setProgressBackgroundColorSchemeColor(surfaceArgb)
            swipeRefresh.isRefreshing = isPullRefreshing && isLoading
            controller.webView?.setBackgroundColor(surfaceArgb)
        }
    )
}

fun launchExternalUri(context: Context, url: String): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}

private fun handleSpecialScheme(context: Context, url: String): Boolean {
    return try {
        val intent = if (url.startsWith("intent:", ignoreCase = true)) {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}
