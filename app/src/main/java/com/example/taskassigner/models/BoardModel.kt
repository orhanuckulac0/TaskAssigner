package com.example.taskassigner.models

import android.os.Parcel
import android.os.Parcelable

data class BoardModel(
    var name: String = "",
    var image: String = "",
    var createdBy: String = "",
    var assignedTo: ArrayList<String> = ArrayList()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BoardModel> {
        override fun createFromParcel(parcel: Parcel): BoardModel {
            return BoardModel(parcel)
        }

        override fun newArray(size: Int): Array<BoardModel?> {
            return arrayOfNulls(size)
        }
    }
}