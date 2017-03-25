package com.udacity.stockhawk.data;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();

    // No need to use "static final" attributes because this is an interface an not a class.
    public interface INTENTS {
        String STOCK_DETAILS = "STOCK_DETAILS";
        String EXTRA_STOCK_SYMBOL = "EXTRA_STOCK_SYMBOL";
    }
}
