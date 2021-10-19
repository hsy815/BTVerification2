package com.hsy.btverification2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hsy.btverification2.R
import com.hsy.btverification2.entity.ReadMixUser

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.adapter
 * @创始人: hsy
 * @创建时间: 2021/8/22 14:00
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/22 14:00
 * @修改描述:
 */
class ReadMixAdapter(
    private var context: Context,
    private var mReadMixList: ArrayList<ReadMixUser>
) : RecyclerView.Adapter<ReadMixAdapter.ReadMixHolder>() {

    inner class ReadMixHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCount: TextView = itemView.findViewById(R.id.mix_item_count)
        val mName: TextView = itemView.findViewById(R.id.mix_item_name)
        val mId: TextView = itemView.findViewById(R.id.mix_item_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadMixHolder {
        return ReadMixHolder(
            LayoutInflater.from(context).inflate(R.layout.item_mix_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReadMixHolder, position: Int) {
        val readMixUser = mReadMixList[position]
        holder.mName.text = readMixUser.Name
        holder.mId.text = readMixUser.IDcard
        holder.mCount.setText(position + 1)
    }

    override fun getItemCount(): Int {
        return mReadMixList.size
    }

    fun setReadMix(readMix: ReadMixUser) {
        this.mReadMixList.add(readMix)
        notifyDataSetChanged()
    }

    fun deleteList() {
        mReadMixList.clear()
        notifyDataSetChanged()
    }
}