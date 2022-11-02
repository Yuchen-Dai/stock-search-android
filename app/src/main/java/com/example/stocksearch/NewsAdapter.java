package com.example.stocksearch;

import android.content.Context;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<News> data;
    private Context context;
    public NewsAdapter(List<News> data, Context context){
        this.data =data;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.recyclerview_news, null);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.NewsViewHolder holder, int position) {
        holder.title.setText(data.get(position).getTitle());
        holder.source.setText(data.get(position).getSource());
        double unix_date = data.get(position).getDate();
        java.util.Date time = new java.util.Date((long)unix_date*1000);

        Date today = new Date();
        long diff_seconds = (today.getTime() - time.getTime())/1000;
        int diff_hours = (int) (diff_seconds / 3600);
        String temp =  diff_hours + " hours ago";
        holder.datetime.setText(temp);
        Glide.with(context)
                .load(data.get(position).getUrl())
                .placeholder(R.drawable.ic_app_playstore)
                .fitCenter()
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return data == null? 0 : data.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView source;
        private TextView datetime;
        private ImageView img;
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            source = itemView.findViewById(R.id.news_source);
            datetime = itemView.findViewById(R.id.news_datetime);
            img = itemView.findViewById(R.id.news_img);

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
