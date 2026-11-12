package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import vn.hvktmm.bach.securitylab.R

class MovieActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarMovie).setNavigationOnClickListener { finish() }
        findViewById<View>(R.id.cardMovie1).setOnClickListener { open("Avengers: Endgame", 110_000L, R.drawable.movie_avengers) }
        findViewById<View>(R.id.cardMovie2).setOnClickListener { open("Lật Mặt 7: Một Điều Ước", 95_000L, R.drawable.movie_lat_mat) }
    }

    private fun open(name: String, price: Long, image: Int) {
        startActivity(Intent(this, MovieBookingActivity::class.java).apply {
            putExtra("MOVIE_NAME", name); putExtra("TICKET_PRICE", price); putExtra("MOVIE_IMAGE", image)
        })
    }
}
