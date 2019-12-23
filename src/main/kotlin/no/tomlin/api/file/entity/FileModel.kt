package no.tomlin.api.file.entity

data class FileModel(
    val path: String,
    val name: String,
    val short: String,
    val size: String,
    val type: String,
    val dir: Boolean,
    val preview: Boolean,
    val files: Int = 0
)
