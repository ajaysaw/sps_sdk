package payworld.com.aeps_lib.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ApiResponse<T>(
    @SerializedName("status")
    val status: Int?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("error_message")
    val errorMessage: String?,

    @SerializedName("error_code")
    val errorCode: Int?,

    @SerializedName("data")
    val data: T?

) : Serializable