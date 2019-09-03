package com.example.newsguardian.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsguardian.R;
import com.example.newsguardian.adapters.NewsAdapter;
import com.example.newsguardian.constants.Constants;
import com.example.newsguardian.data.News;
import com.example.newsguardian.loaders.NewsLoader;
import com.example.newsguardian.widgets.FavoriteNewsWidget;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>,
        NewsAdapter.NewsAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static DatabaseReference databaseReference;

    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.error_message_textView)
    TextView errorMessageTextView;
    @BindView(R.id.anim_toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;

    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;

    private Parcelable recyclerState;

    private boolean appBarExpanded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use Butterknife library
        ButterKnife.bind(this);

        // Use a collapsing toolbar
        setSupportActionBar(toolbar);
        collapsingToolbar.setTitle("News Guardian");

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.guardianlogo);
        Palette.from(bitmap).generate(palette -> {
            int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.colorPrimary));
            collapsingToolbar.setContentScrimColor(vibrantColor);
            collapsingToolbar.setStatusBarScrimColor(getResources().getColor(R.color.colorPrimaryDark));
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) > 200) {
                appBarExpanded = false;
            } else {
                appBarExpanded = true;
            }
            invalidateOptionsMenu();
        });

        // Create a Firebase database
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Constants.DATABASE_REFERENCE_PATH).child(Constants.DATABASE_NEWS_CHILD);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        newsAdapter = new NewsAdapter(this, this);

        recyclerView.setAdapter(newsAdapter);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Check connection
        if (isConnected()) {
            LoaderManager.getInstance(this).initLoader(Constants.NEWS_LOADER_ID, null, MainActivity.this);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            showErrorMessage();
        }

        if (savedInstanceState != null) {
            recyclerState = savedInstanceState.getParcelable(Constants.RECYCLER_VIEW_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<List<News>> onCreateLoader(int i, @Nullable Bundle bundle) {
        loadingIndicator.setVisibility(View.VISIBLE);
        String urlWithApiKey = Constants.REQUEST_URL + getString(R.string.api_key);

        return new NewsLoader(this, urlWithApiKey);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<News>> loader, List<News> news) {
        loadingIndicator.setVisibility(View.INVISIBLE);
        newsAdapter.setNewsData(news);
        // Handle rotation
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
        if (news == null) {
            showErrorMessage();
        } else {
            showNewsDataView();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<News>> loader) {
        newsAdapter.clear();
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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
    protected void onStart() {
        super.onStart();
        LoaderManager.getInstance(this).restartLoader(Constants.NEWS_LOADER_ID, null, MainActivity.this);

    }

    @Override
    public void onClickFavorite(News news, View view) {
        if (isConnected()) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                boolean newsExist = false;

                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.getValue(News.class).getNewsId().equals(news.getNewsId())) {
                            newsExist = true;
                            Toast.makeText(MainActivity.this, getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (!newsExist) {
                        databaseReference.push().setValue(news);
                        Toast.makeText(MainActivity.this, getString(R.string.saved_to_favorite), Toast.LENGTH_SHORT).show();

                        // Update app widgets if any news added to favorite
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(MainActivity.this, FavoriteNewsWidget.class));
                        //Trigger data update to handle the ListView widgets and force a data refresh
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view_widget);
                    }
                }

                @Override
                public void onCancelled(final DatabaseError databaseError) {
                    Log.w(TAG, "Unable to read value", databaseError.toException());
                }
            });

        } else {
            Toast.makeText(this, getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClickNewsHolder(News news, View view) {
        Uri newsUri = Uri.parse(news.getUrl());
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
        startActivity(websiteIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.RECYCLER_VIEW_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
    }
}