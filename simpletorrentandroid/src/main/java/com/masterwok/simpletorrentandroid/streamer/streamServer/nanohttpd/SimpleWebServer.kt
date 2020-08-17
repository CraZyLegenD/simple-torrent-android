package com.masterwok.simpletorrentandroid.streamer.streamServer.nanohttpd

import java.io.*
import java.io.File.separatorChar
import java.net.URLEncoder
import java.util.*
import java.util.Collections.unmodifiableMap


/**
 * Created by hristijan on 9/17/19 to long live and prosper !
 */
open class SimpleWebServer @JvmOverloads constructor(host: String, port: Int, private val quiet: Boolean, private val cors: String? = null) : NanoHTTPD(host, port) {

    private var rootDirs: List<File>? = null

    val notFoundResponse: Response
        get() =
            newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404, file not found.")


    fun onDestory() {
        closeAllConnections()
    }

    private fun canServeUri(uri: String, homeDir: File?): Boolean {
        val canServeUri: Boolean
        val f = File(homeDir, uri)
        canServeUri = f.exists()
        return canServeUri
    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
     * instead of '+'.
     */
    private fun encodeUri(uri: String): String {
        var newUri = ""
        val st = StringTokenizer(uri, "/ ", true)
        while (st.hasMoreTokens()) {
            when (val tok = st.nextToken()) {
                "/" -> newUri += "/"
                " " -> newUri += "%20"
                else -> try {
                    newUri += URLEncoder.encode(tok, "UTF-8")
                } catch (ignored: UnsupportedEncodingException) {
                }
            }
        }
        return newUri
    }

    fun getForbiddenResponse(s: String): Response {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: $s")
    }

    protected fun getInternalErrorResponse(s: String): Response {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "INTERNAL ERROR: $s")
    }


    private fun respond(headers: Map<String, String>, session: IHTTPSession, uri: String): Response {
        // First let's handle CORS OPTION query
        var r: Response
        r = if (cors != null && Method.OPTIONS == session.method) {
            Response(Response.Status.OK, MIME_PLAINTEXT, null, 0)
        } else {
            defaultRespond(headers, uri)
        }

        if (cors != null) {
            r = addCORSHeaders(r, cors)
        }
        return r
    }

    private fun defaultRespond(headers: Map<String, String>, uri: String): Response {
        var child = uri
        // Remove URL arguments
        child = child.trim { it <= ' ' }.replace(separatorChar, '/')
        if (child.indexOf('?') >= 0) {
            child = child.substring(0, child.indexOf('?'))
        }

        // Prohibit getting out of current directory
        if (child.contains("../")) {
            return getForbiddenResponse("Won't serve ../ for security reasons.")
        }

        var canServeUri = false
        var homeDir: File? = null
        var i = 0
        while (!canServeUri && i < this.rootDirs!!.size) {
            homeDir = this.rootDirs!![i]
            canServeUri = canServeUri(child, homeDir)
            i++
        }
        if (!canServeUri) {
            return notFoundResponse
        }

        // Browsers get confused without '/' after the directory, send a
        // redirect.
        val f = File(homeDir, child)
        if (f.isDirectory && !child.endsWith("/")) {
            child += "/"
            val res = newFixedLengthResponse(Response.Status.REDIRECT, MIME_HTML, "<html><body>Redirected: <a href=\"$child\">$child</a></body></html>")
            res.addHeader("Location", child)
            return res
        }

        if (f.isDirectory) {
            return getForbiddenResponse("No directory listing.")
        }
        val mimeTypeForFile = getMimeTypeForFile(child)
        val response = serveFile(headers, f, mimeTypeForFile)
        return response ?: notFoundResponse
    }

    override fun serve(session: IHTTPSession): Response {
        val header = session.headers
        val parms = session.parameters
        val uri = session.uri

        if (!this.quiet) {
            println(session.method.toString() + " '" + uri + "' ")

            var e = header.keys.iterator()
            while (e.hasNext()) {
                val value = e.next()
                println("  HDR: '" + value + "' = '" + header[value] + "'")
            }
            e = parms.keys.iterator()
            while (e.hasNext()) {
                val value = e.next()
                println("  PRM: '" + value + "' = '" + parms[value] + "'")
            }
        }

        return respond(unmodifiableMap(header), session, uri)
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI,
     * ignores all headers and HTTP parameters.
     */

    fun serveFile(header: Map<String, String>, file: File, mime: String): Response {
        var res: Response
        try {
            // Calculate etag
            val etag = Integer.toHexString((file.absolutePath + file.lastModified() + "" + file.length()).hashCode())

            // Support (simple) skipping:
            var startFrom: Long = 0
            var endAt: Long = -1
            var range = header["range"]
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length)
                    val minus = range.indexOf('-')
                    try {
                        if (minus > 0) {
                            startFrom = java.lang.Long.parseLong(range.substring(0, minus))
                            endAt = java.lang.Long.parseLong(range.substring(minus + 1))
                        }
                    } catch (ignored: NumberFormatException) {
                    }

                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            val ifRange = header["if-range"]
            val headerIfRangeMissingOrMatching = ifRange == null || etag == ifRange

            val ifNoneMatch = header["if-none-match"]
            val headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*" == ifNoneMatch || ifNoneMatch == etag)

            // Change return code and add Content-Range header when skipping is
            // requested
            val fileLen = file.length()

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "")
                    res.addHeader("ETag", etag)
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1
                    }
                    var newLen = endAt - startFrom + 1
                    if (newLen < 0) {
                        newLen = 0
                    }

                    val fis = FileInputStream(file)
                    fis.skip(startFrom)

                    res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen)
                    res.addHeader("Accept-Ranges", "bytes")
                    res.addHeader("Content-Length", "" + newLen)
                    res.addHeader("Content-Range", "bytes $startFrom-$endAt/$fileLen")
                    res.addHeader("ETag", etag)
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "")
                    res.addHeader("Content-Range", "bytes */$fileLen")
                    res.addHeader("ETag", etag)
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "")
                    res.addHeader("ETag", etag)
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "")
                    res.addHeader("ETag", etag)
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime)
                    res.addHeader("Content-Length", "" + fileLen)
                    res.addHeader("ETag", etag)
                }
            }
        } catch (ioe: IOException) {
            res = getForbiddenResponse("Reading file failed.")
        }

        res.addHeader("Access-Control-Allow-Origin", cors)
        res.addHeader("Access-Control-Allow-Headers", calculateAllowHeaders())
        res.addHeader("Access-Control-Allow-Credentials", "true")
        res.addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS)
        res.addHeader("Access-Control-Max-Age", "" + MAX_AGE)
        res.addHeader("Access-Control-Allow-Headers", "X-Requested-With")
        res.addHeader("Access-Control-Allow-Headers", "Authorization")
        return res
    }

    @Throws(FileNotFoundException::class)
    protected fun newFixedFileResponse(file: File, mime: String): Response {
        val res: Response = newFixedLengthResponse(Response.Status.OK, mime, FileInputStream(file), (file.length()))
        res.addHeader("Accept-Ranges", "bytes")
        return res
    }

    private fun addCORSHeaders(resp: Response, cors: String): Response {
        resp.addHeader("Access-Control-Allow-Origin", cors)
        resp.addHeader("Access-Control-Allow-Headers", calculateAllowHeaders())
        resp.addHeader("Access-Control-Allow-Credentials", "true")
        resp.addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS)
        resp.addHeader("Access-Control-Max-Age", "" + MAX_AGE)
        resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With")
        resp.addHeader("Access-Control-Allow-Headers", "Authorization")
        resp.setChunkedTransfer(true)
        return resp
    }

    private fun calculateAllowHeaders(): String? {
        // here we should use the given asked headers
        // but NanoHttpd uses a Map whereas it is possible for requester to send
        // several time the same header
        // let's just use default values for this version
        return System.getProperty(ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME, DEFAULT_ALLOWED_HEADERS)
    }

    companion object {

        fun newFixedLengthResponse(status: Response.IStatus, mimeType: String, message: String): Response {
            val response = NanoHTTPD.newFixedLengthResponse(status, mimeType, message)
            response.addHeader("Accept-Ranges", "bytes")
            return response
        }

        private val ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD"

        private val MAX_AGE = 42 * 60 * 60

        // explicitly relax visibility to package for tests purposes
        internal val DEFAULT_ALLOWED_HEADERS = "origin,accept,content-type"

        val ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME = "AccessControlAllowHeader"
    }

}