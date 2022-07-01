package com.example.myapplication.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapters.UsersRecyclerViewAdapter
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.R
import com.example.myapplication.ModelClasses.User
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
class UsersFragment : Fragment() {
    private lateinit var userRv: RecyclerView
    private lateinit var userRecyclerViewAdapter: UsersRecyclerViewAdapter
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

    private fun initVariables() {
        userRv = requireView().findViewById(R.id.user_rv)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVariables()
        setupChatRecyclerView()
        firebaseDatabase.reference.child(ConstantValues.FIREBASE_DATABASE_USERINFO).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v(ConstantValues.LOGCAT_TEST,"Data changed")
                userRecyclerViewAdapter.getUserList().clear()
                for(eachUserSnapshot in snapshot.children) {
                    val currentUser: User? = eachUserSnapshot.getValue(User::class.java)
                    if(currentUser != null && firebaseAuth.currentUser != null) {
                        Log.v(ConstantValues.LOGCAT_TEST, "${currentUser.getUserId()}  ->  ${firebaseAuth.currentUser?.uid}")
                        if(currentUser.getUserId() != firebaseAuth.currentUser!!.uid) {
                            userRecyclerViewAdapter.getUserList().add(currentUser)
                            //Log.v(ConstantValues.LOGCAT_TEST, currentUser.getUserName())
                        }
                    }
                }
                userRecyclerViewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun setupChatRecyclerView() {
        userRecyclerViewAdapter = UsersRecyclerViewAdapter(requireContext())
        userRv.adapter = userRecyclerViewAdapter
        val chatRvLinearLayoutManager: LinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        userRv.layoutManager = chatRvLinearLayoutManager
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
            UsersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}