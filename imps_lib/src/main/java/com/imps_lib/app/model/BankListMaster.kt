package com.imps_lib.app.model
import com.google.gson.annotations.SerializedName

data class BankListMaster(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BankData
)

data class BankData(
    @SerializedName("bankMaster") val bankMaster: ArrayList<BankMaster>,
    @SerializedName("metaData") val metaData: MetaData,
)

data class MetaData(
    @SerializedName("chargesAmount") val chargesAmount: Int?,
    @SerializedName("confirmationText") val confirmationText: String?,
)

data class BankMaster(
    @SerializedName("id") val id: Int?,
    @SerializedName("bank_name") val bankName: String?,
    @SerializedName("bank_code") val bankCode: String?,
    @SerializedName("bank_ifsc_code") val bankIfscCode: String?,
    @SerializedName("bank_ifsc") val bankIfsc: String?,
    @SerializedName("logo") val logo: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
){
    override fun toString(): String {
        return bankName ?: ""
    }
}
