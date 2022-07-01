package com.example.myapplication.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapters.InboxRecyclerViewAdapter
import com.example.myapplication.Adapters.UsersRecyclerViewAdapter
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.ModelClasses.InboxItemModel
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsFragment : Fragment() {
    private lateinit var inboxRv: RecyclerView
    private lateinit var inboxRecyclerViewAdapter: InboxRecyclerViewAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVariables()
        setupInboxRecyclerView()

        if(firebaseAuth.currentUser != null) {
            firebaseDatabase.reference.child("inbox").child(firebaseAuth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.v(ConstantValues.LOGCAT_TEST,"Data changed")
                    inboxRecyclerViewAdapter.getInboxList().clear()
                    for(eachInboxSnapshot in snapshot.children) {
                        val currentInboxItem: InboxItemModel? = eachInboxSnapshot.getValue(InboxItemModel::class.java)
                        if(currentInboxItem != null) {
                            inboxRecyclerViewAdapter.addItemToInboxList(currentInboxItem)
                        } else {
                            Log.v(ConstantValues.LOGCAT_TEST, "Fetched current inbox item object is null")
                        }
                    }
                    inboxRecyclerViewAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) { }
            })
        } else {
            Toast.makeText(requireContext(), "something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupInboxRecyclerView() {
        inboxRecyclerViewAdapter = InboxRecyclerViewAdapter(requireContext())
        inboxRv.adapter = inboxRecyclerViewAdapter
        val inboxRvLinearLayoutManager: LinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        inboxRv.layoutManager = inboxRvLinearLayoutManager
    }

    private fun initVariables() {
        inboxRv = requireView().findViewById(R.id.inbox_rv)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}