package com.example.banchan.rssreaderL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class SettingFGActivity extends Activity {

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        makePrefFragment();

        getActionBar().setDisplayHomeAsUpEnabled(true);

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

    public void makePrefFragment(){
        //  リソースからデータを取得
        TypedArray areaCode = getResources().obtainTypedArray(R.array.weathr_area);
        ArrayList<String> aaa = new ArrayList<String>();
        for(int i=0; i<areaCode.length(); i++){
            aaa.add(areaCode.getString(i));
        }

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("code_set", aaa);   //  bundleでなくてもfragmentから参照できる？
        bundle.putString("last_code", Constants.getPrefrenceString(this, Constants.MY_AREA_CODE, "130010"));

        PrefFragment pF = new PrefFragment();
        pF.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, pF)
                .commit();

    }

    public static class PrefFragment extends PreferenceFragment{

        @Override
        public void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //  xmlを読み込む
            addPreferencesFromResource(R.xml.pref_settings);


            //  各項目の設定値をSummaryに表示する
            ListPreference lp2 =(ListPreference) findPreference(Constants.PERIOD_OF_NEWS);
            lp2.setSummary
                    (Constants.getPrefrenceString(getActivity().getApplicationContext(),
                            Constants.PERIOD_OF_NEWS, "2"));

            ListPreference lp3 =(ListPreference) findPreference(Constants.TEXT_SIZE);
            lp3.setSummary
                    (Constants.getPrefrenceString(getActivity().getApplicationContext(),
                            Constants.TEXT_SIZE, "14"));

            ListPreference lp7 =(ListPreference) findPreference(Constants.TIMEOUT_FOR_PARSE);
            lp7.setSummary
                    (Constants.getPrefrenceString(getActivity().getApplicationContext(),
                            Constants.TIMEOUT_FOR_PARSE, "30"));

                //  リストを取得、bundleに格納されている地域コードを設定リストにセットする
            ListPreference lp =(ListPreference) findPreference(Constants.MY_AREA_CODE);

            final Bundle bundle = getArguments();
            ArrayList<String> aaa = new ArrayList<String>();
            aaa.addAll(bundle.getStringArrayList("code_set"));

            String[] mEntries = new String[aaa.size()];
            String[] mEVlues = new String[aaa.size()];
            for (int i = 0; i < aaa.size(); i++){
                String[] bbb = aaa.get(i).toString().split(";",0);
                mEntries[i] = bbb[1];
                mEVlues[i] = bbb[0];
            }
            lp.setEntries(mEntries);
            lp.setEntryValues(mEVlues);
            lp.setSummary
                    (Constants.getPrefrenceString(getActivity().getApplicationContext(),
                            Constants.MY_AREA_NAME, "東京"));

        }

        @Override
        public void onStart(){
            super.onStart();
            //   設定変更を監視するリスナーを設定
            //  unreg...はonPause（画面を離れた時）されるのに対し
            //  onCreateは、終了せずに戻った時呼ばれないので機能しなくなる。で、こちらへ移動。
            PreferenceScreen root = getPreferenceScreen();
            root.getSharedPreferences().registerOnSharedPreferenceChangeListener(onPreferenceChangeListenter);

        }

        @Override
        public void onPause(){
            super.onPause();
            getPreferenceScreen().getSharedPreferences().
                    unregisterOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
            //  設定表示値を渡す

        }


        private SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListenter
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //  変更された時リストのsummaryに表示値を示す（保存値ではない）
                final Preference preference = getPreferenceScreen().findPreference(key);
                if(preference instanceof ListPreference){
                    final ListPreference listPreference = (ListPreference)preference;
                    //Log.d("■", "" +  listPreference.getValue() + " : " + listPreference.getEntry());
                    listPreference.setSummary(listPreference.getEntry());
                    //  Mainで表示するために表示値を保存しておく
                    if(key.equals(Constants.MY_AREA_CODE)) {
                        Constants.setPrefrenceString
                                (getActivity().getApplicationContext(), Constants.MY_AREA_NAME,
                                        (String) listPreference.getEntry());
                    }
                }
            }
        };

    }

    public static Document getWebDoc(Context context, String uri){
        //  他のクラスからも参照できるようにする
        Document doc;
        try {

            // urlからXMLドキュメントを保持しておく
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            //  timeout設定してドキュメントをパースする
            URL url = new URL(uri);
            URLConnection con = url.openConnection();
            Integer mTimeOut = Integer.parseInt
                    (Constants.getPrefrenceString(context , Constants.TIMEOUT_FOR_PARSE, "30"))
                    * 1000; //  milli秒なので
            con.setConnectTimeout(mTimeOut);
            doc = db.parse(con.getInputStream());
        }catch(Exception e){
            return null;

        }
        return doc;

    }

}
