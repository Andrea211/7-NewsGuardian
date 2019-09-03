package com.example.newsguardian.widgets;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

import com.example.newsguardian.R;
import com.example.newsguardian.constants.Constants;
import com.example.newsguardian.data.News;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    final private Context context;
    final private List<News> favoriteNews;
    private CountDownLatch countDownLatch;


    public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
        context = applicationContext;
        favoriteNews = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        countDownLatch = new CountDownLatch(1);
        if (isConnected()) {
            readDataFromDatabase();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(context.getPackageName(), "trying onDataSet ");
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (favoriteNews == null) return 0;
        return favoriteNews.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (favoriteNews.isEmpty()) return null;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorite_news_widget);

        News currentNews = favoriteNews.get(position);
        views.setTextViewText(R.id.newsTitle_textView, currentNews.getTitle());

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void readDataFromDatabase() {
        favoriteNews.clear();
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_REFERENCE_PATH).child(Constants.DATABASE_NEWS_CHILD).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                    favoriteNews.add(dataSnapshotChild.getValue(News.class));
                }
                countDownLatch.countDown();
                Log.d(context.getPackageName(), "trying readDataFromDatabase() last ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(context.getPackageName(), "Failed to read value.", databaseError.toException());
            }
        });
    }
}

