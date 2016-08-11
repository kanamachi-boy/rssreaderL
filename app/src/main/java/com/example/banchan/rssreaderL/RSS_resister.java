package com.example.banchan.rssreaderL;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

public class RSS_resister extends ActionBarActivity {

    private String mUri4Resist = "";    //  検査後に変更される対策

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_resister);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView mLV = (ListView) findViewById(R.id.lv_reg);
        final EditText edt = (EditText)findViewById(R.id.rss_uri_edit);
        final Button btn1 = (Button) findViewById(R.id.rss_reg_btn);

        //  Thread終了時のHandler#postで ItemClickイベントを発生させ、
        //  リスト内の値（1行目）を調べて「登録ボタン」を有効にする
        mLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (parent.getAdapter().getItem(0).toString().equals("OK")) {

                    btn1.setEnabled(true);
                }
            }
        });

        //edt.setText("http://www3.nhk.or.jp/rss/news/cat5.xml");

        Button btn = (Button) findViewById(R.id.rss_test_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  テストしたURIが変更されないよう保護
                mUri4Resist = edt.getText().toString();

                setNewsTest2ListView(RSS_resister.this, mLV, mUri4Resist);

            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  登録

                edt.setText(mUri4Resist);   //
                btn1.setEnabled(false);     //  その都度テストしないと登録できないようにする

                if (mLV.getAdapter() == null) {

                    Toast.makeText(RSS_resister.this, "登録の前にテストしてください。", Toast.LENGTH_SHORT).show();

                } else if (!mLV.getAdapter().getItem(0).toString().equals("OK")) {

                    Toast.makeText(RSS_resister.this, "登録できません。\nRSSが有効ではありません。",
                            Toast.LENGTH_SHORT).show();

                } else {

                    DatabaseHelper DBH = new DatabaseHelper(RSS_resister.this);

                    long mFlg = DBH.insert(mUri4Resist, mLV.getAdapter().getItem(1).toString());

                    if (mFlg > 0) {
                        Toast.makeText(RSS_resister.this, "登録しました。", Toast.LENGTH_SHORT).show();
                    } else if (mFlg == -1000) {
                        Toast.makeText(RSS_resister.this, "登録できません。\n必要なデータが欠落しています"
                                , Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RSS_resister.this, "登録できません。\nこのアドレスは登録済みです。"
                                , Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setNewsTest2ListView
            (final Activity activity, final ListView mListView, final String uri){
        //  Thread と Handlerで非同期処理。　asynctaskより簡単？
        final ArrayList<CharSequence> rtn = new ArrayList<CharSequence>();
        final Context mCnt = activity.getApplicationContext();

        new Thread(){
            public void run(){
                //  RSSのparseを非同期で
                try {
                    rtn.addAll(checkValuesOfRSS (activity , uri));
                } catch (Exception e) {

                    rtn.add("RSS取得に失敗しました。");
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //  RSS取得がpostしたらアダプターをセットして表示
                        NewsLineAdapter NLA = new NewsLineAdapter(mCnt, rtn);
                        mListView.setAdapter(NLA);
                        mListView.performItemClick(mListView, 0, 0);

                    }
                });
            }
        }.start();
    }

    static ArrayList<String> checkValuesOfRSS(Activity aActivity, String uri)
            throws ParserConfigurationException, IOException, SAXException, ParseException {

        ArrayList<String> rtn = new ArrayList<String>();
        //  channel : ルート直下のノード
        NodeList nodeList = SettingFGActivity.getWebDoc(aActivity, uri)
                .getElementsByTagName("channel");
        if(nodeList == null){

            rtn.add("ニュースが取得できません。ネット接続状況を確認して下さい。");
            return rtn;

        }

        SimpleDateFormat sdf0 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        Date date0;
        long LatestTime = 0;
        String _Name ="";
        boolean hasName = false;
        boolean hasDate = false;
        boolean hasTitle = false;
        String _Title = "";
        boolean hasDescription = false;
        String _Description = "";

        //  channel の子ノードを調べる
        NodeList nodeList1 = nodeList.item(0).getChildNodes();

        for (int i=0; i<nodeList1.getLength(); i++) {
            //  直下のタイトル
            if(nodeList1.item(i).getNodeName().equals("title")){
                if(nodeList1.item(i).getTextContent().length() > 0){    //  空白名はNG
                    _Name = nodeList1.item(i).getTextContent();
                    hasName = true;
                }

            }
            else if(nodeList1.item(i).getNodeName().equals("lastBuildDate")){

                date0 = sdf0.parse(nodeList1.item(i).getTextContent());

                LatestTime = LatestTime < date0.getTime() ? date0.getTime() : LatestTime;

            }
            else if(nodeList1.item(i).getNodeName().equals("item")){
                //  各ニュースitemを調べる
                NodeList nodeList2 = nodeList1.item(i).getChildNodes();
                for (int j=0; j<nodeList2.getLength(); j++){
                    if(nodeList2.item(j).getNodeName().equals("pubDate")){
                        hasDate = true;
                        date0 = sdf0.parse(nodeList2.item(j).getTextContent());
                        LatestTime = LatestTime < date0.getTime() ? date0.getTime() : LatestTime;

                    }
                    else if(nodeList2.item(j).getNodeName().equals("title")){
                        hasTitle = true;
                        _Title = nodeList2.item(j).getTextContent();
                    }
                    else if(nodeList2.item(j).getNodeName().equals("description")){

                        hasDescription = true;
                        _Description = nodeList2.item(j).getTextContent().substring(0,36) + " ...";
                    }
                }
            }

        }
        //rtn.add(String.format("%d", LatestTime));

        rtn.add(hasName && hasDate && hasTitle && hasDescription ? "OK" : "No" );
        rtn.add(_Name);

        sdf0.applyPattern("yyyy/MM/dd HH:mm:ss");
        rtn.add( sdf0.format(LatestTime));
        rtn.add( _Title);
        rtn.add( _Description);

        return rtn;
    }

}
