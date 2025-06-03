package com.leafree.app.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.util.UUID

class CropImageRepository {

    /**
     * 서버로부터 받은 byte[] 이미지를 내부 저장소에 저장하고 URI로 반환
     */
    fun saveImageToInternalStorage(context: Context, imageBytes: ByteArray): Uri {
        val uniqueFileName = "crop_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, uniqueFileName)
        file.writeBytes(imageBytes)
        return file.toUri()
    }

    /**
     * 저장된 이미지 파일을 직접 삭제하거나 정리할 수 있도록 헬퍼 함수 제공 (옵션)
     */
    fun deleteImageFile(context: Context, fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * 저장된 파일의 전체 목록을 확인하거나 디버깅용으로 사용 가능
     */
    fun listSavedImages(context: Context): List<File> {
        return context.filesDir.listFiles()?.toList() ?: emptyList()
    }
}
