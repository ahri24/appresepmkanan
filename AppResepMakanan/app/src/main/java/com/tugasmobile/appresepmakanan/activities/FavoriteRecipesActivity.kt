package com.tugasmobile.appresepmakanan.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasmobile.appresepmakanan.R
import com.tugasmobile.appresepmakanan.adapter.FavoritesAdapter
import com.tugasmobile.appresepmakanan.model.ModelRecipes
import com.tugasmobile.appresepmakanan.realm.RealmHelper
import java.util.ArrayList

class FavoriteRecipesActivity : AppCompatActivity() {

    var modelRecipes: MutableList<ModelRecipes> = ArrayList()
    var helper: RealmHelper? = null
    val rvListFavorite:RecyclerView=findViewById(R.id.rvListFavorite)
    val tvNoData:TextView=findViewById(R.id.tvNoData)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_recipes)

        helper = RealmHelper(this)
        val toolbar:Toolbar=findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        rvListFavorite.setLayoutManager(LinearLayoutManager(this))
        rvListFavorite.setAdapter(FavoritesAdapter(this, modelRecipes))
        rvListFavorite.setHasFixedSize(true)

        //menampilkan data favorite
        getFavorite()
    }

    private fun getFavorite() {
        modelRecipes = helper!!.showFavoriteRecipes()
        if (modelRecipes.size == 0) {

            tvNoData.visibility = View.VISIBLE
            rvListFavorite.visibility = View.GONE
        } else {
            tvNoData.visibility = View.GONE
            rvListFavorite.visibility = View.VISIBLE
            rvListFavorite.adapter = FavoritesAdapter(this, modelRecipes)
        }
    }

    public override fun onResume() {
        super.onResume()
        getFavorite()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}