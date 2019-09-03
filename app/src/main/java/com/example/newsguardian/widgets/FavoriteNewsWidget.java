package com.example.newsguardian.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.newsguardian.R;

public class FavoriteNewsWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorite_widget_list);

        Intent intent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.list_view_widget, intent);

        views.setEmptyView(R.id.list_view_widget, R.id.empty_view);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
    }
}

