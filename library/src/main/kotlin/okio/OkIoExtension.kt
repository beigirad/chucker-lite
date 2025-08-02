package okio

import java.io.File
import java.io.InputStream

public typealias IOException = java.io.IOException

public fun Source.buffer(): BufferedSource = RealBufferedSource(this)
public val ForwardingSource.delegate: Source get() = delegate()

public val Buffer.size: Long get() = size()
public val ByteString.size: Int get() = size()
public fun Sink.buffer(): BufferedSink = RealBufferedSink(this)
public fun Buffer.write(byteString: ByteString, offset: Int, byteCount: Int): Buffer =
    buffer.write(byteString.toByteArray(), offset, byteCount)

public fun Source.gzip(): GzipSource = GzipSource(this)

public fun InputStream.source(): Source = Okio.source(this)

public fun File.sink(): Sink = Okio.sink(this)
public fun File.source(): Source = Okio.source(this.inputStream())


public fun blackholeSink(): Sink = BlackholeSinkBackport()
public class BlackholeSinkBackport : Sink {
    override fun write(source: Buffer, byteCount: Long): Unit = source.skip(byteCount)
    override fun flush() {}
    override fun timeout(): Timeout? = Timeout.NONE
    override fun close() {}
}
