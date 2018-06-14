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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


//RecyclerView Adapter to populate goals and habits data in app
public class GoalRecyclerAdapter extends RecyclerView.Adapter<GoalRecyclerAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    Cursor dataCursor;
    Context context;

    private static final String TAG = GoalRecyclerAdapter.class.getSimpleName();

    private ArrayList<Integer> mGoalOrder;

    // OnClickListener for items in the recyclerView
    final private ListItemClickListener mOnClickListener;

    // OnDragListener used for reordering goal/habit list via drag/drop
    final private OnStartDragListener mDragStartListener;

    // TODO Update cursor position when an item is moved.
    // Determine what to do when an item is moved in the goal/habits recycleView
    @Override
    public Boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mGoalOrder, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mGoalOrder, i, i - 1);
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

        //TODO Update database order when an item is moved....  make sure to update provider class to fix "Notify Change"  in updateGoal method
        String selection = GoalsHabitsEntry.COLUMN_GOAL_ORDER + "=?";
        String[] selectionArgs;
        for (int i = 0; i < mGoalOrder.size(); i++) {
            selectionArgs = new String[]{String.valueOf(mGoalOrder.get(i))};
            ContentValues values = new ContentValues();
            values.put(GoalsHabitsEntry.COLUMN_GOAL_ORDER, i);
            int rowsUpdated = context.getContentResolver().update(GoalsHabitsEntry.CONTENT_URI, values, selection, selectionArgs);
            Log.d("Row Updated " + mGoalOrder.get(i), "New Order " + i + rowsUpdated);
        }
        context.getContentResolver().notifyChange(GoalsHabitsEntry.CONTENT_URI, null);
    }

    // ListItemClickListener with onListItemClick callback for goalsHabitsFeatureActivity to know which list item was clicked
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView goalName;
        public TextView goalType;
        public TextView goalDetails;
        public TextView streakLengthView;
        public ImageView goalHabitIcon;
        public ImageView dragDropButton;

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
            view.setOnClickListener(this);


        }


        @Override
        public void onClick(View view) {

            // Pass back click position via callback
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
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

            // Set GoalOrder ArrayList to determine order of goals/habits in cursor
            // mGoalOrder ArrayList is used to keep track of order when order is changed via drag/drop
            // in the OnItemMoved method in the GoalRecyclerAdapter
            mGoalOrder = new ArrayList<Integer>();
            for (int i = 0; i < cursor.getCount(); i++) {
                mGoalOrder.add(i);
            }
        }
        return oldCursor;
    }

    // Set cursor goal/habit data on viewHolder
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        dataCursor.moveToPosition(position);

        //Set goal name string
        String goalNameText = dataCursor.getString(dataCursor.getColumnIndexOrThrow(LTContract.GoalsHabitsEntry.COLUMN_GOAL_NAME));
        holder.goalName.setText(goalNameText);

        //Set goal type string
        String goalTypeText = GoalsHabitsEntry.getGoalTypeString(dataCursor.getInt(dataCursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE)));
        holder.goalType.setText(goalTypeText);

        //Determine if goal or habit and set the icon accordingly
        int goalOrHabit = dataCursor.getInt(dataCursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT));
        if (goalOrHabit == GoalsHabitsEntry.GOAL) {
            holder.goalHabitIcon.setImageResource(R.drawable.goal_icon);
        }
        if (goalOrHabit == GoalsHabitsEntry.HABIT) {
            holder.goalHabitIcon.setImageResource(R.drawable.habit_icon);
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
        streakDetailsString += Long.toString(streakLengthDays) + " days" + "\n";
        streakDetailsString += Integer.toString(streakCompletionPercent) + "%";
        holder.streakLengthView.setText(streakDetailsString);

        //Set color of streak circle based on streak percent completion
        int streakColor = ContextCompat.getColor(context, getStreakColor(streakCompletionPercent));
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable magnitudeCircle = (GradientDrawable) holder.streakLengthView.getBackground();
        // Set the color on the magnitude circle
        magnitudeCircle.setColor(streakColor);

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

    public int getStreakColor(int streakCompletionPercent) {
        int streakColor;
        if (streakCompletionPercent >= 0 && streakCompletionPercent < 10) {
            streakColor = R.color.streakLength10;
        } else if (streakCompletionPercent >= 10 && streakCompletionPercent < 20) {
            streakColor = R.color.streakLength20;
        } else if (streakCompletionPercent >= 20 && streakCompletionPercent < 30) {
            streakColor = R.color.streakLength30;
        } else if (streakCompletionPercent >= 30 && streakCompletionPercent < 40) {
            streakColor = R.color.streakLength40;
        } else if (streakCompletionPercent >= 40 && streakCompletionPercent < 50) {
            streakColor = R.color.streakLength50;
        } else if (streakCompletionPercent >= 50 && streakCompletionPercent < 60) {
            streakColor = R.color.streakLength60;
        } else if (streakCompletionPercent >= 60 && streakCompletionPercent < 70) {
            streakColor = R.color.streakLength70;
        } else if (streakCompletionPercent >= 70 && streakCompletionPercent < 80) {
            streakColor = R.color.streakLength80;
        } else if (streakCompletionPercent >= 80 && streakCompletionPercent < 90) {
            streakColor = R.color.streakLength90;
        } else {
            streakColor = R.color.streakLength100;
        }

        return streakColor;
    }
}
