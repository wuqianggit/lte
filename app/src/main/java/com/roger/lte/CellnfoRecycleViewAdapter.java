package com.roger.lte;

/**
 * Created by 47641 on 2017/8/3.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class CellnfoRecycleViewAdapter extends RecyclerView.Adapter<CellnfoRecycleViewAdapter.MyViewHolder> {
    private List<CellGeneralInfo> itemsData;
    private Context mContext;
    private LayoutInflater inflater;

    public CellnfoRecycleViewAdapter(Context context, List<CellGeneralInfo> itemsData) {
        this.itemsData = itemsData;
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.tvType.setText(String.valueOf(itemsData.get(position).type));
        holder.tvTac.setText(String.valueOf(itemsData.get(position).tac));
        holder.tvCId.setText(String.valueOf(itemsData.get(position).CId));
        holder.tvPCI.setText(String.valueOf(itemsData.get(position).pci));
        holder.tvdBm.setText(String.valueOf(itemsData.get(position).lac));
        holder.tvLevel.setText(String.valueOf(itemsData.get(position).asulevel));

    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvType;
        public TextView tvTac;
        public TextView tvCId;
        public TextView tvPCI;
        public TextView tvdBm;
        public TextView tvLevel;

        public MyViewHolder(View view) {
            super(view);
            tvType = (TextView) view.findViewById(R.id.tvCellType);
            tvCId = (TextView) view.findViewById(R.id.tvCellId);
            tvPCI = (TextView) view.findViewById(R.id.tvPCI);
            tvTac = (TextView) view.findViewById(R.id.tvTac);
            tvdBm = (TextView) view.findViewById(R.id.tvhistorydBm);
            tvLevel = (TextView) view.findViewById(R.id.tvhistoryasuLevel);
        }
    }
}