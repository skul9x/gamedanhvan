package com.skul9x.danhvan.util

import android.content.Context
import android.media.MediaPlayer

/**
 * Singleton manager for playing sound effects.
 * Prevents resource leak by:
 * 1. Stopping/releasing previous player before starting new one
 * 2. Auto-releasing on completion
 */
object SoundManager {
    private var currentPlayer: MediaPlayer? = null

    /**
     * Play a sound resource. Cancels any currently playing sound first.
     */
    fun play(context: Context, resId: Int) {
        try {
            // Cancel previous sound to prevent resource leak
            currentPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            
            currentPlayer = MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { 
                    it.release()
                    currentPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Release any held resources. Call this when the screen is disposed.
     */
    fun release() {
        try {
            currentPlayer?.release()
            currentPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
