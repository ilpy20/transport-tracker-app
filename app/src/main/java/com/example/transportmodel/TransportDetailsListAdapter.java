package com.example.transportmodel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.exception.ApolloException;
import com.example.stopmodel.Stop;
import com.example.transporttracker.MainActivity;
import com.example.transporttracker.R;
import com.hsl.StopDetailsQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TransportDetailsListAdapter extends RecyclerView.Adapter<TransportDetailsListAdapter.MyViewHolder> {
  private Context mContext;
  private ArrayList<Drawable> mMode;
  private ArrayList<String> stopId;
  private ArrayList<String> mCode;
  private ArrayList<String> mName;
  private ArrayList<String> mZone;
  private ArrayList<String> mPlatform;
  private ArrayList<String> mTime;
  private ArrayList<String> mDelay;

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView code;
    TextView name;
    TextView zone;
    TextView platform;
    TextView time;
    TextView delay;
    ImageView imgMode;

    public MyViewHolder(View itemView) {
      super(itemView);
      this.imgMode = itemView.findViewById(R.id.modeRRV);
      this.code = itemView.findViewById(R.id.numRRV);
      this.name = itemView.findViewById(R.id.nameRRV);
      this.zone = itemView.findViewById(R.id.zoneRRV);
      this.platform = itemView.findViewById(R.id.platformRRV);
      this.time = itemView.findViewById(R.id.timeRRV);
      this.delay = itemView.findViewById(R.id.delayR);
    }
  }

  public TransportDetailsListAdapter(Context mContext, ArrayList<String> stopId,ArrayList<String> code, ArrayList<String> name, ArrayList<String> zone,ArrayList<String> platform, ArrayList<String> time, ArrayList<String> delay) {
    this.mContext = mContext;
    this.stopId = stopId;
    //this.mMode = mode;
    this.mCode = code;
    this.mName = name;
    this.mZone = zone;
    this.mPlatform = platform;
    this.mTime = time;
    this.mDelay = delay;
  }

  int getTransportColor(String mode) {
    switch (mode) {
      default:
      case "bus":
        return R.color.busColor;
      case "train":
        return R.color.trainColor;
      case "tram":
        return R.color.tramColor;
      case "metro":
        return R.color.subwayColor;
      case "ferry":
        return R.color.ferryColor;
    }
  }

  void setCodeBackground(MyViewHolder holder, int colorToSet, boolean isColorResource) {
    Drawable background = holder.code.getBackground();
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
    final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_recyclerview_layout, parent, false);
    MyViewHolder myViewHolder = new MyViewHolder(view);
    return myViewHolder;
  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, final int i) {
    //holder.imgMode.setImageDrawable(mMode.get(i));

    setCodeBackground(holder,Color.GRAY, false);
    holder.code.setTextColor(Color.WHITE);
    holder.code.setText(mCode.get(i));
    holder.name.setText(mName.get(i));
    holder.zone.setBackground(mContext.getResources().getDrawable(R.drawable.stop_icon,mContext.getTheme()));
    holder.zone.setTextColor(Color.WHITE);
    holder.zone.setText(mZone.get(i));
    holder.platform.setText(mPlatform.get(i));
    holder.time.setText(mTime.get(i));
    holder.delay.setText(mDelay.get(i));

    holder.itemView.setOnClickListener(v -> {
      Stop.makeStop(stopId.get(i), new Stop.Callback() {
        @Override
        public void onStop(@NonNull StopDetailsQuery.Data data) {
          //data.stop().code();
          //data.stop().name();
          //data.stop().zoneId();
          //data.stop().platformCode();
          //if (mContext instanceof MainActivity) {
          //  ((MainActivity)mContext).getStopDetails(data);
          //}
        }

        @Override
        public void onError(@NotNull ApolloException e) {
        }
      });
      //Toast.makeText(mContext, "Position : " + i, Toast.LENGTH_LONG).show();
    });

  }

  @Override
  public int getItemCount() {
    return mCode == null ? 0 : mCode.size();
  }
}
