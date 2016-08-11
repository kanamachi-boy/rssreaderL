package com.example.banchan.rssreaderL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.Html;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

public class clsGetNews extends AsyncTask<String, Void, ArrayList<CharSequence>> {
    //  引数①：メインスレッド（Activity）から渡す変数型
    //  引数②：進捗表示onProgressUpdate()　の引数、不要ならVoid
    //  引数③：doInBackbround()の戻り値 = onPostExecute()の引数

    private MainActivity NewsActivity;
    private ProgressDialog progressDialog = null;
    private long mPeriod;

    // コンストラクター
    public clsGetNews(MainActivity activity) {
        //  onPostExecuteでメインスレッドにアクセスするために必要
        NewsActivity = activity;

    }

    @Override
    protected void onPreExecute(){
        progressDialog = new ProgressDialog(NewsActivity);
        progressDialog.setTitle("ニュースデータを取得しています。");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("");
        progressDialog.show();

    }

    // バックグラウンドで処理する（重い処理）
    @Override
    protected ArrayList<CharSequence> doInBackground(String... params) {
        //  メインスレッドでは　clsGetAreaFeather#excuteの引数となる。param数は任意。
        ArrayList<CharSequence> rtn= new ArrayList<CharSequence>();
        String uri = params[0];
        String key = params[1];
        //  何日前？⇒これより小さい（古い）記事は表示しない
        mPeriod = System.currentTimeMillis() - Long.parseLong(params[2]) * 24 * 60 * 60 * 1000;

        try {
            rtn = getValuesFromUri(NewsActivity, uri, key, mPeriod);

            if(rtn.size() == 0){
                rtn.add(Html.fromHtml(
                        "<font color=red>※ニュースが取得できません。<br>" +
                                "「設定」→「ニュースの表示期間」<br>を長くしてみて下さい。<br></font>"));
            }

        } catch (Exception e) {
            rtn.add(Html.fromHtml(
                    "<font color=red>※ニュースが取得できません。<br>" +
                            " ネット接続状況を確認して下さい。<br></font>"));
       }
        //  この返値がonPostExecuteの引数にセットされる
        return rtn;
    }

    // バックグラウンド処理が終了した後にメインスレッドに渡す処理
    @Override
    protected void onPostExecute(ArrayList<CharSequence> result) {
        //  doInBackgroundの完了後、結果を引数にセットして自動的に呼ばれる
        //  呼び出し元のメソッドを呼び出す。viewに値をセットしても良い。
        progressDialog.dismiss();
        NewsActivity.getXMLresult(result);
    }

    static ArrayList<CharSequence> getValuesFromUri(Activity aActivity, String uri,String key, long aPeriod)
            throws ParserConfigurationException, IOException, SAXException, ParseException {
        ArrayList<CharSequence> rtn = new ArrayList<CharSequence>();

        NodeList nodeList = SettingFGActivity.getWebDoc(aActivity, uri)
                .getElementsByTagName(key);
        if(nodeList == null){

            rtn.add(Html.fromHtml("<font color=red>" +
                    "ニュースが取得できません。<br>ネット接続状況を確認して下さい。</font>"));

            return rtn;

        }

        //  ①を順に調べる
        for (int i=0; i<nodeList.getLength(); i++) {
            //  ①の子ノード配列②を作る
            NodeList nodeList2 = nodeList.item(i).getChildNodes();
            String mDate0="", mDate="", mTitle="", mDescription="", mLink="";
            //  ②を順に調べる
            for (int j=0; j<nodeList2.getLength(); j++){
                //  ②の子ノード③を調べる
                if(nodeList2.item(j).hasChildNodes()){
                    String mNName = nodeList2.item(j).getNodeName();    //  ③の名前
                    //  ③の値（値はさらにその子）
                    String mNValue = nodeList2.item(j).getFirstChild().getNodeValue();
                    //  HTML表示するためにタグを追加
                    if (mNName.equals("title")){
                        mTitle = "<font color=blue><big>" + mNValue + "</big></font><br>";
                    }
                    else if (mNName.equals("description")) {

                        mDescription  = removeTag(mNValue);
                        //mDescription  = mNValue;
                     }
                    else if(mNName.equals("pubDate")) {
                        mDate0 = transferDateFormat     //  mPeriodより古いと * を返してくる
                                (mNValue,"EEE, d MMM yyyy HH:mm:ss Z","yyyy/MM/dd", aPeriod);
                        mDate = "  <small>" + mDate0 + "</small>";
                     }
                    else if(mNName.equals("link")){
                        mLink = "<br><a href=\"" + mNValue + "\"><I>詳細</I></a>";
                    }
                }
             }
            if(! mDate0.equals("*")) {  //  古いものを除いて返り値に追加
                rtn.add(Html.fromHtml(mTitle + mDescription + mLink + mDate));
            }
        }
        return rtn;
    }

    static String transferDateFormat (String orgDate, String orgFromat, String outFromat, long aPeriod){
        //  日付形式の変更
        //  ★文字列⇒parseしてdate⇒applyPattrenでdateパターンを変更⇒formatで変更後のパターンで文字列へ
        //
        //  date化したい文字列と同じパターンにしてparseする
        SimpleDateFormat sdf = new SimpleDateFormat(orgFromat, Locale.ENGLISH);
        try{
            Date date = sdf.parse(orgDate);
            if(date.getTime() > aPeriod){    //  long型で比較
                sdf.applyPattern(outFromat);
                return sdf.format(date);
            }
            else{   //  非表示判定用文字を返す
                return "*";
            }
        }
        catch (Exception e){
            return "---";
        }
    }

    static String removeTag(String orgStr){
        //  先にアンカータグを削除
        String str1 = orgStr.replaceFirst("<a.+?>記事を読む</a>", "");
        //  イメージタグを削除
        String str2 = str1.replaceFirst("<img[^>]+>","");
        //  <br />を削除 ：　複数
        String str3 = str2.replaceAll("<br />", "");
        //  末尾部分を削除
        String str4 = str3.replaceFirst("全文.+\\d{2}時\\d{2}分", "");
        //  livedoorの冒頭部分がうざいのでカット
        String str5 = str4.replaceFirst("ざっくり言うと", "");

        return str5;
    }

}