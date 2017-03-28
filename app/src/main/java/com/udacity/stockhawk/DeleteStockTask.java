package com.udacity.stockhawk;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.udacity.stockhawk.data.Contract;

public class DeleteStockTask extends AsyncTask<String, Void, Void> {
    private final Context mContext;

    public DeleteStockTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... symbol) {

        String selection = Contract.Quote.TABLE_NAME+
                "." + Contract.Quote.COLUMN_SYMBOL + " = ? ";
        String[] selectionArgs = symbol;
        mContext.getContentResolver().delete(Contract.Quote.URI,selection,selectionArgs);
        return null;
    }

}