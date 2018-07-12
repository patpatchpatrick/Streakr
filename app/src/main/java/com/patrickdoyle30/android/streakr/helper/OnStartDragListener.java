package com.patrickdoyle30.android.streakr.helper;

import android.support.v7.widget.RecyclerView;

// OnDragListener for reordering the goal/habit list using drag/drop
public interface OnStartDragListener {
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
