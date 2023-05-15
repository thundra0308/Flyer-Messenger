package com.example.flyer.adapters

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.models.Chat
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private val context: Context, private var chatList :List<Chat>, private var image: String, private var receiverName: String, private val delSetSender: HashMap<String,Chat>, private val delSetReceiver: HashMap<String,Chat>, private val recyclerView: RecyclerView): RecyclerView.Adapter<ChatAdapter.MainViewHolder>() {
    private lateinit var mListener: OnItemLongClickListener
    private lateinit var click_Listener: OnItemClickListener
    inner class MainViewHolder(private val itemView: View, private val listener: OnItemLongClickListener): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                listener.onItemLongClick(adapterPosition, itemViewType)
                true
            }
            itemView.setOnClickListener {
                click_Listener.onItemClick(adapterPosition, itemViewType)
            }
        }
        fun bindDataSent(chat: Chat) {
            val cvSent = itemView.findViewById<CardView>(R.id.sent_reply_cv)
            cvSent.setOnClickListener {
                recyclerView.smoothScrollToPosition(chat.reply_pos.toInt())
            }
            val barSent = itemView.findViewById<View>(R.id.sent_reply_bar)
            val nameSent = itemView.findViewById<TextView>(R.id.sent_reply_name)
            val msgSent = itemView.findViewById<TextView>(R.id.sent_reply_msg)
            if(chat.reply_attached && chat.del_for=="") {
                cvSent.visibility = View.VISIBLE
                msgSent.text = chat.reply_text
                if(chat.reply_to==FirebaseAuth.getInstance().uid) {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.text = "You"
                } else {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.text = receiverName
                }
            } else {
                cvSent.visibility = View.GONE
            }
            if(delSetSender.contains(chat.id)) {
                itemView.findViewById<LinearLayout>(R.id.sent_ll_reply).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_sentforeground)
            } else {
                itemView.findViewById<LinearLayout>(R.id.sent_ll_reply).foreground = null
            }
            val text = itemView.findViewById<TextView>(R.id.sent_tv_text)
            val dt = itemView.findViewById<TextView>(R.id.sent_tv_time)
            text.text = chat.text
            dt.text = chat.datetime
            if(chat.del_by?.contains(FirebaseAuth.getInstance().uid)!! || chat.del_for=="Everyone") {
                text.setTextColor(ContextCompat.getColor(context,R.color.del_msg_textcolor))
                text.setTypeface(null,Typeface.ITALIC)
            }
        }
        fun bindDataReceive(chat: Chat) {
            val cvSent = itemView.findViewById<CardView>(R.id.receiver_reply_cv)
            cvSent.setOnClickListener {
                recyclerView.smoothScrollToPosition(chat.reply_pos.toInt())
            }
            val barSent = itemView.findViewById<View>(R.id.received_reply_bar)
            val nameSent = itemView.findViewById<TextView>(R.id.received_reply_name)
            val msgSent = itemView.findViewById<TextView>(R.id.received_reply_msg)
            if(chat.reply_attached && chat.del_for=="") {
                cvSent.visibility = View.VISIBLE
                msgSent.text = chat.reply_text
                if(chat.reply_to==FirebaseAuth.getInstance().uid) {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.text = "You"
                } else {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.text = receiverName
                }
            } else {
                cvSent.visibility = View.GONE
            }
            if(delSetReceiver.contains(chat.id)) {
                itemView.findViewById<LinearLayout>(R.id.received_ll_reply).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_receiveforeground)
            } else {
                itemView.findViewById<LinearLayout>(R.id.received_ll_reply).foreground = null
            }
            val text = itemView.findViewById<TextView>(R.id.received_tv_text)
            val dt = itemView.findViewById<TextView>(R.id.received_tv_datetime)
            text.text = chat.text
            dt.text = chat.datetime
            if(chat.del_by?.contains(FirebaseAuth.getInstance().uid)!! || chat.del_for=="Everyone") {
                text.setTextColor(ContextCompat.getColor(context,R.color.del_msg_textcolor))
                text.setTypeface(null,Typeface.ITALIC)
            }
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

    fun setOnLongClickListener(listener: OnItemLongClickListener) {
        mListener = listener
    }

    fun setOnClickListener(listener: OnItemClickListener) {
        click_Listener = listener
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int, viewType: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, viewType: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return if(FirebaseAuth.getInstance().uid.equals(chatList[position].from_id)) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    private val VIEW_TYPE_SENT: Int = 1
    private val VIEW_TYPE_RECEIVER: Int = 2

}