package com.example.banchan.rssreaderL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    ListView LV_W;
    MenuItem menu0;
    android.app.ActionBar actionBar;
    NewsLineAdapter mNLAdapter;

    //  drawerのコントロール
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawer;
    private ListView mDrawerList;

    //private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getActionBar();

        LV_W=(ListView)findViewById(R.id.lv_w);

        //  最後に見たニュースを表示
        reloadLastContent();

        registerForContextMenu(LV_W); // コンテキストメニューの呼び出し元登録
        /*
        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録
        LV_W.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                newsShareDialog(MainActivity.this, parent.getAdapter().getItem(position).toString());
            }
        });
        */

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        final DatabaseHelper DBH = new DatabaseHelper(MainActivity.this);
        if( ! Constants.getPrefrenceBoolean(MainActivity.this, Constants.DB_INITIALIZED, false)){

            int rtn = DBH.initialSetting(getResources().obtainTypedArray(R.array.rss_uri));
            Toast.makeText(this, "初期化しています " + rtn, Toast.LENGTH_SHORT).show();

        }

        final NewsSelectAdapter NSA = new NewsSelectAdapter(this, DBH.getUriData(1));
        mDrawerList.setAdapter(NSA);
        DBH.close();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                showNews2(NSA.getItemMember(position, 0),NSA.getItemMember(position, 1));
                        //Toast.makeText(MainActivity.this, NSA.getItemMember(position, 1), Toast.LENGTH_SHORT).show();
                        mDrawer.closeDrawers();
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawer,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                0,  /* "open drawer" description */
                0  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                setTitle("RSS Reader LITE");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                setTitle("RSSを選択");
            }
        };

        mDrawer.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected void onResume(){
        super.onResume();

        //  onCreateからこちらへ移動
        DatabaseHelper DBH = new DatabaseHelper(MainActivity.this);
        final NewsSelectAdapter NSA = new NewsSelectAdapter(this, DBH.getUriData(1));
        mDrawerList.setAdapter(NSA);
        DBH.close();


    }

    @Override
    public void onCreateContextMenu
            (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //■■■■ registerForContextMenu()で登録したViewが長押しされると、
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.share:
                //  共有
                //newsShareDialog(MainActivity.this, mNLAdapter.getItem(info.position).toString());

                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT,
                        ">> " + actionBar.getSubtitle()  + " から引用\n" +
                        mNLAdapter.getItem(info.position).toString());
                startActivity(intent1);

                break;

        }

        return true;
    }   //  Context メニュー

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //■■■■   オプションメニュー表示
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu0 = (MenuItem)menu.findItem(R.id.weather);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

            menu0.setTitle("天気予報 " + Constants.getPrefrenceString(this, Constants.MY_AREA_NAME, "東京"));
            mDrawer.closeDrawers();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        // ActionBarDrawerToggleにandroid.id.home(up ナビゲーション)を渡す。
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //■■■■   オプションメニューのイベント処理
        //String a1="";
        switch (item.getItemId()){

            case R.id.weather:
                showWeatherReport();

                break;

            case R.id.reload:
                //  再読み込み
                reloadLastContent();

                break;

            case R.id.settingFG:
                //  設定
                Intent intent = new Intent(this, SettingFGActivity.class);
                startActivity(intent);
                //finish();
                break;
/*
            case R.id.addUri:
                //  RSSの登録
                Intent intent1 = new Intent(this, RSS_resister.class);
                startActivity(intent1);

                //Toast.makeText(this, "準備中です...", Toast.LENGTH_SHORT).show();
                break;
*/
            case R.id.OP1:
                finish();   //  メインActivityへ戻る
                break;
            default:

        }
        return true;
    }   //  Option メニュー

    public void getXMLresult(ArrayList<CharSequence> rtn){
        //  AsyncTascのclsXMから呼ばれるメソッド

        try {

            mNLAdapter = new NewsLineAdapter(this, rtn);
            LV_W.setAdapter(mNLAdapter);
            mNLAdapter.notifyDataSetChanged();

        }catch (Exception e){
            Log.d("■", "" + e.getMessage());
        }


    }

    private void showNews2(String uri,  String newsName){
        //  ニュースを表示する
        actionBar.setSubtitle(newsName);

        clsGetNews thread2 = new clsGetNews(MainActivity.this);
        thread2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri, "item",
                Constants.getPrefrenceString(MainActivity.this, Constants.PERIOD_OF_NEWS, "2"));

        //  最後に見たニュースを記録
        Constants.setPrefrenceString(MainActivity.this, Constants.LAST_NEWS_URI, uri);
        Constants.setPrefrenceString(MainActivity.this, Constants.LAST_NEWS_NAME, newsName);

        //  ニュース優先にセット
        Constants.setPrefrenceBoolean(MainActivity.this, Constants.IS_NEWS_LAST_CONTENT_, true);

    }

    static void setNews2ListView(final Activity activity, final ListView mListView, final String uri){
        //  asynctaskより簡単？
        final ArrayList<CharSequence> rtn = new ArrayList<CharSequence>();
        final Context mCnt = activity.getApplicationContext();

        new Thread(){
            public void run(){
                //  RSSのparseを非同期で
                try {
                    rtn.addAll(clsGetNews.getValuesFromUri (activity , uri, "item", 2));
                } catch (Exception e) {
                    rtn.add("RSS取得に失敗しました。");
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //  RSS取得がpostしたらアダプターをセットして表示
                        NewsLineAdapter NLA = new NewsLineAdapter(mCnt, rtn);
                        mListView.setAdapter(NLA);
                    }
                });
            }
        }.start();
    }

    private void showWeatherReport(){
        actionBar.setSubtitle("天気予報 " +
                Constants.getPrefrenceString(this, Constants.MY_AREA_NAME, ""));

        clsGetAreaWeather thread1 = new clsGetAreaWeather(MainActivity.this);

        String url = "http://www.drk7.jp/weather/xml/" +
                Constants.getPrefrenceString(MainActivity.this, Constants.MY_AREA_CODE, "13") +
                ".xml";
        //  並列処理を指定（複数スレッドを走らせていないので特に意味はない？）
        thread1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

        //  天気予報優先にセット
        Constants.setPrefrenceBoolean(MainActivity.this, Constants.IS_NEWS_LAST_CONTENT_, false);

    }

    public void newsShareDialog(Context context, String memo){

        try {
            //  撮影サイズを設定
            LinearLayout alertLayout0 = new LinearLayout(context);
            alertLayout0.setOrientation(LinearLayout.VERTICAL);
            alertLayout0.setPadding(15, 15, 15, 15);
            alertLayout0.setBackgroundColor(Color.argb(32, 0, 0, 255)); //  第一パラメータを0にすると透明

            final TextView textView = new TextView(context);

            textView.setTextSize(Integer.parseInt
                    (Constants.getPrefrenceString(MainActivity.this, Constants.TEXT_SIZE, "14")));
            //  引用を明記する
            String memo1 = memo.replaceAll("詳細", "");
            textView.setText( ">> " + actionBar.getSubtitle()  + " から引用\n" + memo1 );
            alertLayout0.addView(textView);

            AlertDialog.Builder builder0 = new AlertDialog.Builder(context);
            builder0.setTitle("記事の共有");
            //  作成したレイアウトをセット
            builder0.setView(alertLayout0)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent1 = new Intent(Intent.ACTION_SEND);
                                    intent1.setType("text/plain");
                                    intent1.putExtra(Intent.EXTRA_TEXT, textView.getText().toString());
                                    startActivity(intent1);
                                }
                            })
                    .setNegativeButton("キャンセル",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    .show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }   //

    public void reloadLastContent(){

        if(Constants.getPrefrenceBoolean(this, Constants.IS_NEWS_LAST_CONTENT_, true)){
            //  ニュースが最後
            showNews2(
                    Constants.getPrefrenceString(MainActivity.this, Constants.LAST_NEWS_URI,
                            "http://www3.nhk.or.jp/rss/news/cat0.xml"),
                    Constants.getPrefrenceString(MainActivity.this, Constants.LAST_NEWS_NAME, "NHK 総合")
            );

        }
        else{
            //  天気予報が最後
            showWeatherReport();
        }

    }

}
