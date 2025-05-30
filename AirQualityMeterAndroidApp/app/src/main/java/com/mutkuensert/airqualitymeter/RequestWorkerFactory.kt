package com.mutkuensert.airqualitymeter

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.mutkuensert.airqualitymeter.data.Repository

class RequestWorkerFactory(private val repository: Repository) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return RequestWorker(
            appContext,
            workerParameters,
            repository
        )
    }
}