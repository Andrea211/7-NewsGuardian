package com.example.newsguardian.loaders;

import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import com.example.newsguardian.data.News;
import com.example.newsguardian.utils.QueryUtils;

import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<News>> {

    private List<News> newsData = null;
    private final String url;

    public NewsLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    // onStartLoading() includes method forceLoad() that enables us to use background thread
    @Override
    protected void onStartLoading() {
        if (newsData != null) {
            deliverResult(newsData);
        } else {
            forceLoad();
        }
    }

    // Load data in background
    @Override
    public List<News> loadInBackground() {
        try {
            return QueryUtils.fetchNewsData(url, getContext());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Deliver result to specified listener
    @Override
    public void deliverResult(List<News> data) {
        newsData = data;
        super.deliverResult(data);
    }
}