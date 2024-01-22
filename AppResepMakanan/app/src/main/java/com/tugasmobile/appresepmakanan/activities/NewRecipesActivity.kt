package com.tugasmobile.appresepmakanan.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.tv.TvContract.Programs
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tugasmobile.appresepmakanan.R
import com.tugasmobile.appresepmakanan.adapter.CategoriesAdapter
import com.tugasmobile.appresepmakanan.adapter.NewRecipesAdapter
import com.tugasmobile.appresepmakanan.model.ModelCategories
import com.tugasmobile.appresepmakanan.model.ModelRecipes
import com.tugasmobile.appresepmakanan.networking.ApiEndpoint
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class NewRecipesActivity : AppCompatActivity() {

    var modelRecipesList: MutableList<ModelRecipes> = ArrayList()
    var modelCategoriesList: MutableList<ModelCategories> = ArrayList()
    var newRecipesAdapter: NewRecipesAdapter? = null
    var categoriesAdapter: CategoriesAdapter? = null
    var page = 0
    val pbListNewResep:ProgressBar=findViewById(R.id.pbListNewResep)
    val searchRecipe:SearchView=findViewById(R.id.searchRecipe)
    val rvCategories:RecyclerView=findViewById(R.id.rvCategories)
    val fabFavorite:FloatingActionButton=findViewById(R.id.fabFavorite)
    val nestedScrollView:NestedScrollView=findViewById(R.id.nestedScrollView)
    val rvListNewResep:RecyclerView=findViewById(R.id.rvListNewResep)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipes)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (Build.VERSION.SDK_INT >= 26) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        pbListNewResep.setVisibility(View.GONE)
        searchRecipe.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                getSearchRecipe(query)
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") {
                    getListNewResep(page)
                }
                return false
            }
        })

        val searchPlateId = searchRecipe.getContext().resources.getIdentifier("android:id/search_plate",
            null, null)
        val searchPlate = searchRecipe.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        categoriesAdapter = CategoriesAdapter(this, modelCategoriesList)
        rvCategories.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        rvCategories.setHasFixedSize(true)
        rvCategories.setAdapter(categoriesAdapter)

        //pagination
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
                v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                pbListNewResep.setVisibility(View.VISIBLE)
                getListNewResep(page)
            }
        })

        fabFavorite.setOnClickListener {
            val intent = Intent(this@NewRecipesActivity, FavoriteRecipesActivity::class.java)
            startActivity(intent)
        }

        showRecyclerRecipe()

        //method get data
        getCategories()
        getListNewResep(page)
    }

    private fun showRecyclerRecipe() {
        newRecipesAdapter = NewRecipesAdapter(this@NewRecipesActivity, modelRecipesList)
        rvListNewResep.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvListNewResep.setHasFixedSize(true)
        rvListNewResep.adapter = newRecipesAdapter
    }

    private fun getSearchRecipe(query: String) {
        rvListNewResep.showShimmerAdapter()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.SEARCH_RECIPES)
            .addPathParameter("query", query)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        modelRecipesList = ArrayList()
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
                            modelRecipesList.add(dataApi)
                        }
                        showRecyclerRecipe()
                        newRecipesAdapter?.notifyDataSetChanged()
                        rvListNewResep.hideShimmerAdapter()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@NewRecipesActivity, "Oops, gagal menampilkan resep makanan.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    pbListNewResep.visibility = View.GONE
                    Toast.makeText(this@NewRecipesActivity,
                        "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getCategories() {
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.CATEGORIES)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    pbCategories.visibility = View.GONE
                    try {
                        val jsonArray = response.getJSONArray("results")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObjectList = jsonArray.getJSONObject(i)
                            val dataApi = ModelCategories()
                            dataApi.strKategori = jsonObjectList.getString("category")
                            dataApi.strKategoriKey = jsonObjectList.getString("key")
                            modelCategoriesList.add(dataApi)
                        }
                        categoriesAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@NewRecipesActivity,
                            "Oops, gagal menampilkan kategori resep masakan.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    pbCategories.visibility = View.GONE
                    Toast.makeText(this@NewRecipesActivity,
                        "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getListNewResep(page: Int) {
        rvListNewResep.showShimmerAdapter()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.PAGE)
            .addQueryParameter("page", page.toString())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    pbListNewResep.visibility = View.GONE
                    try {
                        modelRecipesList = ArrayList()
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
                            modelRecipesList.add(dataApi)
                        }
                        showRecyclerRecipe()
                        newRecipesAdapter?.notifyDataSetChanged()
                        rvListNewResep.hideShimmerAdapter()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@NewRecipesActivity,
                            "Oops, gagal menampilkan resep makanan.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    pbListNewResep.visibility = View.GONE
                    Toast.makeText(this@NewRecipesActivity,
                        "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    companion object {
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