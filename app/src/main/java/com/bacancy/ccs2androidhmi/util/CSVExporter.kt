package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.util.CommonUtils.FILE_NAME_DATE_TIME_FORMAT
import com.bacancy.ccs2androidhmi.util.CommonUtils.FILE_NAME_EXTENSION
import com.bacancy.ccs2androidhmi.util.CommonUtils.FILE_NAME_PREFIX
import com.bacancy.ccs2androidhmi.util.ToastUtils.showCustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CSVExporter {

    private const val TAG = "CSVExporter"

    // Method to export data to CSV and save it in internal storage
    fun exportCSVInDownloadsDirectory(context: Context, dataList: List<TbChargingHistory>) {
        // Check if the external storage is writable
        if (isExternalStorageWritable()) {
            // Get the root directory of the external storage
            val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (!exportDir?.exists()!!) {
                exportDir.mkdirs()
            }

            // Create a new file for the CSV
            val file = File(exportDir, "charging_history.csv")
            try {
                // Create a FileWriter object to write to the CSV file
                val writer = FileWriter(file)

                // Write the header row to the CSV file
                writer.append("Column1,Column2,Column3,Column4\n")

                // Iterate through the list of data objects and write each row to the CSV file
                for (data in dataList) {
                    writer.append("${data.evMacAddress}, ${data.totalChargingTime}, ${data.chargingStartTime}, ${data.chargingEndTime}\n")
                }

                // Close the FileWriter object
                writer.flush()
                writer.close()

                // Log success message
                Log.d(TAG, "CSV file saved successfully")

            } catch (e: IOException) {
                Log.e(TAG, "Error writing CSV file", e)
            }
        } else {
            // Log error if external storage is not writable
            Log.e(TAG, "External storage not writable")
        }
    }

    suspend fun Context.exportCSVInCustomDirectory(
        dataList: List<TbChargingHistory>,
        folderUri: Uri
    ) {
        withContext(Dispatchers.IO) {
            if (isExternalStorageWritable()) {
                try {
                    val file = DocumentFile.fromTreeUri(this@exportCSVInCustomDirectory, folderUri)
                        ?.createFile(getString(R.string.csv_mime_type), generateFileNameWithTimestamp())

                    file?.let { documentFile ->
                        contentResolver.openOutputStream(documentFile.uri)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                val csvStringBuilder = StringBuilder()
                                csvStringBuilder.appendLine(getString(R.string.csv_row_headers))

                                // Batch writing data
                                dataList.chunked(100) { batch ->
                                    batch.forEach { model ->
                                        val line =
                                            "${model.summaryId},${model.gunNumber},${model.evMacAddress},${model.chargingStartTime},${model.chargingEndTime},${model.totalChargingTime},${model.startSoc},${model.endSoc},${model.energyConsumption},${model.sessionEndReason}"
                                        csvStringBuilder.appendLine(line)
                                    }
                                    writer.write(csvStringBuilder.toString())
                                    csvStringBuilder.clear()
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            this@exportCSVInCustomDirectory.showCustomToast(
                                getString(R.string.msg_file_saved_successfully),
                                true
                            )
                        }
                    } ?: withContext(Dispatchers.Main) {
                        this@exportCSVInCustomDirectory.showCustomToast(
                            getString(R.string.msg_failed_to_create_file),
                            false
                        )
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "IOException: ${e.message}")
                    withContext(Dispatchers.Main) {
                        this@exportCSVInCustomDirectory.showCustomToast(
                            getString(R.string.msg_failed_to_save_file),
                            false
                        )
                    }
                }
            } else {
                Log.e(TAG, "External storage not writable")
                withContext(Dispatchers.Main) {
                    this@exportCSVInCustomDirectory.showCustomToast(
                        getString(R.string.msg_external_storage_not_writable),
                        false
                    )
                }
            }
        }
    }

    private fun generateFileNameWithTimestamp(
        prefix: String = FILE_NAME_PREFIX,
        extension: String = FILE_NAME_EXTENSION
    ): String {
        val dateFormat = SimpleDateFormat(FILE_NAME_DATE_TIME_FORMAT, Locale.getDefault())
        val currentTimeStamp = dateFormat.format(Date())
        return "$prefix$currentTimeStamp.$extension"
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

}