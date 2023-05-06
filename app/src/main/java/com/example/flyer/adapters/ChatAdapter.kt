package com.example.flyer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.models.Chat
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private val context: Context, private var chatList :List<Chat>, private var image: String): RecyclerView.Adapter<ChatAdapter.MainViewHolder>() {
    private lateinit var mListener: onItemClickListener
    inner class MainViewHolder(private val itemView: View, private val listener: onItemClickListener): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
        fun bindDataSent(chat: Chat) {
            val text = itemView.findViewById<TextView>(R.id.sent_tv_text)
            val dt = itemView.findViewById<TextView>(R.id.sent_tv_time)
            text.text = chat.text
            dt.text = chat.datetime
        }
        fun bindDataReceive(chat: Chat) {
            val text = itemView.findViewById<TextView>(R.id.received_tv_text)
            val dt = itemView.findViewById<TextView>(R.id.received_tv_datetime)
            text.text = chat.text
            dt.text = chat.datetime
            val iv = itemView.findViewById<CircleImageView>(R.id.receivec_iv_profile)
            Glide
                .with(context)
                .load(image)
                .centerCrop()
                .placeholder(R.drawable.profile_icon_placeholder_1)
                .into(iv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        var view: View = if(viewType == VIEW_TYPE_SENT) {
            LayoutInflater.from(context).inflate(R.layout.item_container_sent_messages,parent,false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.item_container_recived_message, parent, false)
        }
        return MainViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        if(getItemViewType(position)==VIEW_TYPE_RECEIVER) {
            holder.bindDataReceive(chatList[position])
        } else {
            holder.bindDataSent(chatList[position])
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun setOnClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return if(FirebaseAuth.getInstance().uid.equals(chatList[position].id)) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    private val VIEW_TYPE_SENT: Int = 1
    private val VIEW_TYPE_RECEIVER: Int = 2

}