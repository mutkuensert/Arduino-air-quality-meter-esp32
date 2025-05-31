package com.mutkuensert.airqualitymonitor.application

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.mutkuensert.airqualitymonitor.application.ApplicationState.CREATED
import com.mutkuensert.airqualitymonitor.application.ApplicationState.RESUMED
import com.mutkuensert.airqualitymonitor.application.ApplicationState.STARTED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApplicationLifecycleObserver : DefaultLifecycleObserver {
    private val _applicationState = MutableStateFlow(CREATED)
    val applicationState = _applicationState.asStateFlow()

    private val observers = mutableListOf<DefaultLifecycleObserver>()
    private val groundObservers = mutableListOf<AppGroundObserver>()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        _applicationState.value = CREATED
        observers.forEach { it.onCreate(owner) }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        _applicationState.value = STARTED
        observers.forEach { it.onStart(owner) }
        groundObservers.forEach { it.onAppForegrounded() }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        _applicationState.value = RESUMED
        observers.forEach { it.onResume(owner) }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        _applicationState.value = ApplicationState.PAUSED
        observers.forEach { it.onPause(owner) }
        groundObservers.forEach { it.onAppBackgrounded() }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        _applicationState.value = ApplicationState.STOPPED
        observers.forEach { it.onStop(owner) }
    }

    fun addObserver(observer: DefaultLifecycleObserver) {
        observers.add(observer)
    }

    fun addGroundObserver(observer: AppGroundObserver) {
        groundObservers.add(observer)
    }

    fun isAppForegrounded(): Boolean {
        return applicationState.value == CREATED
                || applicationState.value == STARTED
                || applicationState.value == RESUMED
    }
}

interface AppGroundObserver {
    fun onAppForegrounded() {}
    fun onAppBackgrounded() {}
}

enum class ApplicationState {
    CREATED, STARTED, RESUMED, PAUSED, STOPPED
}