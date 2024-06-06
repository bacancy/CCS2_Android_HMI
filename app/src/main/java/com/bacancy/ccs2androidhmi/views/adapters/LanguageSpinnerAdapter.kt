package com.bacancy.ccs2androidhmi.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.models.Language

class LanguageSpinnerAdapter(
    context: Context,
    private val languageList: List<Language>
) :
    ArrayAdapter<Language>(context, 0, languageList) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    @SuppressLint("ViewHolder")
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        val language = languageList[position]
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item_language, parent, false)
        val textViewLanguageName = view.findViewById<TextView>(R.id.textViewLanguageName)
        textViewLanguageName.text = language.name
        return view
    }
}