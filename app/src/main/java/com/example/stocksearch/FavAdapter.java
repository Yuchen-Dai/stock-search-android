package com.example.stocksearch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.FavViewHolder> {

    private List<Stocks> data;
    private Context context;
    public FavAdapter(List<Stocks> data, Context context){
        this.data =data;
        this.context = context;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.recyclerview_fav, null);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavAdapter.FavViewHolder holder, int position) {
        //String.format(Locale.ENGLISH,"%.2f", )
        holder.symbol.setText(data.get(position).getSymbol());
        holder.current.setText("$" + String.format(Locale.ENGLISH,"%.2f",data.get(position).getCurrent_price()));
//        holder.change.setText("");
        holder.name.setText(data.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return data == null? 0 : data.size();
    }

    public class FavViewHolder extends RecyclerView.ViewHolder {
        private TextView symbol;
        private TextView current;
        private TextView change;
        private TextView name;


        public FavViewHolder(@NonNull View itemView) {
            super(itemView);
            symbol = itemView.findViewById(R.id.fav_symbol);
            current = itemView.findViewById(R.id.fav_price);
            change = itemView.findViewById(R.id.fav_change);
            name = itemView.findViewById(R.id.fav_name);

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
