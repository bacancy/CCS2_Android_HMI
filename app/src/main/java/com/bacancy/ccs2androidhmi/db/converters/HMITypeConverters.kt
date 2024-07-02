package com.bacancy.ccs2androidhmi.db.converters

import androidx.room.TypeConverter
import com.bacancy.ccs2androidhmi.db.model.ACMeterUserDefinedFields
import com.bacancy.ccs2androidhmi.db.model.DCMeterUserDefinedFields
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HMITypeConverters {

    private val gson = Gson()

    // Type converter for ACMeterUserDefinedFields
    @TypeConverter
    fun fromACMeterUserDefinedFields(fields: ACMeterUserDefinedFields): String {
        return gson.toJson(fields)
    }

    @TypeConverter
    fun toACMeterUserDefinedFields(data: String): ACMeterUserDefinedFields {
        val type = object : TypeToken<ACMeterUserDefinedFields>() {}.type
        return gson.fromJson(data, type)
    }

    // Type converter for DCMeterUserDefinedFields
    @TypeConverter
    fun fromDCMeterUserDefinedFields(fields: DCMeterUserDefinedFields): String {
        return gson.toJson(fields)
    }

    @TypeConverter
    fun toDCMeterUserDefinedFields(data: String): DCMeterUserDefinedFields {
        val type = object : TypeToken<DCMeterUserDefinedFields>() {}.type
        return gson.fromJson(data, type)
    }

}