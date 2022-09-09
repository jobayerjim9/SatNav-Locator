package com.jobayerjim9.satnav.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ActivityProfileBinding
import com.jobayerjim9.satnav.ui.models.Constants
import com.jobayerjim9.satnav.ui.models.UserProfile
import com.jobayerjim9.satnav.utility.Utils
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.ArrayList
import com.google.gson.reflect.TypeToken
import com.jobayerjim9.satnav.data.model.CountryModel
import com.jobayerjim9.satnav.utility.SelectItemListener
import org.json.JSONObject

class ProfileActivity : AppCompatActivity(), SelectItemListener {
    private lateinit var binding: ActivityProfileBinding
    lateinit var institutes: ArrayList<String>
    lateinit var countryModels: ArrayList<CountryModel>
    var cities: ArrayList<String> = ArrayList()
    var countries: ArrayList<String> = ArrayList()
    lateinit var cityData: String
    lateinit var jsonObject: JSONObject
    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        initView()
    }


    private fun initView() {
        binding.loading = true

        val database =
            Firebase.database.reference.child("users").child(Firebase!!.auth.uid.toString())

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.loading = false
                val user = snapshot.getValue(UserProfile::class.java) as UserProfile
                if (user.photo.isNotEmpty()) {
                    Picasso.get().load(user.photo).into(binding.profileImage)
                }
                binding.data = user
                if (user.gender == "m") {
                    binding.maleRadio.isChecked = true
                } else if (user.gender == "f") {
                    binding.femaleRadio.isChecked = true
                }
                if (user.signUpType == "phone") {
                    binding.phone.isEnabled = false
                    binding.emailProfile.isEnabled = true
                }
                binding.cityButton.setOnClickListener {
                    if (user.country.isEmpty()) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Select Country First!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val city = jsonObject.get(user.country).toString()
                        val gson = Gson()
                        val listUserType: Type = object : TypeToken<List<String>>() {}.type
                        cities = gson.fromJson<ArrayList<String>>(city, listUserType)
                        val dialog =
                            ChooserDialogFragment(Constants.TYPE_CITY, this@ProfileActivity, cities)
                        dialog.show(supportFragmentManager, "Cities")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        cityData = Utils.loadJSONFromAsset(this, Constants.TYPE_CITY).toString()
        jsonObject = JSONObject(cityData)


        binding.backButton.setOnClickListener {
            finish()
        }
        binding.editImage.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(
                    this@ProfileActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                        != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                    this@ProfileActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                        != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(this@ProfileActivity, permissions, 1)
            } else {
                //pickImage(1);
                selectImage()
            }
        }
        binding.profileImage.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(
                    this@ProfileActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                        != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                    this@ProfileActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                        != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(this@ProfileActivity, permissions, 1)
            } else {
                //pickImage(1);
                selectImage()
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.maleRadio) {
                binding!!.data?.gender = "m"
            } else if (checkedId == R.id.femaleRadio) {
                binding!!.data?.gender = "f"
            }
        }
        binding.saveButton.setOnClickListener {
            saveData()
        }

        val jsonData = Utils.loadJSONFromAsset(this, Constants.TYPE_INDIAN_COLLEGE)
        val gson = Gson()
        val listUserType: Type = object : TypeToken<List<String>>() {}.type
        institutes = gson.fromJson<ArrayList<String>>(jsonData, listUserType)
        binding.chooseInstituteButton.setOnClickListener {
            val dialog = ChooserDialogFragment(Constants.TYPE_INDIAN_COLLEGE, this, institutes)
            dialog.show(supportFragmentManager, "Institute")
        }







        binding.countryButton.setOnClickListener {
            countries.clear()
            val countryData = Utils.loadJSONFromAsset(this, Constants.TYPE_COUNTRY)
            val listCountryType: Type = object : TypeToken<List<CountryModel>>() {}.type
            countryModels = gson.fromJson<ArrayList<CountryModel>>(countryData, listCountryType)
            for (country in countryModels) {
                countries.add(country.Name)
            }
            val dialog = ChooserDialogFragment(Constants.TYPE_COUNTRY, this, countries)
            dialog.show(supportFragmentManager, "Countries")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                ActivityCompat.requestPermissions(this@ProfileActivity, permissions, 1)
            }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    var bitmap: Bitmap? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            var uri = data!!.data
            if (uri == null) {
                val extras = data.extras
                val imageBitmap = extras!!["data"] as Bitmap
                uri = getImageUri(this@ProfileActivity, imageBitmap)
            }
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            binding.profileImage.setImageBitmap(bitmap)
        }

    }

    private fun selectImage() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }

    private fun saveData() {
        binding.loading = true
        if (bitmap != null) {
            val baos = ByteArrayOutputStream()
            val path = binding!!.data?.uid + ".jpg"
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val storage = FirebaseStorage.getInstance().getReference("userImage")
                .child(path)
            storage.putBytes(data).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storage.downloadUrl.addOnSuccessListener { uri ->
                        binding!!.data?.photo = uri!!.toString()
                        uploadData()
                    }
                }
            }
        } else {
            uploadData()
        }

    }

    override fun onDestroy() {

        super.onDestroy()
    }

    private fun uploadData() {
        val database = Firebase.database.reference.child("users")
            .child(binding!!.data?.uid.toString())
        database.setValue(binding.data).addOnCompleteListener(this@ProfileActivity) {
            binding.loading = false
            if (it.isSuccessful) {
                Toast.makeText(this@ProfileActivity, "Saved Successfully", Toast.LENGTH_SHORT)
                    .show()
                finish()
            } else {
                Toast.makeText(
                    this@ProfileActivity,
                    it.exception?.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun selectedItem(type: String, position: Int) {
        if (type == Constants.TYPE_INDIAN_COLLEGE) {
            binding.data?.institute = institutes[position]
            binding.instituteName.text = binding.data?.institute
        } else if (type == Constants.TYPE_CITY) {
            binding.data?.city = cities[position]
            binding.cityName.text = binding.data?.city
        } else if (type == Constants.TYPE_COUNTRY) {
            binding.data?.city = ""
            val city = jsonObject.get(countries[position]).toString()
            val gson = Gson()
            val listUserType: Type = object : TypeToken<List<String>>() {}.type
            cities = gson.fromJson<ArrayList<String>>(city, listUserType)
            binding.data?.country = countries[position]
            binding.countryName.text = binding.data?.country
        }
    }
}