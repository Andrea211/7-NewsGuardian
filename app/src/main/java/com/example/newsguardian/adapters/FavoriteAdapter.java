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

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    final private FavoriteAdapterOnClickHandler clickHandler;
    final private Context context;
    private List<News> favoriteNews;

    public FavoriteAdapter(Context context, FavoriteAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    // Create an empty view holder to populate it later
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.favorite_list_item, viewGroup, false);
        return new FavoriteViewHolder(view);
    }

    // Create a method to bind an empty view holder with data
    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int i) {
        News currentNews = favoriteNews.get(i);
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
        if (favoriteNews == null) return 0;
        return favoriteNews.size();
    }

    public void setFavoriteNewsData(List<News> favoriteNews) {
        this.favoriteNews = favoriteNews;
        notifyDataSetChanged();
    }

    public void clear() {
        final int size = getItemCount();
        this.favoriteNews.clear();
        notifyItemRangeRemoved(0, size);
    }

    // This interface reveives on click action
    public interface FavoriteAdapterOnClickHandler {
        void onClickNewsHolder(News news, View view);
    }

    // Create a view holder
    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.favoriteNews_imageView)
        ImageView newsImageView;
        @BindView(R.id.favorite_section_textView)
        TextView sectionTextView;
        @BindView(R.id.favorite_title_textView)
        TextView titleTextView;
        @BindView(R.id.favorite_authors_textView)
        TextView authorsTextView;
        @BindView(R.id.favorite_date_textView)
        TextView dateTextView;
        @BindView(R.id.favoriteNewsHolder)
        ConstraintLayout constraintLayout;

        private FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            constraintLayout.setOnClickListener(view -> {
                News currentNews = favoriteNews.get(getAdapterPosition());
                clickHandler.onClickNewsHolder(currentNews, view);
            });
        }
    }
}
