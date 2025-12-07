import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

abstract class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    private val errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    private val boundary = "apiclient-${System.currentTimeMillis()}"

    override fun getBodyContentType(): String {
        return "multipart/form-data;boundary=$boundary"
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            // Add file data
            for ((key, dataPart) in getByteData()) {
                buildPart(dos, key, dataPart)
            }

            // Add string params
            for ((key, value) in getParams() ?: emptyMap()) {
                buildTextPart(dos, key, value)
            }

            dos.writeBytes("--$boundary--\r\n")
            return bos.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException("IOException writing to ByteArrayOutputStream", e)
        }
    }

    @Throws(AuthFailureError::class)
    abstract fun getByteData(): Map<String, DataPart>

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return try {
            Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    override fun deliverError(error: VolleyError) {
        errorListener.onErrorResponse(error)
    }

    private fun buildPart(dos: DataOutputStream, paramName: String, dataFile: DataPart) {
        try {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$paramName\"; filename=\"${dataFile.fileName}\"\r\n")
            dos.writeBytes("Content-Type: ${dataFile.type}\r\n\r\n")
            dos.write(dataFile.content)
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun buildTextPart(dos: DataOutputStream, paramName: String, value: String) {
        try {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$paramName\"\r\n\r\n")
            dos.writeBytes(value)
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String = "application/octet-stream"
    )
}
