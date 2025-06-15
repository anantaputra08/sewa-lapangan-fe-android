package com.example.booking_lapangan.api

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener,
    private val headers: Map<String, String>? = null,
    private val params: Map<String, String>? = null,
    private val fileParams: Map<String, DataPart>? = null
) : Request<NetworkResponse>(method, url, errorListener) {

    private val twoHyphens = "--"
    private val lineEnd = "\r\n"
    private val boundary = "apiclient-" + System.currentTimeMillis()

    override fun getHeaders(): MutableMap<String, String> {
        return headers?.toMutableMap() ?: super.getHeaders()
    }

    override fun getBodyContentType(): String {
        return "multipart/form-data;boundary=$boundary"
    }

    @Throws(IOException::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        try {
            // Tambahkan parameter teks
            params?.forEach { (key, value) ->
                buildTextPart(dos, key, value)
            }
            // Tambahkan parameter file
            fileParams?.forEach { (key, value) ->
                buildFilePart(dos, key, value)
            }
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
            return bos.toByteArray()
        } finally {
            dos.close()
            bos.close()
        }
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return try {
            Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: Exception) {
            Response.error(e as VolleyError?)
        }
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    @Throws(IOException::class)
    private fun buildTextPart(dos: DataOutputStream, key: String, value: String) {
        dos.writeBytes(twoHyphens + boundary + lineEnd)
        dos.writeBytes("Content-Disposition: form-data; name=\"$key\"$lineEnd")
        dos.writeBytes("Content-Type: text/plain; charset=UTF-8$lineEnd")
        dos.writeBytes(lineEnd)
        dos.writeBytes(value + lineEnd)
    }

    @Throws(IOException::class)
    private fun buildFilePart(dos: DataOutputStream, key: String, dataPart: DataPart) {
        dos.writeBytes(twoHyphens + boundary + lineEnd)
        dos.writeBytes("Content-Disposition: form-data; name=\"$key\"; filename=\"${dataPart.fileName}\"$lineEnd")
        dataPart.type?.let { dos.writeBytes("Content-Type: $it$lineEnd") }
        dos.writeBytes(lineEnd)
        dos.write(dataPart.content)
        dos.writeBytes(lineEnd)
    }

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String? = "image/jpeg"
    )
}
