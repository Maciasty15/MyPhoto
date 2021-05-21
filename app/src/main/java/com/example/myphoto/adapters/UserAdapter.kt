package com.example.myphoto.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myphoto.R
import com.example.myphoto.fragments.ProfileFragment
import com.example.myphoto.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.user_item_layout.view.*

class UserAdapter(private var mContext: Context, private var mUser: List<User>,private var isFragemnt: Boolean = false): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }


    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {

        //wczytanie danych do user_item
        val user = mUser[position]
        holder.userNameTextView.text = user.getUsername()
        holder.userFullnameTextView.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImage)

        //sprawdzenie listy followanych userow
        chceckFollowingStatus(user.getUid(), holder.followeButton)

        //przejscie na profil z fragmentu search
        holder.itemView.setOnClickListener ( View.OnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId",user.getUid())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
        } )

        //przycisk follow i logika
        holder.followeButton.setOnClickListener {
            if(holder.followeButton.text.toString() == "Follow")
            {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.getUid())
                            .setValue(true).addOnCompleteListener { task ->
                                if(task.isSuccessful)
                                {
                                    firebaseUser?.uid.let { it1 ->
                                        FirebaseDatabase.getInstance().reference
                                                .child("Follow").child(user.getUid())
                                                .child("Followers").child(it1.toString())
                                                .setValue(true).addOnCompleteListener { task ->
                                                    if(task.isSuccessful)
                                                    {

                                                    }
                                                }
                                        }
                                }
                            }
                    }
            }
            else
            {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.getUid())
                            .removeValue().addOnCompleteListener { task ->
                                if(task.isSuccessful)
                                {
                                    firebaseUser?.uid.let { it1 ->
                                        FirebaseDatabase.getInstance().reference
                                                .child("Follow").child(user.getUid())
                                                .child("Followers").child(it1.toString())
                                                .removeValue().addOnCompleteListener { task ->
                                                    if(task.isSuccessful)
                                                    {

                                                    }
                                                }
                                        }
                                }
                            }
                    }
            }
        }
    }


    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView)
    {
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var userFullnameTextView: TextView = itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followeButton: Button = itemView.findViewById(R.id.follow_btn_search)
    }

    private fun chceckFollowingStatus(uid: String, followeButton: Button)
    {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it1.toString())
                    .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(datasnapshot: DataSnapshot)
            {
                if(datasnapshot.child(uid).exists())
                {
                    followeButton.text = mContext.getString(R.string.following)
                }
                else
                {
                    followeButton.text = mContext.getString(R.string.follow)
                }
            }

            override fun onCancelled(error: DatabaseError)
            {

            }

        })
    }
}