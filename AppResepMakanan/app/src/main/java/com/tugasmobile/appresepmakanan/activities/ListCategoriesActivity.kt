package com.tugasmobile.appresepmakanan.activities

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasmobile.appresepmakanan.R
import com.tugasmobile.appresepmakanan.adapter.ListCategoriesAdapter
import com.tugasmobile.appresepmakanan.model.ModelCategories
import com.tugasmobile.appresepmakanan.model.ModelRecipes
import com.tugasmobile.appresepmakanan.networking.ApiEndpoint
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class ListCategoriesActivity : AppCompatActivity() {

    var modelCategories: ModelCategories? = null
    var modelCategoriesList: MutableList<ModelRecipes> = ArrayList()
    var listCategoriesAdapter: ListCategoriesAdapter? = null
    var strKategoriKey: String? = null
    var strKategori: String? = null
    val toolbar:Toolbar=findViewById(R.id.toolbar)
    val rvListCategories:RecyclerView=findViewById(R.id.rvListFavorite)
    val tvTitleRecipe:TextView=findViewById(R.id.tvTitleRecipe)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_categories)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }


        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        listCategoriesAdapter = ListCategoriesAdapter(this, modelCategoriesList)
        rvListCategories.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
        rvListCategories.setHasFixedSize(true)
        rvListCategories.setAdapter(listCategoriesAdapter)

        modelCategories = intent.getSerializableExtra(LIST_CATEGORIES) as ModelCategories
        if (modelCategories != null) {
            strKategoriKey = modelCategories?.strKategoriKey
            strKategori = modelCategories?.strKategori
            tvTitleRecipe.setText(strKategori)

            //method get kategori
            getListCategories()
        }
    }

    private fun getListCategories() {
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.LIST_CATEGORIES)
            .addPathParameter("key", strKategoriKey)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val jsonArray = response.getJSONArray("results")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObjectList = jsonArray.getJSONObject(i)
                            val dataApi = ModelRecipes()
                            dataApi.strTitleResep = jsonObjectList.getString("title")
                            dataApi.strThumbnail = jsonObjectList.getString("thumb")
                            dataApi.strKeyResep = jsonObjectList.getString("key")
                            dataApi.strTimes = jsonObjectList.getString("times")
                            dataApi.strPortion = jsonObjectList.getString("serving")
                            dataApi.strDificulty = jsonObjectList.getString("difficulty")
                            modelCategoriesList.add(dataApi)
                        }
                        listCategoriesAdapter?.notifyDataSetChanged()
                        rvListCategories.hideShimmerAdapter()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@ListCategoriesActivity,
                            "Oops, gagal menampilkan resep masakan.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@ListCategoriesActivity,
                        "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val LIST_CATEGORIES = "strListCategories"
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }

}