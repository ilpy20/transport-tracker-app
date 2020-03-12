package com.example.stopmodel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transportmodel.TransportTag;
import com.example.transporttracker.R;

import java.util.ArrayList;

/**
 * StopDetailsListAdapter set up recycler view in stop bottom sheet
 * @author Ilya Pyshkin, Sergey Ushakov
 * @version 1.0
 * @since 2020-03-12
 */
public class StopDetailsListAdapter extends RecyclerView.Adapter<StopDetailsListAdapter.MyViewHolder>implements View.OnClickListener  {
  private Context mContext;
  private String vehicleMode;
  private ArrayList<String> tripId;
  private ArrayList<String> mNum;
  private ArrayList<String> mName;
  private ArrayList<String> mTime;
  private ArrayList<String> mDelay;
  private ArrayList<String> mRouteDirections;

  private OnItemClickCallback onItemClickCallback;

  /**
   * Show transport marker by clicking item
   * @param v view of item
   */
  @Override
  public void onClick(View v) {
    onItemClickCallback.onClick((TransportTag) v.getTag());
  }

  /**
   * Set up MyViewHolder
   */
  public static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView num;
    TextView name;
    TextView time;
    TextView delay;

    /**
     * Find view in item by id
     * @param itemView view of item
     */
    public MyViewHolder(View itemView) {
      super(itemView);
      this.num = itemView.findViewById(R.id.codeRV);
      this.name = itemView.findViewById(R.id.nameSRV);
      this.time = itemView.findViewById(R.id.timeSRV);
      this.delay = itemView.findViewById(R.id.delayS);
    }
  }

  /**
   * Set up StopDetailsListAdapter
   * @param mContext context
   * @param vehicleMode vehicle mode
   * @param tripId ArrayList<String>
   * @param num ArrayList<String>
   * @param name ArrayList<String>
   * @param time ArrayList<String>
   * @param delay ArrayList<String>
   * @param routeDirections ArrayList<String>
   */
  public StopDetailsListAdapter(Context mContext, String vehicleMode, ArrayList<String> tripId, ArrayList<String> num, ArrayList<String> name, ArrayList<String> time, ArrayList<String> delay, ArrayList<String> routeDirections) {
    this.mContext = mContext;
    this.vehicleMode = vehicleMode;
    this.tripId = tripId;
    this.mNum = num;
    this.mName = name;
    this.mTime = time;
    this.mDelay = delay;
    this.mRouteDirections = routeDirections;
  }

  /**
   * get transport color by transport mode
   * @param mode String
   * @return int Color
   */
  int getTransportColor(String mode) {
    switch (mode) {
      default:
      case "BUS":
        return R.color.busColor;
      case "RAIL":
        return R.color.trainColor;
      case "TRAM":
        return R.color.tramColor;
      case "SUBWAY":
        return R.color.subwayColor;
      case "FERRY":
        return R.color.ferryColor;
    }
  }

  /**
   * Set code background
   * @param holder MyViewHolder holder of my view
   * @param colorToSet int
   * @param isColorResource boolean
   */
  void setCodeBackground(MyViewHolder holder, int colorToSet, boolean isColorResource) {
    Drawable background = holder.num.getBackground();
    int color = isColorResource ? ContextCompat.getColor(mContext, colorToSet) : colorToSet;
    if (background instanceof ShapeDrawable) {
      ((ShapeDrawable) background).getPaint().setColor(color);
    } else if (background instanceof GradientDrawable) {
      ((GradientDrawable) background).setColor(color);
    } else if (background instanceof ColorDrawable) {
      ((ColorDrawable) background).setColor(color);
    }
  }

  /**
   * Called when RecyclerView needs a new RecyclerView.ViewHolder to represent an item.
   * @param parent ViewGroup
   * @param viewType int
   * @return myViewHolder
   */
  @Override
  public MyViewHolder onCreateViewHolder(final ViewGroup parent,
                                         final int viewType) {
    final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stop_recyclerview_layout, parent, false);
    MyViewHolder myViewHolder = new MyViewHolder(view);
    return myViewHolder;
  }

  /**
   * setOnItemClickListener callback
   * @param callback OnItemClickCallback
   */
  public void setOnItemClickListener(OnItemClickCallback callback) {
    this.onItemClickCallback = callback;
  }

  /**
   * OnItemClickCallback interface
   */
  public interface OnItemClickCallback {
    void onClick(TransportTag tag);
  }

  /**
   * Put data from arrays to item
   * @param holder MyViewHolder
   * @param i int number of item
   */
  @Override
  public void onBindViewHolder(final MyViewHolder holder, final int i) {
    setCodeBackground(holder,getTransportColor(vehicleMode), true);
    holder.num.setTextColor(Color.WHITE);

    holder.num.setText(mNum.get(i));
    holder.name.setText(mName.get(i));
    holder.time.setText(mTime.get(i));
    holder.delay.setText(mDelay.get(i));
    holder.itemView.setTag(new TransportTag(mNum.get(i), mRouteDirections.get(i)));
    holder.itemView.setOnClickListener(this);
  }

  /**
   * Count items in recycler view
   * @return mNum == null ? 0: mNum.size()
   */
  @Override
  public int getItemCount() {
    return mNum == null ? 0: mNum.size();
  }
}