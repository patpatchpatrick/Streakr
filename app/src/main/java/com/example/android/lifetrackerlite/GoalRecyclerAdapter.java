package com.example.android.lifetrackerlite;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.helper.ItemTouchHelperAdapter;
import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.helper.OnStartDragListener;
import com.example.android.lifetrackerlite.helper.PercentView;
import com.example.android.lifetrackerlite.helper.ThemeHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;


//RecyclerView Adapter to populate goals and habits data in app
public class GoalRecyclerAdapter extends RecyclerView.Adapter<GoalRecyclerAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    Cursor dataCursor;
    Context context;


    private static final String TAG = GoalRecyclerAdapter.class.getSimpleName();

    public ArrayList<Integer> mGoalOrderList;
    public LinkedHashMap<Integer, Integer> mGoalOrderToIDMap;

    // OnClickListener for items in the recyclerView
    final private ListItemClickListener mOnClickListener;

    // OnDragListener used for reordering goal/habit list via drag/drop
    final private OnStartDragListener mDragStartListener;

    // When a list item is dragged to a different position, update the mGoalOrderList order of the goals
    // to correspond to the position the goal was dragged to
    @Override
    public Boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mGoalOrderList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mGoalOrderList, i, i - 1);
            }
        }
        // Notify the adapter that the item has moved
        notifyItemMoved(fromPosition, toPosition);
        return true;

    }

    // Used as a placeholder for now.  Swipe functionality is currently disabled but if it is enabled
    // in the future, this method can be used to determine what to do when a swipe occurs
    @Override
    public void onItemDismiss(int position) {
        //mItems.remove(position);
        notifyItemRemoved(position);

    }

    @Override
    public void onItemDropped() {

        //TODO  make sure to update provider class to fix "Notify Change"  in updateGoal method

        // When an item is dropped via drag/drop we need to update the values of the orders of the goals.
        // The new goal order is contained in the mGoalOrderList ArrayList.
        // The values of the orders of the goals and the IDs of the goals are mapped in the mGoalOrderToIDMap HashMap.
        // The goal order arraylist is used in conjuction with the hashmap to set the new goal orders when
        // goals are moved around using drag and drop.

        String selection = GoalsHabitsEntry._ID + "=?";
        String[] selectionArgs;
        Iterator<Integer> goalIterator = mGoalOrderToIDMap.keySet().iterator();
        for (int i = 0; i < mGoalOrderList.size(); i++) {
            Integer currentOrder = mGoalOrderList.get(i);
            Integer currentID = mGoalOrderToIDMap.get(currentOrder);
            Integer newOrder = goalIterator.next();
            selectionArgs = new String[]{String.valueOf(currentID)};
            ContentValues values = new ContentValues();
            values.put(GoalsHabitsEntry.COLUMN_GOAL_ORDER, newOrder);
            int rowsUpdated = context.getContentResolver().update(GoalsHabitsEntry.CONTENT_URI, values, selection, selectionArgs);
        }

        context.getContentResolver().notifyChange(GoalsHabitsEntry.CONTENT_URI, null);
    }

    // ListItemClickListener with onListItemClick callback for goalsHabitsFeatureActivity to know which list item was clicked
    public interface ListItemClickListener {
        void onListItemClick(int clickedGoalID);
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView goalName;
        public TextView goalType;
        public TextView goalDetails;
        public TextView streakLengthView;
        public ImageView goalHabitIcon;
        public ImageView dragDropButton;
        public ImageView goalCompleteImageView;
        public PercentView percentView;

        public ViewHolder(View view) {

            // Get references to all TextViews,ImageViews within view that need to be populated with data
            // Set onClickListener on View to provide info about which list item was clicked
            super(view);
            goalName = (TextView) view.findViewById(R.id.goal_name);
            goalType = (TextView) view.findViewById(R.id.goal_type);
            goalDetails = (TextView) view.findViewById(R.id.goal_details);
            streakLengthView = (TextView) view.findViewById(R.id.streak_length);
            goalHabitIcon = (ImageView) view.findViewById(R.id.goal_habit_icon);
            dragDropButton = (ImageView) view.findViewById(R.id.drag_drop_button);
            goalCompleteImageView = (ImageView) view.findViewById(R.id.goal_complete_imageview);
            percentView = (PercentView) view.findViewById(R.id.percent_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            // Pass goal ID of clicked item via callback
            // This goal ID will be used in URI passed to editor activity via GoalsHabitsFeatureActivity intent
            // The goal ID is obtained below from the mGoalOrderToID hashmap which is created when a
            // new cursor is swapped in the RecyclerAdapter

            int clickedPosition = getAdapterPosition();
            int clickedGoalID;

            clickedGoalID = mGoalOrderToIDMap.get(mGoalOrderList.get(clickedPosition));

            mOnClickListener.onListItemClick(clickedGoalID);
        }
    }

    public GoalRecyclerAdapter(Activity mContext, Cursor cursor, ListItemClickListener listener, OnStartDragListener dragStartListener) {

        mDragStartListener = dragStartListener;
        dataCursor = cursor;
        context = mContext;
        mOnClickListener = listener;


    }

    @Override
    public GoalRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View goalView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_goalshabits_item, parent, false);
        return new ViewHolder(goalView);
    }

    // Swap cursor with loader data if data changes
    public Cursor swapCursor(Cursor cursor) {


        if (dataCursor == cursor) {
            return null;
        }

        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;

        if (cursor != null) {
            this.notifyDataSetChanged();

            // When a new cursor is swapped in the recyclerAdapter, an arraylist(mGoalOrderList) and
            // a LinkedHashMap(mGoalOrderToIDMap) are used in conjuction to keep track of the order
            // of goals as they are moved around via drag/drop and the ID of the goals that corresponds to the order

            // The mGoalOrderList is a list of the current order of the goals.  This list order is
            // modified whenever goals are moved around via drag/drop (onItemMoved method).

            // The mGoalOrderToIDMap is a LinkedHashMap that maps the current order of the goals with
            // the ID of the goals.  This list remains static.  Therefore, when goals are moved around
            // using the mGoalOrderList as stated above,  the IDs of the goals that were moved can be determined
            // using the mGoalOrderToIDMap and the goal orders can be updated in the database.

            // The mGoalOrderList is changed in the OnItemMoved method that is called when list items are drag/dropped.
            // The database is updated with the new goal orders in the onItemDropped method whenever a list item is dropped.

            mGoalOrderList = new ArrayList<Integer>();
            mGoalOrderToIDMap = new LinkedHashMap<Integer, Integer>();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                int goalOrder = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_ORDER));
                int goalID = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry._ID));
                mGoalOrderList.add(goalOrder);
                mGoalOrderToIDMap.put(goalOrder, goalID);
            }

        }
        return oldCursor;
    }

    // Set cursor goal/habit data on viewHolder
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        dataCursor.moveToPosition(position);

        //Determine if goal has been completed
        int goalCompleted = dataCursor.getInt(dataCursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED));

        //Set goal name string
        String goalNameText = dataCursor.getString(dataCursor.getColumnIndexOrThrow(LTContract.GoalsHabitsEntry.COLUMN_GOAL_NAME));
        holder.goalName.setText(goalNameText);

        //Set goal type string
        String goalTypeText = dataCursor.getString(dataCursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE));
        holder.goalType.setText(goalTypeText);

        //Determine if goal or habit and set the icon accordingly
        int goalOrHabit = dataCursor.getInt(dataCursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT));
        if (goalOrHabit == GoalsHabitsEntry.GOAL) {
            holder.goalHabitIcon.setImageResource(R.drawable.ic_goal);
        }
        if (goalOrHabit == GoalsHabitsEntry.HABIT) {
            holder.goalHabitIcon.setImageResource(R.drawable.ic_habit);
        }

        //Get data for goal details string

        //Convert unix start date to string and add to details
        long startDateMillis = dataCursor.getLong(dataCursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_START_DATE)) * 1000;
        SimpleDateFormat startSdf = new SimpleDateFormat("MMMM d, yyyy");
        String startDateString = startSdf.format(startDateMillis);

        //Convert unix end date to string and add to details
        long endDateMillis = dataCursor.getLong(dataCursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_END_DATE)) * 1000;
        SimpleDateFormat endSdf = new SimpleDateFormat("MMMM d, yyyy");
        String endDateString = endSdf.format(endDateMillis);

        //Set goal details string
        String goalDetailString = "";
        goalDetailString += "S: " + startDateString + "\n";
        goalDetailString += "E: " + endDateString;
        holder.goalDetails.setText(goalDetailString);

        //Get data for streak details string
        long currentTimeMillis = System.currentTimeMillis();
        long streakLengthMillis = currentTimeMillis - startDateMillis;
        long streakLengthDays = streakLengthMillis / (1000 * 60 * 60 * 24);
        long totalGoalLengthMillis = endDateMillis - startDateMillis;
        long totalGoalLengthDays = totalGoalLengthMillis / (1000 * 60 * 60 * 24);
        int streakCompletionPercent = (int) Math.round(((double) streakLengthDays / (double) totalGoalLengthDays) * 100);

        //Set streak details string
        String streakDetailsString = "";
        //If the streak is in the future (completion percentage will be negative), then show the streak
        //details text as "Future Date" instead of a negative streak and percentage
        if (streakCompletionPercent < 0) {
            streakDetailsString += "Future Date";
            streakCompletionPercent = 0;
        } else {
            streakDetailsString += Long.toString(streakLengthDays) + " days" + "\n";
            streakDetailsString += Integer.toString(streakCompletionPercent) + "%";
        }


        if (goalCompleted == GoalsHabitsEntry.GOAL_COMPLETED_YES) {
            //If the goal is completed, show the comppleted goal star icon and show a full percentView
            // pie chart (100 percent)

            holder.percentView.setPercentage(100);
            holder.goalCompleteImageView.setVisibility(View.VISIBLE);
            holder.percentView.setColor(ThemeHelper.getTheme());
            holder.streakLengthView.setVisibility(View.INVISIBLE);

        } else {

            //If the goal is not complete, set the appropriate percentage on the percentView and the
            //appropriate streak length in the streak length view

            holder.goalCompleteImageView.setVisibility(View.INVISIBLE);
            holder.streakLengthView.setVisibility(View.VISIBLE);

            holder.streakLengthView.setText(streakDetailsString);
            // If the black theme is being used, change the text color to dark
            if (ThemeHelper.getTheme() == R.style.BlackAppTheme){
                holder.streakLengthView.setTextColor(ContextCompat.getColor(context,  R.color.colorPrimaryLightBlack));
            }

            //Set the percent on the percentView so that percentView pie chart gets filled out accordingly
            holder.percentView.setPercentage(streakCompletionPercent);
            //Set the color of the percentView  based on whatever user-selected theme is being used
            holder.percentView.setColor(ThemeHelper.getTheme());
        }

        // Set an onTouchListener on the view, and when the view is touched, begin drag/drop
        holder.dragDropButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }


}
