package com.example.myphoto.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myphoto.AccountSettingsActivity
import com.example.myphoto.R
import com.example.myphoto.adapters.MyPhotoAdapter
import com.example.myphoto.models.Post
import com.example.myphoto.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postList: List<Post>? = null
    var myPhotoAdapter: MyPhotoAdapter? = null

    var myPhotoSavedAdapter: MyPhotoAdapter? = null
    var postSavedList: List<Post>? = null
    var mySavesPhotoKey: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        if (profileId == firebaseUser.uid)
        {
            view.edit_account_settings_btn.text = "Edit Profile"
        }
        else if (profileId != firebaseUser.uid)
        {
            checkFollowAndFollowingButton()
        }

        //recycler view dla postów usera
        val recyclerViewUploadPic: RecyclerView = view.findViewById(R.id.recyceler_view_upload_pic)
        recyclerViewUploadPic.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadPic.layoutManager = linearLayoutManager

        postList = ArrayList()
        myPhotoAdapter = context?.let { MyPhotoAdapter(it, postList as ArrayList<Post> ) }
        recyclerViewUploadPic.adapter = myPhotoAdapter


        //recycler view dla zapisanych zdjęć usera
        val recyclerViewSavadPic: RecyclerView = view.findViewById(R.id.recyceler_view_saved_pic)
        recyclerViewSavadPic.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSavadPic.layoutManager = linearLayoutManager2

        postSavedList = ArrayList()
        myPhotoSavedAdapter = context?.let { MyPhotoAdapter(it, postSavedList as ArrayList<Post> ) }
        recyclerViewSavadPic.adapter = myPhotoSavedAdapter


        recyclerViewSavadPic.visibility = View.GONE
        recyclerViewUploadPic.visibility = View.VISIBLE

        val uploadedPhotoBtn: ImageButton = view.findViewById(R.id.imeges_grid_view_btn)
        uploadedPhotoBtn.setOnClickListener {
            recyclerViewSavadPic.visibility = View.GONE
            recyclerViewUploadPic.visibility = View.VISIBLE
        }

        val savedPhotoBtn: ImageButton = view.findViewById(R.id.imeges_save_btn)
        savedPhotoBtn.setOnClickListener {
            recyclerViewSavadPic.visibility = View.VISIBLE
            recyclerViewUploadPic.visibility = View.GONE
        }


        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Follow" ->
                {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1)
                                .child("Following").child(profileId)
                                .setValue(true)
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1)
                                .setValue(true)

                    }
                }
                getButtonText == "Following" ->
                {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1)
                                .child("Following").child(profileId)
                                .removeValue()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1)
                                .removeValue()

                    }
                }
            }
        }

        getTotalNumberOfPosts()
        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
        mySaves()

        return view
    }

    private fun checkFollowAndFollowingButton()
    {
        val followingRef = firebaseUser.uid.let { it1 ->
        FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1)
                .child("Following")
        }

        if(followingRef != null)
        {
            followingRef.addValueEventListener(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    if (snapshot.child(profileId).exists())
                    {
                        view?.edit_account_settings_btn?.text = "Following"
                    }
                    else
                    {
                        view?.edit_account_settings_btn?.text = "Follow"
                    }
                }
                override fun onCancelled(error: DatabaseError)
                {

                }

            })
        }

    }


    private fun getFollowers()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
                    .child("Follow").child(profileId)
                    .child("Followers")


        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    view?.total_followers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError)
            {

            }
        })
    }

    private fun getFollowings()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
                    .child("Follow").child(profileId)
                    .child("Following")


        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    val following = (snapshot.childrenCount - 1)
                    view?.total_following?.text = following.toString()
                }
            }

            override fun onCancelled(error: DatabaseError)
            {

            }
        })
    }

    private fun myPhotos()
    {
        val postrRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postrRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    (postList as ArrayList<Post>).clear()

                    for (x in snapshot.children)
                    {
                        val post = x.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileId)
                        {
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myPhotoAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
            }

        })
    }


    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                if (snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.image_profile_fragment)
                    view?.profile_fragment_username?.text = user.getUsername()
                    view?.full_name_profile_fragment?.text = user.getFullname()
                    view?.biogram_profile_fragment?.text = user.getBio()

                }
            }

            override fun onCancelled(error: DatabaseError)
            {

            }

        })
    }

    private fun getTotalNumberOfPosts()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    var postCounter = 0

                    for(x in snapshot.children)
                    {
                        val post = x.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileId)
                        {
                            postCounter++
                        }
                    }
                    total_posts.text = postCounter.toString()
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
            }
        })
    }

    private fun mySaves()
    {
        mySavesPhotoKey = ArrayList()

        val savedRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)

        savedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    for (x in snapshot.children)
                    {
                        (mySavesPhotoKey as ArrayList<String>).add(x.key!!)
                    }
                    readSavedPhotoData()
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
            }
        })
    }

    private fun readSavedPhotoData()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    (postSavedList as ArrayList<Post>).clear()

                    for (x in snapshot.children)
                    {
                        val post = x.getValue(Post::class.java)

                        for (key in mySavesPhotoKey!!)
                        {
                            if (post!!.getPostid() == key)
                            {
                                (postSavedList as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    myPhotoSavedAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
            }

        })
    }


    override fun onStart() {
        super.onStart()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }



}