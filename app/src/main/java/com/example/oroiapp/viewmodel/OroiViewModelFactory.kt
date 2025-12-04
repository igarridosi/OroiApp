package com.example.oroiapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.oroiapp.data.CancellationLinkDao
import com.example.oroiapp.data.SubscriptionDao
import com.example.oroiapp.data.UserPreferencesRepository

object OroiViewModelFactory : ViewModelProvider.Factory {

    lateinit var dao: SubscriptionDao
    lateinit var userPrefs: UserPreferencesRepository
    lateinit var cancellationDao: CancellationLinkDao

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        val savedStateHandle = extras.createSavedStateHandle()

        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(dao, userPrefs) as T
            }
            // ORDENA: Lehenengo 'application', gero 'dao'
            modelClass.isAssignableFrom(AddEditViewModel::class.java) -> {
                AddEditViewModel(application, dao, cancellationDao) as T
            }
            // ORDENA: Lehenengo 'application', gero 'dao', gero 'savedStateHandle'
            modelClass.isAssignableFrom(EditSubscriptionViewModel::class.java) -> {
                EditSubscriptionViewModel(dao, application, savedStateHandle) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}