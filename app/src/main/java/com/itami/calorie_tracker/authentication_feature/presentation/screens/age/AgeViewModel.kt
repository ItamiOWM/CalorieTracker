package com.itami.calorie_tracker.authentication_feature.presentation.screens.age

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itami.calorie_tracker.R
import com.itami.calorie_tracker.core.domain.exceptions.InvalidAgeException
import com.itami.calorie_tracker.core.domain.repository.UserManager
import com.itami.calorie_tracker.core.domain.use_case.SetAgeUseCase
import com.itami.calorie_tracker.core.utils.AppResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgeViewModel @Inject constructor(
    private val setAgeUseCase: SetAgeUseCase,
    private val userManager: UserManager,
    private val application: Application,
) : ViewModel() {

    private val _uiEvent = Channel<AgeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    var state by mutableStateOf(AgeState())
        private set

    init {
        getAge()
    }

    fun onEvent(event: AgeEvent) {
        when (event) {
            is AgeEvent.SaveAge -> {
                saveAge(age = state.age.toInt())
            }

            is AgeEvent.AgeValueChange -> {
                state = state.copy(age = event.age)
            }
        }
    }

    private fun sendUiEvent(uiEvent: AgeUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(uiEvent)
        }
    }

    private fun getAge() {
        viewModelScope.launch {
            val user = userManager.getUser()
            state = state.copy(age = user.age.toString())
        }
    }

    private fun saveAge(age: Int) {
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = setAgeUseCase(age = age)) {
                is AppResponse.Success -> {
                    sendUiEvent(uiEvent = AgeUiEvent.AgeSaved)
                }

                is AppResponse.Failed -> {
                    handleException(exception = result.exception, message = result.message)
                }
            }
            state = state.copy(isLoading = false)
        }
    }

    private fun handleException(exception: Exception, message: String?) {
        when (exception) {
            is InvalidAgeException -> {
                sendUiEvent(
                    uiEvent = AgeUiEvent.ShowSnackbar(
                        message = application.getString(R.string.error_invalid_age)
                    )
                )
            }

            else -> {
                sendUiEvent(
                    uiEvent = AgeUiEvent.ShowSnackbar(
                        message = message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }
}