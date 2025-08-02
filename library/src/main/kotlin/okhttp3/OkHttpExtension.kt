package okhttp3

import okio.BufferedSource


public val Headers.size: Int get() = size()

public val Request.url: HttpUrl get() = url()
public val Request.headers: Headers get() = headers()
public val Request.body: RequestBody? get() = body()
public val Request.method: String get() = method()

public val Response.headers: Headers get() = headers()
public val Response.body: ResponseBody? get() = body()
public val Response.request: Request get() = request()
public val Response.code: Int get() = code()
public val Response.sentRequestAtMillis: Long get() = sentRequestAtMillis()
public val Response.receivedResponseAtMillis: Long get() = receivedResponseAtMillis()
public val Response.protocol: Protocol get() = protocol()
public val Response.message: String get() = message()
public val Response.handshake: Handshake? get() = handshake()
public val Handshake.tlsVersion: TlsVersion get() = tlsVersion()
public val TlsVersion.javaName: String get() = javaName()
public val Handshake.cipherSuite: CipherSuite get() = cipherSuite()
public val CipherSuite.javaName: String get() = javaName()

public val HttpUrl.encodedPath: String get() = encodedPath()
public val HttpUrl.encodedPathSegments: List<String?> get() = encodedPathSegments()
public val HttpUrl.host: String get() = host()
public val HttpUrl.scheme: String get() = scheme()
public val HttpUrl.port: Int get() = port()
public val HttpUrl.querySize: Int get() = querySize()
public val HttpUrl.encodedQuery: String? get() = encodedQuery()
public val HttpUrl.pathSegments: List<String?> get() = pathSegments()
public val HttpUrl.query: String? get() = query()

public fun String.toHttpUrl(): HttpUrl = HttpUrl.get(this)
public fun String.toHttpUrlOrNull(): HttpUrl? = HttpUrl.get(this)

public fun BufferedSource.asResponseBody(
    contentType: MediaType? = null,
    contentLength: Long = -1L,
): ResponseBody =
    object : ResponseBody() {
        override fun contentType(): MediaType? = contentType

        override fun contentLength(): Long = contentLength

        override fun source(): BufferedSource = this@asResponseBody
    }
