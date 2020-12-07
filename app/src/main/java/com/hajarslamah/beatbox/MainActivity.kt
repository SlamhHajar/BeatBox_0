package com.hajarslamah.beatbox

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hajarslamah.beatbox.databinding.ActivityMainBinding
import com.hajarslamah.beatbox.databinding.ListItemSoundBinding

class MainActivity : AppCompatActivity(), AudioManager.OnAudioFocusChangeListener {
    private lateinit var beatBox: BeatBox
    private var speed: Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beatBox = BeatBox(assets,speed)
      //  beatBox.loadSounds()
      var  audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
      var  focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
               setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_GAME)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                    .setAcceptsDelayedFocusGain(true)
                    . setOnAudioFocusChangeListener(this@MainActivity)
                    . build()
           } else {
               TODO("VERSION.SDK_INT < O")
           }
      }
        val binding: ActivityMainBinding =          // binding  using mvvm
                DataBindingUtil.                       //class on library we call it
                  setContentView(this, R.layout.activity_main) //use it on binding activity,int resource
                 binding.recyclerView.apply {     // the same of id in camelcase
              layoutManager = GridLayoutManager(context, 3) //build the grid show  with 3 elements
            adapter = SoundAdapter(beatBox.sounds)
        }
        binding.playbackSpeedSeekBar.apply {
           setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
               override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                   if (p1 != 0) {
                       speed = (progress / 10).toFloat();
                       beatBox.rateOfSound = speed;
                   }
                  binding.playbackSpeedText.setText("the speed of sound  $p1")

               }

               override fun onStartTrackingTouch(p0: SeekBar?) {

               }

               override fun onStopTrackingTouch(p0: SeekBar?) {

               }
           })
        }
    }

    ///////////////////////////////The recyclerView/////////////////////////
private inner class SoundHolder(private val binding: ListItemSoundBinding) :      //holder that like container  list item Sound binding
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.viewModel = SoundViewModel(beatBox)
               }
         fun bind(sound: Sound) {
            binding.apply {
                viewModel?.sound = sound
                executePendingBindings()
            }
        }
    } //root instead of id or getRoot() fun the 1st use it on layout
    private inner class SoundAdapter(private val sounds: List<Sound>)  : RecyclerView.Adapter<SoundHolder>() {
                 override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
                    SoundHolder {
                   val binding = DataBindingUtil.inflate<ListItemSoundBinding>(
                 layoutInflater,
                R.layout.list_item_sound,
              parent,
 false
            )
        return SoundHolder(binding)
                 }
          override fun onBindViewHolder(holder: SoundHolder, position: Int) {

              val sound = sounds[position]
              holder.bind(sound)


        }
                   override fun getItemCount():Int {
                       Toast.makeText(this@MainActivity,"${sounds.size}",Toast.LENGTH_LONG ).show();
                       return sounds.size
                   }
               }
    override fun onDestroy() {
        super.onDestroy()
        beatBox.release()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when(focusChange){
            AudioManager.AUDIOFOCUS_GAIN -> mediaController.transportControls.play()
            AudioManager.AUDIOFOCUS_LOSS ->mediaController.transportControls.pause()
        }
    }
}

