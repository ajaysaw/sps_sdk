package com.imps_lib.app.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import com.google.gson.annotations.SerializedName

data class BeneficiaryListResult(
    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: ArrayList<BeneficiaryData> = arrayListOf()
) : Parcelable {  // Implement Parcelable

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(), // Convert byte to Boolean
        parcel.readString(),
        parcel.createTypedArrayList(BeneficiaryData.CREATOR) ?: arrayListOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (status == true) 1 else 0)
        parcel.writeString(message)
        parcel.writeTypedList(data)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BeneficiaryListResult> {
        override fun createFromParcel(parcel: Parcel): BeneficiaryListResult {
            return BeneficiaryListResult(parcel)
        }

        override fun newArray(size: Int): Array<BeneficiaryListResult?> {
            return arrayOfNulls(size)
        }
    }
}


data class BeneficiaryData(
    @SerializedName("beneficiaryCode") var beneficiaryCode: String? = "",
    @SerializedName("beneficiaryName") var beneficiaryName: String? = "",
    @SerializedName("beneficiaryType") var beneficiaryType: String? = "",
    @SerializedName("bankName") var bankName: String? = "",
    @SerializedName("accountNumber") var accountNumber: String? = "",
    @SerializedName("accountType") var accountType: String? = "",
    @SerializedName("ifsc") var ifsc: String? = "",
    @SerializedName("status") var status: String? = "",
    @SerializedName("created") var created: String? = "",
    @SerializedName("isVerified") var isVerified: String? = "",
    @SerializedName("isCoolingPeriodPassed") var isCoolingPeriodPassed: Boolean? = false,
    @SerializedName("remainingCoolingPeriodInSeconds") var remainingCoolingPeriodInSeconds: Int? = 0
) : Parcelable {  // Implement Parcelable

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),  // Convert byte to Boolean
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(beneficiaryCode)
        parcel.writeString(beneficiaryName)
        parcel.writeString(beneficiaryType)
        parcel.writeString(bankName)
        parcel.writeString(accountNumber)
        parcel.writeString(accountType)
        parcel.writeString(ifsc)
        parcel.writeString(status)
        parcel.writeString(created)
        parcel.writeString(isVerified)
        parcel.writeByte(if (isCoolingPeriodPassed == true) 1 else 0)
        parcel.writeInt(remainingCoolingPeriodInSeconds ?: 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BeneficiaryData> {
        override fun createFromParcel(parcel: Parcel): BeneficiaryData {
            return BeneficiaryData(parcel)
        }

        override fun newArray(size: Int): Array<BeneficiaryData?> {
            return arrayOfNulls(size)
        }
    }
}
