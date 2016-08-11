package com.example.banchan.rssreaderL;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

public class clsGetAreaWeather extends AsyncTask<String, Void, ArrayList<CharSequence>> {
    //  引数①：メインスレッド（Activity）から渡す変数型
    //  引数②：進捗表示onProgressUpdate()　の引数、不要ならVoid
    //  引数③：doInBackbround()の戻り値 = onPostExecute()の引数

    private MainActivity NewsActivity;
    ProgressDialog progressDialog;

    // コンストラクター
    public clsGetAreaWeather(MainActivity activity) {
        //  onPostExecuteでメインスレッドにアクセスするために必要
        NewsActivity = activity;
    }

    @Override
    protected void onPreExecute(){
        progressDialog = new ProgressDialog(NewsActivity);
        progressDialog.setTitle("天気予報を取得しています。");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

    }

    // バックグラウンドで処理する
    @Override
    protected ArrayList<CharSequence> doInBackground(String... params) {
        //  メインスレッドでは　clsGetAreaWeather#excuteの引数となる。param数は任意。
        ArrayList<CharSequence> rtn= new ArrayList<CharSequence>();
        String uri = params[0];

        try {
            rtn = getValuesFromUri(uri);
        } catch (Exception e) {
            rtn.add(e.getMessage());
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

    public ArrayList<CharSequence> getValuesFromUri(String uri)
            throws ParserConfigurationException, IOException, SAXException, ParseException {
        ArrayList<CharSequence> rtn = new ArrayList<CharSequence>();

        Document doc = SettingFGActivity.getWebDoc(NewsActivity, uri);
        //  発行日を取得
        NodeList nL0 = doc.getElementsByTagName("pubDate");
        SimpleDateFormat sdf0 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        Date date0 = sdf0.parse(nL0.item(0).getTextContent());
        sdf0.applyPattern("M/d HH:mm");
        rtn.add(Html.fromHtml("<small>" + sdf0.format(date0) + " 発行</small>"));

        //  地域別の予報を取得
        NodeList nL1 = doc.getElementsByTagName("area");

        for (int i=0; i<nL1.getLength(); i++) {
            //   area = 地方名
            NamedNodeMap attrList =  nL1.item(i).getAttributes();
            String mRTN = "<h2>" + attrList.item(0).getNodeValue() +"</h2>";  //  地方名

            NodeList nL2 = nL1.item(i).getChildNodes();
            for (int j=0; j<nL2.getLength(); j++){

                if(nL2.item(j).getNodeName().equals("info")){
                    //  info = 日付
                    NamedNodeMap attrList2 = nL2.item(j).getAttributes();
                        //  簡易書式に変換・color tagを付加
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN );
                    Date date = sdf.parse(attrList2.item(0).getNodeValue());
                    sdf.applyPattern("MM/dd E");
                    String colDate = replaceColorStr(sdf.format(date), "日", "red");
                    String colDate2 = replaceColorStr(colDate, "土", "blue");
                    mRTN += "<big>[ " + colDate2 + " ] </big>" ;

                    NodeList nl3 = nL2.item(j).getChildNodes();
                    for ( int k=0; k<nl3.getLength(); k++){

                        if(nl3.item(k).getNodeName().equals("weather")){
                            //  天気コードに変換
                            String bbb0 = nl3.item(k).getFirstChild().getNodeValue();
                            String bbb = replaceWeather2Code(bbb0);
                            if(bbb == null){    //  変換失敗したら文字のまま表示
                                mRTN +=  bbb0 + "  " ;
                            }
                            else{   //  imageGetterで画像を埋め込む
                                mRTN += "<img src='" + bbb + "'> ";
                            }
                        }
                        else if(nl3.item(k).getNodeName().equals("temperature")) {
                            //  気温
                            NodeList nl4 = nl3.item(k).getChildNodes();

                            for (int m = 0; m < nl4.getLength(); m++) {
                                //NamedNodeMap attrList4 = nl4.item(m).getAttributes();
                                if(nl4.item(m).getNodeName().equals("range")){
                                    // 最高気温・最低気温
                                    mRTN += nl4.item(m).getTextContent() + "°  ";
                                }
                            }
                            mRTN +="<br>  ";
                        }
                        else if(nl3.item(k).getNodeName().equals("rainfallchance")) {
                            //  降水確率
                            NodeList nl5 = nl3.item(k).getChildNodes();
                            mRTN +="<tt>";
                            int pCnt=0;
                            for (int n = 0; n < nl5.getLength(); n++) {
                                //NamedNodeMap attrList5 = nl5.item(n).getAttributes();

                                if(nl5.item(n).getNodeName().equals("period")){
                                    //
                                    Integer mRoR = Integer.parseInt(nl5.item(n).getTextContent());
                                    String mColor ="";
                                    if(mRoR <= 20){
                                        mColor ="red";
                                    }
                                    else if(mRoR >20 && mRoR<=40){
                                        mColor ="gray";
                                    }
                                    else{
                                        mColor ="blue";
                                    }
                                    String ccc = String.format(" %02d",mRoR);
                                    String ccc1 = "";
                                    if(ccc.equals(" 00")){
                                        //  桁合わせのため色を変える
                                        ccc1 = "<font color=#ffffff>0</font>" +
                                                "<font color=red>0</font>";
                                    }
                                    else{
                                        ccc1 = "<font color=" + mColor +">" + ccc  +"</font>";
                                    }

                                    mRTN += ccc1;
                                    mRTN += "<sup><small> " + String.format ("%02d",(pCnt +1) *6)  + " </small></sup>";
                                    pCnt++;
                                }

                            }
                            mRTN +="</tt><br><br>";
                        }
                    }
                }
            }
            MyImageGetter myIG = new MyImageGetter(NewsActivity);
            rtn.add(Html.fromHtml(mRTN, myIG, null));

        }
        return rtn;
    }

    public String replaceColorStr(String orgStr, String pattern, String color){
        //  patternの部分のcolorを変える
        Pattern p1 = Pattern.compile("(.*)(" + pattern + ")(.*)");
        Matcher m2 = p1.matcher(orgStr);
        return m2.replaceFirst("$1<font color=" + color + ">$2</font>$3");
    }

    public String replaceWeather2Code(String orgStr){
        //  天気予報文言をコードに変換
            //  基本語句をコードに変換
        String[] mWord = {"晴れ","くもり","雨","雪"};
        String[] convCode = {"1","2","3","4"};
        String[] convStr = new String[mWord.length + 1];
        convStr[0] = orgStr;
        for(int i=0; i <mWord.length; i++ ){
            convStr[i + 1] = convStr[i].replaceFirst(mWord[i], convCode[i]);
        }
            //  コード以外の語句（のち、時々など）を削除
        String rtn = convStr[mWord.length].replaceAll("[^\\d]", "");

            //  数字を昇順に変える
        if(rtn.length() == 0){
            return null;
        }
        else if(rtn.length() == 1){
            return "f" + rtn;
        }
        else{
            int[] aaa =
                    {Integer.parseInt(rtn.substring(0, 1)), Integer.parseInt(rtn.substring(1, 2))};
            return
                    "f" + String.format("%d", Math.min(aaa[0], aaa[1]) * 10 + Math.max(aaa[0], aaa[1]));
        }
    }

    public class MyImageGetter implements Html.ImageGetter{

        Context mContext;

        MyImageGetter(Context context){
            mContext = context;
        }

        public Drawable getDrawable(String source) {

            //  ①
            // 画像のリソースIDを取得         R.drawable.'source' の場合。
            int id1 = mContext.getResources().getIdentifier(source, "drawable", mContext.getPackageName());
            // リソースIDから Drawable のインスタンスを取得
            Drawable d1 = mContext.getResources().getDrawable(id1);

            float mRatio =
                    Float.parseFloat(Constants.getPrefrenceString(mContext, Constants.TEXT_SIZE, "14"))
                    / 12F;

            int mW = (int)(d1.getIntrinsicWidth() * mRatio);
            int mH = (int)(d1.getIntrinsicHeight() * mRatio);

            d1.setBounds(0, 0, mW, mH);

            return d1;
        }

    }

}

