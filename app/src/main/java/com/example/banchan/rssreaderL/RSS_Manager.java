package com.example.banchan.rssreaderL;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

public class RSS_Manager extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss__manager);

        getActionBar().setDisplayHomeAsUpEnabled(true);


        ListView LV = (ListView) findViewById(R.id.lv_RSS_mgr);

        DatabaseHelper DBH = new DatabaseHelper(RSS_Manager.this);
        final NewsManagingAdapter NMA = new NewsManagingAdapter(this, DBH.getUriDataAll());
        LV.setAdapter(NMA);
        DBH.close();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //  action barの「戻る」アイコン
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
