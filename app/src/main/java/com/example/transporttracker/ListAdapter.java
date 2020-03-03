package com.example.transporttracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {
  private Context mContext;
  private ArrayList<String> mMode;
  private ArrayList<String> mNum;
  private ArrayList<String> mName;
  private ArrayList<String> mTime;

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView num;
    TextView name;
    ImageView imgMode;

    public MyViewHolder(View itemView) {
      super(itemView);

      this.num = (TextView) itemView.findViewById(R.id.numRV);
      this.name = (TextView) itemView.findViewById(R.id.nameRV);
      this.imgMode = (ImageView) itemView.findViewById(R.id.modeRV);
    }
  }

  public ListAdapter(Context mContext,ArrayList<String> num,ArrayList<String> name) {
    this.mContext = mContext;
    //this.mMode = mode;
    this.mNum = num;
    this.mName = name;
  }

  @Override
  public MyViewHolder onCreateViewHolder(final ViewGroup parent,
                                         final int viewType) {
    final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_layout, parent, false);
    MyViewHolder myViewHolder = new MyViewHolder(view);
    return myViewHolder;
  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, final int i) {
    holder.num.setText(mNum.get(i));
    holder.name.setText(mName.get(i));


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