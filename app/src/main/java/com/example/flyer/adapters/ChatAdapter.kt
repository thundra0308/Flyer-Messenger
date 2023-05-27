package com.example.flyer.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.models.Chat
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.HashSet
import java.util.Locale

class ChatAdapter(private val context: Context, private var chats :List<Chat>, private var image: String, private var receiverName: String, private val recyclerView: RecyclerView, private val senderId: String): RecyclerView.Adapter<ChatAdapter.MainViewHolder>() {
    var senderSet = HashSet<Int>()
    var receiverSet = HashSet<Int>()
    var liveSenderSet = MutableLiveData<HashSet<Int>>()
    var liveReceiverSet = MutableLiveData<HashSet<Int>>()
    var selectionEnabledLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    inner class MainViewHolder(private val itemView: View): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                if(selectionEnabledLiveData.value==false) {
                    selectionEnabledLiveData.value = true
                    if(chats[adapterPosition].from_id==senderId) {
                        senderSet.add(adapterPosition)
                        liveSenderSet.value = senderSet
                    } else {
                        receiverSet.add(adapterPosition)
                        liveReceiverSet.value = receiverSet
                    }
                    notifyItemChanged(adapterPosition)
                }
                true
            }
            itemView.setOnClickListener {
                if(selectionEnabledLiveData.value==true) {
                    if(chats[adapterPosition].from_id==senderId) {
                        if(senderSet.contains(adapterPosition)) {
                            senderSet.remove(adapterPosition)
                        } else {
                            senderSet.add(adapterPosition)
                        }
                        liveSenderSet.value = senderSet
                    } else {
                        if(receiverSet.contains(adapterPosition)) {
                            receiverSet.remove(adapterPosition)
                        } else {
                            receiverSet.add(adapterPosition)
                        }
                        liveReceiverSet.value = receiverSet
                    }
                    if(senderSet.size==0 && receiverSet.size==0) {
                        senderSet.clear()
                        receiverSet.clear()
                        liveSenderSet.value = senderSet
                        liveReceiverSet.value = receiverSet
                        selectionEnabledLiveData.value = false
                    }
                    notifyItemChanged(adapterPosition)
                }
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
            if(senderSet.contains(adapterPosition)) {
                itemView.findViewById<ConstraintLayout>(R.id.sent_ll_reply).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_sentforeground)
            } else {
                itemView.findViewById<ConstraintLayout>(R.id.sent_ll_reply).foreground = null
            }
            val text = itemView.findViewById<TextView>(R.id.sent_tv_text)
            val dt = itemView.findViewById<TextView>(R.id.sent_tv_time)
            text.text = chat.text
            val t1 = chat.datetime?.toDate()
            val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
            val t3 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1)
            dt.text = "${t2}"
            if(chat.del_by?.contains(FirebaseAuth.getInstance().uid)!! || chat.del_for=="Everyone") {
                text.setTextColor(ContextCompat.getColor(context,R.color.sender_del_text_color))
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
            if(receiverSet.contains(adapterPosition)) {
                itemView.findViewById<ConstraintLayout>(R.id.received_ll_reply).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_receiveforeground)
            } else {
                itemView.findViewById<ConstraintLayout>(R.id.received_ll_reply).foreground = null
            }
            val text = itemView.findViewById<TextView>(R.id.received_tv_text)
            val dt = itemView.findViewById<TextView>(R.id.received_tv_datetime)
            text.text = chat.text
            val t1 = chat.datetime?.toDate()
            val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
            val t3 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1)
            dt.text = "${t2}"
            if(chat.del_by?.contains(FirebaseAuth.getInstance().uid)!! || chat.del_for=="Everyone") {
                text.setTextColor(ContextCompat.getColor(context,R.color.receiver_del_text_color))
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
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        if(getItemViewType(position)==VIEW_TYPE_RECEIVER) {
            holder.bindDataReceive(chats[position])
        } else {
            holder.bindDataSent(chats[position])
        }

    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(FirebaseAuth.getInstance().uid.equals(chats[position].from_id)) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    fun getSelectedItems(): List<Chat> {
        val selectedList = mutableListOf<Chat>()
        for (position in senderSet) {
            if (position in chats.indices) {
                selectedList.add(chats[position])
            }
        }
        for (position in receiverSet) {
            if (position in chats.indices) {
                selectedList.add(chats[position])
            }
        }
        selectionEnabledLiveData.value = false
        senderSet.clear()
        receiverSet.clear()
        liveSenderSet.value = senderSet
        liveReceiverSet.value = receiverSet
        notifyDataSetChanged()
        return selectedList
    }

    private val VIEW_TYPE_SENT: Int = 1
    private val VIEW_TYPE_RECEIVER: Int = 2

}