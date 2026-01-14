package com.odtheking.odin.utils.skyblock.dungeon.tiles

import com.google.gson.annotations.SerializedName

enum class RoomShape(val displayName: String) {
    @SerializedName("Unknown")
    UNKNOWN("Unknown"),
    @SerializedName("L")
    L("L"),
    @SerializedName("1x1")
    S1x1("1x1"),
    @SerializedName("1x2")
    S2x1("1x2"),
    @SerializedName("1x3")
    S3x1("1x3"),
    @SerializedName("1x4")
    S4x1("1x4"),
    @SerializedName("2x2")
    S2x2("2x2");
}