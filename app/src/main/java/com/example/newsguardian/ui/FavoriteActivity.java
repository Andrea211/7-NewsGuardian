package com.example.newsguardian.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsguardian.R;
import com.example.newsguardian.adapters.FavoriteAdapter;
import com.example.newsguardian.constants.Constants;
import com.example.newsguardian.data.News;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.FavoriteAdapterOnClickHandler {

    private static final String TAG = FavoriteActivity.class.getSimpleName();

    @BindView(R.id.favorite_loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.favorite_error_message_textView)
    TextView errorMessageTextView;
    @BindView(R.id.favorite_toolbar)
    Toolbar toolbar;

    private FavoriteAdapter favoriteAdapter;
    private RecyclerView recyclerView;
    private List<News> favoriteNews;
    private Parcelable recyclerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Use Butterknife library
        ButterKnife.bind(this);

        // Use "normal" toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favoriteNews = new ArrayList<>();

        recyclerView = findViewById(R.id.favorite_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoriteAdapter = new FavoriteAdapter(this, this);

        recyclerView.setAdapter(favoriteAdapter);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Check connection
        if (isConnected()) {
            readDataFromDatabase();
        } else {
            loadingIndicator.setVisibility(View.INVISIBLE);
            errorMessageTextView.setText(R.string.check_internet_connection);
            showErrorMessage();
        }

        if (savedInstanceState != null) {
            recyclerState = savedInstanceState.getParcelable(Constants.RECYCLER_VIEW_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
        }

    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void readDataFromDatabase() {
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_REFERENCE_PATH).child(Constants.DATABASE_NEWS_CHILD).addValueEventListener(new ValueEventListener() {

            // Call method at the begging and every time the database has changed
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                    favoriteNews.add(dataSnapshotChild.getValue(News.class));
                }

                loadingIndicator.setVisibility(View.INVISIBLE);
                favoriteAdapter.setFavoriteNewsData(favoriteNews);
                // Handle rotation
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
                showNewsDataView();

                if (favoriteNews.isEmpty()) {
                    // Show text message that the data is empty
                    errorMessageTextView.setText(getString(R.string.no_data_favorite));
                    showErrorMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Unable to read value.", databaseError.toException());
            }
        });
    }

    @Override
    public void onClickNewsHolder(News news, View view) {
        Uri newsUri = Uri.parse(news.getUrl());
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
        startActivity(websiteIntent);
    }

    private void showNewsDataView() {
        errorMessageTextView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.RECYCLER_VIEW_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
    }
}