package com.example.android.lifetrackerlite.helper;

// Interface to provide callbacks to notify if an item is moved or swiped to update the data accordingly
public interface ItemTouchHelperAdapter {

    Boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}