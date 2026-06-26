package com.nova.app.ui.routines
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nova.app.databinding.ActivityRoutinesBinding

class RoutinesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoutinesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoutinesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Routines"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnAddRoutine.setOnClickListener {
            Toast.makeText(this, "Routine builder — coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}