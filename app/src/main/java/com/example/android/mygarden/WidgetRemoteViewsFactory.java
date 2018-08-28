package com.example.android.mygarden;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;


/**
 * Created by bratner on 27/08/18.
 */

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    Cursor mCursor; //holds the list of what to show in the list/grid widget

    public WidgetRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }


    @Override
    public void onDataSetChanged() {
        Uri query_uri = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();
        //we are going to re-query the provider. Stale data can go to /dev/null
        if (mCursor != null)
            mCursor.close();

        mCursor = mContext.getContentResolver().query(query_uri,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME //Maybe last watered time?
        );
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null)
            return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mCursor == null || mCursor.getCount() == 0)
            return null;
        mCursor.moveToPosition(position);

        int idIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int createTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

        long plantId = mCursor.getLong(idIndex);
        long timeNow = System.currentTimeMillis();
        long wateredAt = mCursor.getLong(waterTimeIndex);
        long createdAt = mCursor.getLong(createTimeIndex);
        int plantType = mCursor.getInt(plantTypeIndex);


        boolean needsWater = (timeNow - wateredAt) > PlantUtils.MIN_AGE_BETWEEN_WATER &&
                (timeNow - wateredAt) < PlantUtils.MAX_AGE_WITHOUT_WATER;
        int imgRes = PlantUtils.getPlantImageRes(mContext, timeNow - createdAt, timeNow - wateredAt, plantType);

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        // Update plant ID text
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        // Show/Hide the water drop button
        if (needsWater) views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);

        Bundle params = new Bundle();
        params.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        Intent fillIntent = new Intent();
        fillIntent.putExtras(params);
        views.setOnClickFillInIntent(R.id.widget_plant_image, fillIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
