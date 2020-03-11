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

  @Override
  public void onClick(View v) {
    onItemClickCallback.onClick((TransportTag) v.getTag());
  }

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView num;
    TextView name;
    TextView time;
    TextView delay;

    public MyViewHolder(View itemView) {
      super(itemView);
      this.num = itemView.findViewById(R.id.codeRV);
      this.name = itemView.findViewById(R.id.nameSRV);
      this.time = itemView.findViewById(R.id.timeSRV);
      this.delay = itemView.findViewById(R.id.delayS);
    }
  }

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

  @Override
  public MyViewHolder onCreateViewHolder(final ViewGroup parent,
                                         final int viewType) {
    final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stop_recyclerview_layout, parent, false);
    MyViewHolder myViewHolder = new MyViewHolder(view);
    return myViewHolder;
  }

  public void setOnItemClickListener(OnItemClickCallback callback) {
    this.onItemClickCallback = callback;
  }

  public interface OnItemClickCallback {
    void onClick(TransportTag tag);
  }

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

  @Override
  public int getItemCount() {
    return mNum == null ? 0: mNum.size();
  }
}