package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import java.io.File
import java.io.FileWriter
import java.io.IOException
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

    fun Context.exportCSVInCustomDirectory(dataList: List<TbChargingHistory>, folderUri: Uri) {
        if (isExternalStorageWritable()) {
            try {
                val file = DocumentFile.fromTreeUri(this, folderUri)?.createFile("text/csv", generateFileNameWithTimestamp())
                val outputStream = this.contentResolver.openOutputStream(file?.uri!!)
                //outputStream?.write("some string".toByteArray())
                outputStream?.bufferedWriter().use { writer ->
                    // Write header
                    writer?.write("GunId,EV Mac Address,Charging Start Time, Charging End Time, Total Charging Time\n")
                    // Write data
                    dataList.forEach { model ->
                        writer?.write("${model.gunNumber},${model.evMacAddress},${model.chargingStartTime},${model.chargingEndTime},${model.totalChargingTime},\n")
                    }
                }
                outputStream?.close()
                Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Log.e(TAG, "External storage not writable")
        }
    }

    private fun generateFileNameWithTimestamp(prefix: String = "ccs2_", extension: String = "csv"): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentTimeStamp = dateFormat.format(Date())
        return "$prefix$currentTimeStamp.$extension"
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

}