package com.adjarabet.user.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adjarabet.user.R
import com.adjarabet.user.model.Player
import kotlinx.android.synthetic.main.player_move_item.view.*

class MoveRecyclerAdapter(val lastMoveBy: Player, val lastMove: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val HEADER_VIEW = 0
        const val MOVE_VIEW = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_VIEW else MOVE_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == HEADER_VIEW) MoveHeaderViewHolder(
            inflater.inflate(R.layout.move_header_item, parent, false)
        ) else MoveViewHolder(inflater.inflate(R.layout.player_move_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MoveViewHolder) {
            holder.view.lastMoveWord.text = if (lastMoveBy == Player.BOT) {
                if (position == 2) lastMove else ""
            } else {
                if (position == 1) lastMove else ""
            }
        }

    }

    override fun getItemCount(): Int {
        return 3
    }


    class MoveHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class MoveViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}