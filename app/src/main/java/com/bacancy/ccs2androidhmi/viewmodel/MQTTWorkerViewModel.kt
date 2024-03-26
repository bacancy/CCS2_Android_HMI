package com.bacancy.ccs2androidhmi.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MQTTWorkerViewModel @Inject constructor() : ViewModel() {

    private val _publishMessageRequest = MutableStateFlow("" to "")

    val publishMessageRequest = _publishMessageRequest.asStateFlow()

    fun sendPublishMessageRequest(request: Pair<String, String>){
        _publishMessageRequest.value = request
    }
}