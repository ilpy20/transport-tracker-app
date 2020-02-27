package com.example.stopmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import androidx.recyclerview.widget.RecyclerView;

import com.example.transporttracker.R;

import java.util.ArrayList;

public class StopDetailsListAdapter extends RecyclerView.Adapter<StopDetailsListAdapter.MyViewHolder> {
  private Context mContext;
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

  public StopDetailsListAdapter(Context mContext,ArrayList<String> num,ArrayList<String> name,ArrayList<String> time,ArrayList<String> delay) {
    this.mContext = mContext;
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
    holder.num.setText(mNum.get(i));
    holder.name.setText(mName.get(i));
    holder.time.setText(mTime.get(i));
    holder.delay.setText(mDelay.get(i));

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(mContext,"Position : "+i,Toast.LENGTH_LONG).show();
      }
    });

  }

  @Override
  public int getItemCount() {
    return mNum == null ? 0: mNum.size();
  }
}