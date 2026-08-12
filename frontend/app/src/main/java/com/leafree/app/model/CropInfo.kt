package com.leafree.app.model

data class CropInfo(
    val cropId: Long,
    val cropType: String,
    val cropImage: String,
    val createdAt : String,
    val harvestedAt : String?
)
