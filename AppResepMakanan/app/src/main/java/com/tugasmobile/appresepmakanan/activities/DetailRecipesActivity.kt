package com.tugasmobile.appresepmakanan.activities

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.ivbaranov.mfb.MaterialFavoriteButton
import com.google.android.material.snackbar.Snackbar
import com.tugasmobile.appresepmakanan.R
import com.tugasmobile.appresepmakanan.adapter.IngredientsAdapter
import com.tugasmobile.appresepmakanan.adapter.StepsAdapter
import com.tugasmobile.appresepmakanan.model.ModelIngredients
import com.tugasmobile.appresepmakanan.model.ModelRecipes
import com.tugasmobile.appresepmakanan.model.ModelSteps
import com.tugasmobile.appresepmakanan.networking.ApiEndpoint
import com.tugasmobile.appresepmakanan.realm.RealmHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class DetailRecipesActivity : AppCompatActivity() {

    var modelRecipes: ModelRecipes? = null
    var modelIngredients: MutableList<ModelIngredients> = ArrayList()
    var modelRecipesRealm: List<ModelRecipes> = ArrayList()
    var modelSteps: MutableList<ModelSteps> = ArrayList()
    var ingredientsAdapter: IngredientsAdapter? = null
    var stepsAdapter: StepsAdapter? = null
    var strRecipeKey: String? = null
    var strThumbnail: String? = null
    var strTitleRecipe: String? = null
    var strAuthor: String? = null
    var strDate: String? = null
    var strDificulty: String? = null
    var strPortion: String? = null
    var strTimes: String? = null
    var strDesc: String? = null
    var strIngredient: String? = null
    var strStep: String? = null
    var progressDialog: ProgressDialog? = null
    var helper: RealmHelper? = null
    val tvTitleRecipe:TextView=findViewById(R.id.tvTitleRecipe)
    val tvDificulty:TextView=findViewById(R.id.tvDificulty)
    val tvPortion:TextView=findViewById(R.id.tvPortion)
    val tvTimes:TextView=findViewById(R.id.tvTimes)
    val rvIngredients:RecyclerView=findViewById(R.id.rvIngredients)
    val rvSteps:RecyclerView=findViewById(R.id.rvSteps)
    val imgFavorite:MaterialFavoriteButton=findViewById(R.id.imgFavorite)
    val tvAuthor:TextView=findViewById(R.id.tvAuthor)
    val tvDate:TextView=findViewById(R.id.tvDate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_recipes)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }
        val toolbar:Toolbar=findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Mohon Tunggu…")
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("sedang menampilkan resep")

        helper = RealmHelper(this)

        modelRecipes = intent.getSerializableExtra(DETAIL_RECIPES) as ModelRecipes
        if (modelRecipes != null) {

            //get string model recipes
            strRecipeKey = modelRecipes?.strKeyResep
            strThumbnail = modelRecipes?.strThumbnail
            strTitleRecipe = modelRecipes?.strTitleResep
            strDificulty = modelRecipes?.strDificulty
            strPortion = modelRecipes?.strPortion
            strTimes = modelRecipes?.strTimes

            val imageThumbnail:ImageView=findViewById(R.id.imageThumbnail)
            //image thumbnail
            Glide.with(this)
                .load(strThumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageThumbnail)

            //set text
            tvTitleRecipe.setText(strTitleRecipe)
            tvDificulty.setText(strDificulty)
            tvPortion.setText(strPortion)
            tvTimes.setText(strTimes)

            //recycler ingredients
            ingredientsAdapter = IngredientsAdapter(modelIngredients)
            rvIngredients.setLayoutManager(GridLayoutManager(this, 2, RecyclerView.VERTICAL, false))
            rvIngredients.setHasFixedSize(true)
            rvIngredients.setAdapter(ingredientsAdapter)

            //recycler steps
            stepsAdapter = StepsAdapter(modelSteps)
            rvSteps.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
            rvSteps.setHasFixedSize(true)
            rvSteps.setAdapter(stepsAdapter)

            //get recipes detail
            getDetailRecipes()
        }

        //cek data
        modelRecipesRealm = helper!!.showFavoriteRecipes()
        if (modelRecipesRealm.size == 0) {
            imgFavorite.setFavorite(false, true)
        } else {
            imgFavorite.setFavorite(true, true)
        }

        imgFavorite.setOnFavoriteChangeListener { buttonView, favorite ->
            if (favorite) {
                strRecipeKey = modelRecipes?.strKeyResep
                strTitleRecipe = modelRecipes?.strTitleResep
                strThumbnail = modelRecipes?.strThumbnail
                helper?.addFavoriteRecipes(strRecipeKey, strTitleRecipe, strThumbnail,
                    strDificulty, strPortion, strTimes)
                Snackbar.make(buttonView, modelRecipes?.strTitleResep + " Added to Favorite",
                    Snackbar.LENGTH_SHORT).show()
            } else {
                helper?.deleteFavoriteRecipes(modelRecipes?.strKeyResep)
                Snackbar.make(buttonView, modelRecipes?.strTitleResep + " Removed from Favorite",
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDetailRecipes() {
        progressDialog?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.DETAIL_RECIPES)
            .addPathParameter("key", strRecipeKey)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val jsonObject = response.getJSONObject("results")
                        val jsonObjectAuthor = jsonObject.getJSONObject("author")
                        strAuthor = jsonObjectAuthor.getString("user")
                        strDate = jsonObjectAuthor.getString("datePublished")
                        strDesc = jsonObject.getString("desc")

                        //set text
                        tvAuthor.text = strAuthor
                        tvDate.text = "   -   $strDate"

                        //read more
                        val readMoreOption = ReadMoreOption.Builder(this@DetailRecipesActivity)
                            .textLength(3, ReadMoreOption.TYPE_LINE)
                            .moreLabel(" Lihat Selengkapnya")
                            .lessLabel(" Persingkat Detil")
                            .moreLabelColor(resources.getColor(R.color.colorPrimaryDark))
                            .lessLabelColor(resources.getColor(R.color.colorPrimaryDark))
                            .expandAnimation(true)
                            .build()
                        readMoreOption.addReadMoreTo(tvDesc, strDesc)

                        //get ingredient
                        try {
                            val jsonArrayIngredient = jsonObject.getJSONArray("ingredient")
                            for (i in 0 until jsonArrayIngredient.length()) {
                                val dataApi = ModelIngredients()
                                strIngredient = jsonArrayIngredient[i].toString()
                                dataApi.strIngredient = strIngredient
                                modelIngredients.add(dataApi)
                            }
                            ingredientsAdapter?.notifyDataSetChanged()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@DetailRecipesActivity,
                                "Oops, gagal menampilkan bahan-bahan masakan.", Toast.LENGTH_SHORT).show()
                        }

                        //get step
                        try {
                            val jsonArrayStep = jsonObject.getJSONArray("step")
                            for (j in 0 until jsonArrayStep.length()) {
                                val dataApi = ModelSteps()
                                strStep = jsonArrayStep[j].toString()
                                dataApi.strSteps = strStep
                                modelSteps.add(dataApi)
                            }
                            stepsAdapter?.notifyDataSetChanged()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@DetailRecipesActivity,
                                "Oops, gagal menampilkan langkah pembuatan masakan.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@DetailRecipesActivity,
                            "Oops, gagal detail resep masakan.", Toast.LENGTH_SHORT).show()
                    }
                    progressDialog?.dismiss()
                }

                override fun onError(anError: ANError) {
                    progressDialog?.dismiss()
                    Toast.makeText(this@DetailRecipesActivity,
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
        const val DETAIL_RECIPES = "strDetailRecipes"
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