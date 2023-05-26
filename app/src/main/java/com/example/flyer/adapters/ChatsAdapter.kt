package com.example.flyer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.activity.ChatActivity
import com.example.flyer.models.ChatRooms
import com.example.flyer.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(private val context: Context, private val chats: List<ChatRooms>): RecyclerView.Adapter<ChatsAdapter.MainViewHolder>() {
    private val selectedItems = HashSet<Int>()
    val selectionEnabledLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    inner class MainViewHolder(private val itemView: View): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                if(selectionEnabledLiveData.value==false) {
                    selectionEnabledLiveData.value = true
                    selectedItems.add(adapterPosition)
                    notifyItemChanged(adapterPosition)
                }
                true
            }

            itemView.setOnClickListener {
                if (selectionEnabledLiveData.value==true) {
                    if (selectedItems.contains(adapterPosition)) {
                        selectedItems.remove(adapterPosition)
                        if(selectedItems.size==0) {
                            selectedItems.clear()
                            selectionEnabledLiveData.value = false
                        }
                    } else {
                        selectedItems.add(adapterPosition)
                    }
                    notifyItemChanged(adapterPosition)
                } else {
                    val senderId = chats[adapterPosition].sender_id
                    val receiverId = chats[adapterPosition].receiver_id
                    val chatRoomId = chats[adapterPosition].id
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra(Constants.KEY_SENDER_ID,senderId)
                    intent.putExtra(Constants.KEY_RECEIVER_ID,receiverId)
                    intent.putExtra(Constants.KEY_CHAT_ROOM_ID,chatRoomId)
                    startActivity(context,intent,null)
                }
            }
        }
        fun bindData(receiver: ChatRooms) {
            if(selectedItems.contains(adapterPosition)) {
                itemView.findViewById<CardView>(R.id.chatscreen_rvchat_cv_parent).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chatroom_background)
            } else {
                itemView.findViewById<CardView>(R.id.chatscreen_rvchat_cv_parent).foreground = null
            }
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
                if(receiver.last_msg_del_status?.contains(FirebaseAuth.getInstance().uid!!)!!) {
                    text.text = "This Message has been Deleted !"
                } else {
                    text.text = receiver.last_text
                }
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
                if(receiver.last_msg_del_status?.contains(FirebaseAuth.getInstance().uid!!)!!) {
                    text.text = "This Message has been Deleted !"
                } else {
                    text.text = receiver.last_text
                }
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
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bindData(chats[position])
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    fun getSelectedItems(): List<String> {
        val selectedList = mutableListOf<String>()
        for (position in selectedItems) {
            if (position in chats.indices) {
                selectedList.add(chats[position].id!!)
            }
        }
        selectionEnabledLiveData.value = false
        selectedItems.clear()
        notifyDataSetChanged()
        return selectedList
    }

}