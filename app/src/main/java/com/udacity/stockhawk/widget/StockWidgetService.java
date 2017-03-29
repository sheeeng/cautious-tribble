package com.udacity.stockhawk.widget;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.StockDetailsActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockWidgetViewFactory(getApplicationContext());
    }

    private class StockWidgetViewFactory implements RemoteViewsFactory {
        private final Context mContext;
        private DecimalFormat dollarFormat;
        private DecimalFormat dollarFormatWithPlus;
        private DecimalFormat percentageFormat;
        private List<ContentValues> mCvList = new ArrayList<>();

        public StockWidgetViewFactory(Context context) {
            mContext = context;

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onCreate() {
            setData();
        }

        private void setData() {
            mCvList.clear();

            long identity = Binder.clearCallingIdentity();

            try {
                ContentResolver contentResolver = mContext.getContentResolver();

                Cursor cursor = contentResolver.query(
                        Contract.Quote.URI,
                        null,
                        null,
                        null,
                        null
                );

                while (cursor.moveToNext()) {
                    String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                    float price = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                    float absoluteChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                    float percentageChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                    ContentValues cv = new ContentValues();

                    cv.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    cv.put(Contract.Quote.COLUMN_PRICE, price);
                    cv.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, absoluteChange);
                    cv.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentageChange);

                    Timber.d(symbol + " - "+ String.valueOf(price));

                    mCvList.add(cv);
                }

                cursor.close();
            }
            finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @Override
        public void onDataSetChanged() {
            setData();
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mCvList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            ContentValues cv = mCvList.get(position);
            RemoteViews remoteViews = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.list_item_quote);

            String symbol = cv.getAsString(Contract.Quote.COLUMN_SYMBOL);
            Timber.d("String.valueOf(symbol): %s", String.valueOf(symbol));

            remoteViews.setTextViewText(
                    R.id.symbol,
                    symbol);

            remoteViews.setTextViewText(R.id.price,
                    dollarFormat.format(
                            cv.getAsFloat(
                                    Contract.Quote.COLUMN_PRICE)
                    )
            );

            float absChange = cv.getAsFloat(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
            Timber.d("String.valueOf(absChange): %s", String.valueOf(absChange));
            float perChange = cv.getAsFloat(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);
            Timber.d("String.valueOf(perChange): %s", String.valueOf(perChange));

            if (absChange > 0) {
                remoteViews.setInt(
                        R.id.change,
                        "setBackgroundResource",
                        R.drawable.percent_change_pill_green);
            } else {
                remoteViews.setInt(
                        R.id.change,
                        "setBackgroundResource",
                        R.drawable.percent_change_pill_red);
            }

            String percentageChange = percentageFormat.format(perChange / 100);
            Timber.d("String.valueOf(percentageChange): %s", String.valueOf(percentageChange));
            remoteViews.setTextViewText(
                    R.id.change,
                    percentageChange);

            Intent fillIntent = new Intent(mContext, StockDetailsActivity.class);
            fillIntent.putExtra(StockWidgetProvider.EXTRA_SYMBOL, symbol);
            remoteViews.setOnClickFillInIntent(R.id.list_item, fillIntent);

            return remoteViews;
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
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
