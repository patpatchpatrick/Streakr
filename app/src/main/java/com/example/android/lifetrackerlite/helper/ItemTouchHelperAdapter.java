package com.example.android.lifetrackerlite.helper;

// Interface to provide callbacks to notify if an item is moved or swiped to update the data accordingly
public interface ItemTouchHelperAdapter {

    // Interface for if item is moved
    Boolean onItemMove(int fromPosition, int toPosition);

    // Interface for if item is dismissed
    void onItemDismiss(int position);

    // Interface for if item is dropped
    void onItemDropped();
}