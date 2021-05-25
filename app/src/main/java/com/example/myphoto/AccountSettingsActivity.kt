package com.example.myphoto

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.myphoto.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        logout_bnt.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_text_bnt.setOnClickListener {

            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        save_info_profile_bnt.setOnClickListener {
            if (checker == "clicked")
            {
                uploadImageAndUpdateInfo()
            }
            else
            {
                updateUserInfo()
            }
        }


        //wyczytanie danych do pól
        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri =  result.uri
            profile_image_view_profile_fragment.setImageURI(imageUri)
        }

    }

    private fun updateUserInfo()
    {
        when {
            TextUtils.isEmpty(full_name_profile_fragment.text.toString()) -> {
                Toast.makeText(this, getString(R.string.fullnameWrite), Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(username_profile_fragment.text.toString()) -> {
                Toast.makeText(this, getString(R.string.usernameWrite), Toast.LENGTH_LONG).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = full_name_profile_fragment.text.toString().toLowerCase()
                userMap["username"] = username_profile_fragment.text.toString().toLowerCase()
                userMap["bio"] = biogram_profile_fragment.text.toString()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this,getString(R.string.updateSucces),Toast.LENGTH_LONG).show()

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                if (snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_view_profile_fragment)
                    username_profile_fragment.setText(user.getUsername())
                    full_name_profile_fragment.setText(user.getFullname())
                    biogram_profile_fragment.setText(user.getBio())

                }
            }

            override fun onCancelled(error: DatabaseError)
            {

            }

        })
    }

    private fun uploadImageAndUpdateInfo()
    {


        when {
            imageUri == null -> Toast.makeText(this, getString(R.string.imageSelectWarning), Toast.LENGTH_LONG).show()

            TextUtils.isEmpty(full_name_profile_fragment.text.toString()) -> {
                Toast.makeText(this, getString(R.string.fullnameWrite), Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(username_profile_fragment.text.toString()) -> {
                Toast.makeText(this, getString(R.string.usernameWrite), Toast.LENGTH_LONG).show()
            }

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle(getString(R.string.settingsDialog))
                progressDialog.setMessage(getString(R.string.waitDialogSettings))
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser.uid + getString(R.string.jpg))

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if(!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw  it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = full_name_profile_fragment.text.toString().toLowerCase()
                        userMap["username"] = username_profile_fragment.text.toString().toLowerCase()
                        userMap["bio"] = biogram_profile_fragment.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this,getString(R.string.updateSucces),Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                } )
            }
        }



    }
}