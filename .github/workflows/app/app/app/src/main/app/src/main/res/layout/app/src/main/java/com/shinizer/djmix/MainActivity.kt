package com.shinizer.djmix

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var playerA: ExoPlayer
    private lateinit var playerB: ExoPlayer
    private lateinit var playerViewA: PlayerView
    private lateinit var playerViewB: PlayerView
    private lateinit var tvTrackA: TextView
    private lateinit var tvTrackB: TextView
    private lateinit var tvCrossfader: TextView

    private val pickMediaA = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadMediaToDeck(isDeckA = true, uri = it, label = it.lastPathSegment ?: "Local File A") }
    }

    private val pickMediaB = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadMediaToDeck(isDeckA = false, uri = it, label = it.lastPathSegment ?: "Local File B") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerViewA = findViewById(R.id.playerViewA)
        playerViewB = findViewById(R.id.playerViewB)
        tvTrackA = findViewById(R.id.tvTrackA)
        tvTrackB = findViewById(R.id.tvTrackB)
        tvCrossfader = findViewById(R.id.tvCrossfader)

        playerA = ExoPlayer.Builder(this).build()
        playerB = ExoPlayer.Builder(this).build()

        playerViewA.player = playerA
        playerViewB.player = playerB

        playerA.volume = 1.0f
        playerB.volume = 0.0f

        findViewById<Button>(R.id.btnLoadLocalA).setOnClickListener { pickMediaA.launch("*/*") }
        findViewById<Button>(R.id.btnLoadLocalB).setOnClickListener { pickMediaB.launch("*/*") }

        findViewById<Button>(R.id.btnLoadUrlA).setOnClickListener { showOnlineUrlDialog(isDeckA = true) }
        findViewById<Button>(R.id.btnLoadUrlB).setOnClickListener { showOnlineUrlDialog(isDeckA = false) }

        findViewById<Button>(R.id.btnPlayPauseA).setOnClickListener {
            if (playerA.isPlaying) playerA.pause() else playerA.play()
        }

        findViewById<Button>(R.id.btnPlayPauseB).setOnClickListener {
            if (playerB.isPlaying) playerB.pause() else playerB.play()
        }

        findViewById<SeekBar>(R.id.crossfaderSeekBar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val ratioB = progress / 100f
                val ratioA = 1f - ratioB
                playerA.volume = ratioA
                playerB.volume = ratioB
                playerViewA.alpha = ratioA
                playerViewB.alpha = ratioB
                tvCrossfader.text = "CROSSFADER: DECK A (${(ratioA * 100).toInt()}%) | DECK B (${(ratioB * 100).toInt()}%)"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setupSpeedSeek(findViewById(R.id.speedSeekA), playerA)
        setupSpeedSeek(findViewById(R.id.speedSeekB), playerB)
    }

    private fun setupSpeedSeek(seekBar: SeekBar, player: ExoPlayer) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = 0.5f + (progress / 100f)
                player.playbackParameters = PlaybackParameters(speed, speed)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun showOnlineUrlDialog(isDeckA: Boolean) {
        val input = EditText(this)
        input.hint = "Weka link ya MP3 au MP4 (https://...)"
        AlertDialog.Builder(this)
            .setTitle(if (isDeckA) "Wimbo Online - Deck A" else "Wimbo Online - Deck B")
            .setView(input)
            .setPositiveButton("Pakia") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    loadMediaToDeck(isDeckA, Uri.parse(url), "Online: $url")
                }
            }
            .setNegativeButton("Ghairi", null)
            .show()
    }

    private fun loadMediaToDeck(isDeckA: Boolean, uri: Uri, label: String) {
        val mediaItem = MediaItem.fromUri(uri)
        if (isDeckA) {
            playerA.setMediaItem(mediaItem)
            playerA.prepare()
            playerA.play()
            tvTrackA.text = "DECK A: $label"
        } else {
            playerB.setMediaItem(mediaItem)
            playerB.prepare()
            playerB.play()
            tvTrackB.text = "DECK B: $label"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerA.release()
        playerB.release()
    }
}
