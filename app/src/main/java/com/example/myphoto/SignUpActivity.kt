package com.example.myphoto

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.signup_link_btn
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signin_link_btn.setOnClickListener { startActivity(Intent(this, SignInActivity::class.java)) }

        signup_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = fullname_signup.text.toString()
        val userName = usernick_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()

        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, getString(R.string.fullnameRequired), Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, getString(R.string.usernameRequired), Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, getString(R.string.emailRequired), Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, getString(R.string.passwordRequired), Toast.LENGTH_LONG).show()

            else -> {

                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle(getString(R.string.SignUpTitle))
                progressDialog.setMessage(getString(R.string.passMess))
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()


                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener() { task ->
                    if (task.isSuccessful)
                    {
                        saveUserInfo(fullName,userName,email,progressDialog)
                    }
                    else{
                        val message = task.exception!!.toString()
                        Toast.makeText(this,getString(R.string.errorString) + message, Toast.LENGTH_LONG).show()
                        mAuth.signOut()
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = getString(R.string.defaultBio)
        userMap["image"] = getString(R.string.defaultPicture)

        usersRef.child(currentUserID).setValue(userMap).addOnCompleteListener { task ->
            if(task.isSuccessful)
            {
                progressDialog.dismiss()
                Toast.makeText(this,getString(R.string.TOASTcreateAcc),Toast.LENGTH_LONG).show()


                FirebaseDatabase.getInstance().reference
                            .child("Follow").child(currentUserID)
                            .child("Following").child(currentUserID)
                            .setValue(true)



                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            else{
                val message = task.exception!!.toString()
                Toast.makeText(this,getString(R.string.errorString) + message, Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
                progressDialog.dismiss()
            }
        }
    }
}