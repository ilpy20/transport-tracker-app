package com.example.transportmodel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.recyclerview.widget.RecyclerView;

import com.example.transporttracker.R;

import java.util.ArrayList;

public class TransportDetailsListAdapter extends RecyclerView.Adapter<TransportDetailsListAdapter.MyViewHolder> {
  private Context mContext;
  private ArrayList<Drawable> mMode;
  private ArrayList<String> mCode;
  private ArrayList<String> mName;
  private ArrayList<String> mZone;
  private ArrayList<String> mTime;
  private ArrayList<String> mDelay;

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView code;
    TextView name;
    TextView zone;
    TextView time;
    TextView delay;
    ImageView imgMode;

    public MyViewHolder(View itemView) {
      super(itemView);
      this.imgMode = (ImageView) itemView.findViewById(R.id.modeRRV);
      this.code = (TextView) itemView.findViewById(R.id.codeRV);
      this.name = (TextView) itemView.findViewById(R.id.nameRRV);
      this.zone = itemView.findViewById(R.id.zoneRRV);
      this.time = itemView.findViewById(R.id.timeRRV);
      this.delay = itemView.findViewById(R.id.delayR);
    }
  }

  public TransportDetailsListAdapter(Context mContext, ArrayList<Drawable> mode, ArrayList<String> code, ArrayList<String> name, ArrayList<String> zone, ArrayList<String> time, ArrayList<String> delay) {
    this.mContext = mContext;
    this.mMode = mode;
    this.mCode = code;
    this.mName = name;
    this.mZone = zone;
    this.mTime = time;
    this.mDelay = delay;
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
    holder.imgMode.setImageDrawable(mMode.get(i));
    holder.code.setText(mCode.get(i));
    holder.name.setText(mName.get(i));
    holder.zone.setText(mZone.get(i));
    holder.time.setText(mTime.get(i));
    holder.delay.setText(mDelay.get(i));

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(mContext, "Position : " + i, Toast.LENGTH_LONG).show();
      }
    });

  }

  @Override
  public int getItemCount() {
    return mCode == null ? 0 : mCode.size();
  }
}
