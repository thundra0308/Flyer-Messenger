package com.example.flyer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.models.ChatRooms
import com.example.flyer.models.User
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ContactsAdapter(private val context: Context, private val user: List<ChatRooms>): RecyclerView.Adapter<ContactsAdapter.MainViewHolder>() {
    private lateinit var mListener: onItemClickListener
    inner class MainViewHolder(private val itemView: View, private val listener: onItemClickListener): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
        fun bindData(receiver: ChatRooms) {
            val sender_id: String = FirebaseAuth.getInstance().uid!!
            val profile = itemView.findViewById<CircleImageView>(R.id.contactscreen_rvchat_profileicon)
            val name = itemView.findViewById<TextView>(R.id.contactscreen_rvchat_tv_name)
            val text_status = itemView.findViewById<TextView>(R.id.contactscreen_rv_chats_tv_textstatus)
            val date_added = itemView.findViewById<TextView>(R.id.contactscreen_rv_chats_tv_dateadded)
            if(receiver.receiver_id.equals(sender_id)) {
                Glide
                    .with(context)
                    .load(receiver.sender_image)
                    .centerCrop()
                    .placeholder(R.drawable.profile_icon_placeholder_1)
                    .into(profile)
                name.text = receiver.sender_name
                text_status.text = receiver.sender_thoughts
                if(receiver.date_added!=null) {
                    val t1 = receiver.date_added?.toDate()
                    val t2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1!!)
                    date_added.text = t2.toString()
                }
            } else {
                Glide
                    .with(context)
                    .load(receiver.receiver_image)
                    .centerCrop()
                    .placeholder(R.drawable.profile_icon_placeholder_1)
                    .into(profile)
                name.text = receiver.receiver_name
                text_status.text = receiver.receiver_thoughts
                if(receiver.date_added!=null) {
                    val t1 = receiver.date_added?.toDate()
                    val t2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1!!)
                    date_added.text = t2.toString()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item_contacts,parent,false)
        return MainViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bindData(user[position])
    }

    override fun getItemCount(): Int {
        return user.size
    }

    fun setOnClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
}