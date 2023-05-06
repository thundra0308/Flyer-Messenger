package com.example.flyer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.models.ChatRooms
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(private val context: Context, private val user: List<ChatRooms>): RecyclerView.Adapter<ChatsAdapter.MainViewHolder>() {
    private lateinit var mListener: onItemClickListener
    inner class MainViewHolder(private val itemView: View, private val listener: onItemClickListener): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
        fun bindData(receiver: ChatRooms) {
            val sender_id: String = FirebaseAuth.getInstance().uid!!
            val profile = itemView.findViewById<CircleImageView>(R.id.chatscreen_rvchat_profileicon)
            val name = itemView.findViewById<TextView>(R.id.chatscreen_rvchat_tv_name)
            val text = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_tv_message)
            val time = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_tv_time)
            val date = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_tv_date)
            val tick = itemView.findViewById<AppCompatImageView>(R.id.chatscreen_rv_chats_tickmark)
            val msgcount = itemView.findViewById<CardView>(R.id.chatscreen_rv_chats_cv_msgcount)
            val unread_count = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_mssgcount)
            if(receiver.receiver_id.equals(sender_id)) {
                Glide
                    .with(context)
                    .load(receiver.sender_image)
                    .centerCrop()
                    .placeholder(R.drawable.profile_icon_placeholder_1)
                    .into(profile)
                name.text = receiver.sender_name
                text.text = receiver.last_text
                if(receiver.last_text_from != sender_id) {
                    tick.visibility = View.GONE
                    msgcount.visibility = View.VISIBLE
                } else {
                    tick.visibility = View.VISIBLE
                    msgcount.visibility = View.GONE
                }
                if(receiver.unread_count<=0) msgcount.visibility = View.GONE
                val t1 = receiver.timestamp?.toDate()
                val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
                val t3 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1)
                unread_count.text = receiver.unread_count.toString()
                time.text = t2.toString()
                date.text = t3.toString()
            } else {
                Glide
                    .with(context)
                    .load(receiver.receiver_image)
                    .centerCrop()
                    .placeholder(R.drawable.profile_icon_placeholder_1)
                    .into(profile)
                name.text = receiver.receiver_name
                text.text = receiver.last_text
                if(receiver.last_text_from != sender_id) {
                    tick.visibility = View.GONE
                    msgcount.visibility = View.VISIBLE
                } else {
                    tick.visibility = View.VISIBLE
                    msgcount.visibility = View.GONE
                }
                if(receiver.unread_count<=0) msgcount.visibility = View.GONE
                val t1 = receiver.timestamp?.toDate()
                val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
                val t3 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1)
                unread_count.text = receiver.unread_count.toString()
                time.text = t2.toString()
                date.text = t3.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chatscreen_rv_chats,parent,false)
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