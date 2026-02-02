package com.skul9x.danhvan

import android.app.Application
import android.content.Intent
import android.os.Process
import com.skul9x.danhvan.ui.crash.CrashActivity
import java.io.PrintWriter
import java.io.StringWriter

class DanhVanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Set global exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                handleUncaughtException(thread, throwable)
            } catch (e: Exception) {
                // If our handler fails, fall back to default
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val intent = Intent(this, CrashActivity::class.java).apply {
            putExtra("error_report", stackTrace)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)

        // Kill the process to ensure a clean state
        Process.killProcess(Process.myPid())
        System.exit(1)
    }
}
