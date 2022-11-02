package com.example.stocksearch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class PortAdapter extends RecyclerView.Adapter<PortAdapter.PortViewHolder> {

    private List<Stocks> data;
    private Context context;
    public PortAdapter(List<Stocks> data, Context context){
        this.data =data;
        this.context = context;
    }


    @NonNull
    @Override
    public PortViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.recyclerview_port, null);
        return new PortViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortAdapter.PortViewHolder holder, int position) {
        //String.format(Locale.ENGLISH,"%.2f", )
        holder.symbol.setText(data.get(position).getSymbol());
        holder.current.setText("$" + String.format(Locale.ENGLISH,"%.2f",data.get(position).getTotal_cost()));
//        holder.change.setText("");
        holder.shares.setText(data.get(position).getShares() + " shares");

    }

    @Override
    public int getItemCount() {
        return data == null? 0 : data.size();
    }

    public class PortViewHolder extends RecyclerView.ViewHolder {
        private TextView symbol;
        private TextView current;
        private TextView change;
        private TextView shares;


        public PortViewHolder(@NonNull View itemView) {
            super(itemView);
            symbol = itemView.findViewById(R.id.port_symbol);
            current = itemView.findViewById(R.id.port_price);
            change = itemView.findViewById(R.id.port_change);
            shares = itemView.findViewById(R.id.port_share);

            itemView.setOnClickListener(view -> {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onRecyclerItemClick(getAdapterPosition());
                }
            });
        }
    }

    private OnRecyclerItemClickListener mOnItemClickListener;

    public void setRecyclerItemClickListener(OnRecyclerItemClickListener listener){
        mOnItemClickListener = listener;
    }

    public interface OnRecyclerItemClickListener{
        void onRecyclerItemClick(int position);
    }

}
