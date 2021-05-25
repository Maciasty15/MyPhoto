package com.example.myphoto.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myphoto.R
import com.example.myphoto.adapters.NotificationsAdapter
import com.example.myphoto.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList


class NotificationsFragment : Fragment()
{
    private var notificationList: List<Notification>? = null
    private var notificationsAdapter: NotificationsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_notifications)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationList = ArrayList()
        notificationsAdapter = context?.let { NotificationsAdapter(it, notificationList as ArrayList<Notification>) }
        recyclerView.adapter = notificationsAdapter

        readNotifications()

        return view
    }

    private fun readNotifications()
    {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(FirebaseAuth.getInstance().currentUser!!.uid)

        notiRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                if (snapshot.exists())
                {
                    (notificationList as ArrayList<Notification>).clear()

                    for (x in snapshot.children)
                    {
                        val notification = x.getValue(Notification::class.java)
                        if (notification!!.getUserId() != FirebaseAuth.getInstance().currentUser!!.uid) {
                            (notificationList as ArrayList<Notification>).add(notification)
                        }
                    }
                    Collections.reverse(notificationList)
                    notificationsAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
            }


        })
    }

}