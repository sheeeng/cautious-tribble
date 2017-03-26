package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Constants;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailsActivity extends Activity {



    @BindView(R.id.tv_stock_details_symbol)
    TextView textViewSymbol;

    @BindView(R.id.chart)
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        ButterKnife.bind(this);

        String stringStockSymbol = getIntent().getStringExtra(Constants.INTENTS.EXTRA_STOCK_SYMBOL);
        textViewSymbol.setText(stringStockSymbol);

        showHistory(stringStockSymbol);
    }

    private void showHistory(String symbol) {
        String history = getHistoryString(symbol);

        List<String[]> lines = getLines(history);

        ArrayList<Entry> entries = new ArrayList<>(lines.size());

        final ArrayList<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;

        for (int i = lines.size() - 1; i >= 0; i--) {
            String[] line = lines.get(i);

            // setup xAxis
            xAxisValues.add(Long.valueOf(line[0]));
            xAxisPosition++;

            // add entry data
            Entry entry = new Entry(
                    xAxisPosition, // timestamp
                    Float.valueOf(line[1])  // symbol value
            );
            entries.add(entry);
        }

        setupChart(symbol, entries, xAxisValues);
    }

    private void setupChart(String symbol, List<Entry> entries, final List<Long> xAxisValues) {
        LineData lineData = new LineData(new LineDataSet(entries, symbol));
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Long dateLong = xAxisValues.get( Math.round(value) );
                Date date = new Date(dateLong);
                return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        .format(date);
            }
        });
    }

    @Nullable
    private List<String[]> getLines(String history) {
        List<String[]> lines = null;
        CSVReader reader = new CSVReader(new StringReader(history));
        try {
            lines = reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private String getHistoryString(String symbol) {
        Cursor cursor = getContentResolver().query(
                Contract.Quote.makeUriForStock(symbol),
                null,
                null,
                null,
                null);
        String history = "";
        if (cursor.moveToFirst()) {
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }
        return history;
    }
}
