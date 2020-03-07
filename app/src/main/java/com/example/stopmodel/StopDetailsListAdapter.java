package com.example.stopmodel;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.exception.ApolloException;
import com.example.transportmodel.Transport;
import com.example.transporttracker.MainActivity;
import com.example.transporttracker.R;
import com.hsl.TransportDetailsFromMapQuery;
import com.hsl.TransportDetailsFromStopQuery;
import com.hsl.type.Mode;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.graphics.Color.parseColor;

public class StopDetailsListAdapter extends RecyclerView.Adapter<StopDetailsListAdapter.MyViewHolder> {
  private Context mContext;
  private String vehicleMode;
  private ArrayList<String> tripId;
  private ArrayList<String> mNum;
  private ArrayList<String> mName;
  private ArrayList<String> mTime;
  private ArrayList<String> mDelay;

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

  public StopDetailsListAdapter(Context mContext, String vehicleMode, ArrayList<String> tripId, ArrayList<String> num, ArrayList<String> name, ArrayList<String> time, ArrayList<String> delay) {
    this.mContext = mContext;
    this.vehicleMode = vehicleMode;
    this.tripId = tripId;
    this.mNum = num;
    this.mName = name;
    this.mTime = time;
    this.mDelay = delay;
  }

  @Override
  public MyViewHolder onCreateViewHolder(final ViewGroup parent,
                                         final int viewType) {
    final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stop_recyclerview_layout, parent, false);
    MyViewHolder myViewHolder = new MyViewHolder(view);
    return myViewHolder;
  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, final int i) {
    if(vehicleMode.equals("BUS")){
      holder.num.setBackgroundColor(Color.BLUE);
      holder.num.setTextColor(Color.WHITE);
    }
    if(vehicleMode.equals("RAIL")){
      holder.num.setBackgroundColor(Color.MAGENTA);
      holder.num.setTextColor(Color.WHITE);
    }
    if(vehicleMode.equals("TRAM")){
      holder.num.setBackgroundColor(Color.GREEN);
      holder.num.setTextColor(Color.WHITE);
    }
    if(vehicleMode.equals("SUBWAY")){
      holder.num.setBackgroundColor(parseColor("#FF4F00"));
      holder.num.setTextColor(Color.WHITE);
    }
    if(vehicleMode.equals("FERRY")){
      holder.num.setBackgroundColor(parseColor("#ADD8E6"));
      holder.num.setTextColor(Color.WHITE);
    }
    holder.num.setText(mNum.get(i));
    holder.name.setText(mName.get(i));
    holder.time.setText(mTime.get(i));
    holder.delay.setText(mDelay.get(i));

    holder.itemView.setOnClickListener(v -> {
      Transport.getTransportDetailsFromStop(tripId.get(i), new Transport.Callback() {
        @Override
        public void onTransportFromMap(@NonNull TransportDetailsFromMapQuery.Data data) {
        }

        @Override
        public void onTransportFromStop(@NonNull TransportDetailsFromStopQuery.Data data) {
          //data.trip().routeShortName();
          //data.trip().tripHeadsign();
          //if (mContext instanceof MainActivity) {
          //  ((MainActivity)mContext).getTransportDetailsFromStop(data);
          //}
        }

        @Override
        public void onError(@NotNull ApolloException e) {

        }
      });
      //Toast.makeText(mContext,"Position : "+i,Toast.LENGTH_LONG).show();
    });

  }

  @Override
  public int getItemCount() {
    return mNum == null ? 0: mNum.size();
  }
}