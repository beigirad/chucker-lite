package okio

import okhttp3.internal.Util.checkOffsetAndCount
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


public typealias IOException = java.io.IOException

public fun Source.buffer(): BufferedSource = RealBufferedSource(this)
public val ForwardingSource.delegate: Source get() = delegate()

public fun OutputStream.sink(): Sink = OutputStreamSink(this, Timeout())
public val Buffer.size: Long get() = size()
public val ByteString.size: Int get() = size()
public fun Sink.buffer(): BufferedSink = RealBufferedSink(this)
public fun Buffer.write(byteString: ByteString, offset: Int, byteCount: Int): Buffer =
    buffer.write(byteString.toByteArray(), offset, byteCount)

public fun File.sink(append: Boolean = false): Sink = FileOutputStream(this, append).sink()
public fun File.source(): Source = InputStreamSource(inputStream(), Timeout.NONE)

public fun blackholeSink(): Sink = BlackholeSink()

public fun Source.gzip(): GzipSource = GzipSource(this)

public fun InputStream.source(): Source = InputStreamSource(this, Timeout())

private class OutputStreamSink(
    private val out: OutputStream,
    private val timeout: Timeout,
) : Sink {

    override fun write(source: Buffer, byteCount: Long) {
        checkOffsetAndCount(source.size, 0, byteCount)
        var remaining = byteCount
        while (remaining > 0) {
            timeout.throwIfReached()
            val head = source.head!!
            val toCopy = minOf(remaining, (head.limit - head.pos).toLong()).toInt()
            out.write(head.data, head.pos, toCopy)

            head.pos += toCopy
            remaining -= toCopy
            source.size -= toCopy

            if (head.pos == head.limit) {
                source.head = head.pop()
                SegmentPool.recycle(head)
            }
        }
    }

    override fun flush() = out.flush()

    override fun close() = out.close()

    override fun timeout() = timeout

    override fun toString() = "sink($out)"
}


private class BlackholeSink : Sink {
    override fun write(source: Buffer, byteCount: Long) = source.skip(byteCount)
    override fun flush() {}
    override fun timeout() = Timeout.NONE
    override fun close() {}
}

private open class InputStreamSource(
    private val input: InputStream,
    private val timeout: Timeout,
) : Source {

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (byteCount == 0L) return 0L
        require(byteCount >= 0L) { "byteCount < 0: $byteCount" }
        try {
            timeout.throwIfReached()
            val tail = sink.writableSegment(1)
            val maxToCopy = minOf(byteCount, (Segment.SIZE - tail.limit).toLong()).toInt()
            val bytesRead = input.read(tail.data, tail.limit, maxToCopy)
            if (bytesRead == -1) {
                if (tail.pos == tail.limit) {
                    // We allocated a tail segment, but didn't end up needing it. Recycle!
                    sink.head = tail.pop()
                    SegmentPool.recycle(tail)
                }
                return -1
            }
            tail.limit += bytesRead
            sink.size += bytesRead
            return bytesRead.toLong()
        } catch (e: AssertionError) {
            if (e.isAndroidGetsocknameError) throw IOException(e)
            throw e
        }
    }

    override fun close() = input.close()

    override fun timeout() = timeout

    override fun toString() = "source($input)"
}

internal val AssertionError.isAndroidGetsocknameError: Boolean get() {
    return cause != null && message?.contains("getsockname failed") ?: false
}
