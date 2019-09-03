package com.example.newsguardian.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsguardian.R;
import com.example.newsguardian.data.News;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    final private NewsAdapterOnClickHandler clickHandler;
    final private Context context;
    private List<News> news;

    public NewsAdapter(Context context, NewsAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    // Create an empty view holder to populate it later
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.list_item, viewGroup, false);
        return new NewsViewHolder(view);
    }

    // Create a method to bind an empty view holder with data
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int i) {
        News currentNews = news.get(i);
        if (!currentNews.getThumbnail().isEmpty()) {
            Picasso.get().load(currentNews.getThumbnail()).into(holder.newsImageView);
        }
        holder.sectionTextView.setText(currentNews.getSection());
        holder.titleTextView.setText(currentNews.getTitle());
        holder.authorsTextView.setText(currentNews.getAuthors());

        // Split the date so it can be presented in two separate lines
        String newsDate = currentNews.getDate();
        String[] parts = newsDate.split("T");
        parts[1] = parts[1].substring(0, parts[1].length() - 1);
        holder.dateTextView.setText(parts[1] + "\n" + parts[0]);
    }

    @Override
    public int getItemCount() {
        if (news == null) return 0;
        return news.size();
    }

    public void setNewsData(List<News> news) {
        this.news = news;
        notifyDataSetChanged();
    }

    public void clear() {
        final int size = getItemCount();
        this.news.clear();
        notifyItemRangeRemoved(0, size);
    }

    // This interface reveives on click actions
    public interface NewsAdapterOnClickHandler {
        void onClickFavorite(News news, View view);

        void onClickNewsHolder(News news, View view);
    }

    // Create a view holder
    public class NewsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.news_imageView)
        ImageView newsImageView;
        @BindView(R.id.section_textView)
        TextView sectionTextView;
        @BindView(R.id.title_textView)
        TextView titleTextView;
        @BindView(R.id.authors_textView)
        TextView authorsTextView;
        @BindView(R.id.date_textView)
        TextView dateTextView;
        @BindView(R.id.favoriteActionButton)
        FloatingActionButton favoriteButton;
        @BindView(R.id.newsHolder)
        ConstraintLayout constraintLayout;

        private NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            constraintLayout.setOnClickListener(view -> {
                News currentNews = news.get(getAdapterPosition());
                clickHandler.onClickNewsHolder(currentNews, view);
            });

            favoriteButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                News currentNews = news.get(position);
                clickHandler.onClickFavorite(currentNews, view);
            });
        }
    }
}
