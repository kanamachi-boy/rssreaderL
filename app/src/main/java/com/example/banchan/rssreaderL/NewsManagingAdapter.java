package com.example.banchan.rssreaderL;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Banchan on 2016/05/18.
 */
public class NewsManagingAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<String[]> newsList;
    String LastAccessUri;

    public NewsManagingAdapter(Context context, ArrayList<String[]> newsLine) {
        this.context = context;
        this.newsList = newsLine;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.LastAccessUri = Constants.getPrefrenceString(context, Constants.LAST_NEWS_URI,
                "http://www3.nhk.or.jp/rss/news/cat0.xml");

    }

    static class ViewHolder{

        CheckBox cb;
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
    }

    public String getItemMember(int position, int num){
        return newsList.get(position)[num];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {


        convertView = layoutInflater.inflate(R.layout.inbox_1row_chk, parent, false);

        final CheckBox cb = (CheckBox) convertView.findViewById(R.id.chk_view);

        //  フォントサイズをセット
        cb.setTextSize(Integer.parseInt(Constants.getPrefrenceString(context, Constants.TEXT_SIZE, "14")));
        //  テキストをセット
        cb.setText(newsList.get(position)[1]);
        //  チェックをセット
        boolean mFlg = newsList.get(position)[2].equals("1") ? true : false;
        cb.setChecked(mFlg);

        if(newsList.get(position)[0].equals(LastAccessUri)) {

            //  最後に見たURIは非表示にできない⇒NPEの防止にもなる
            cb.setChecked(true);
            cb.setEnabled(false);
        }

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                int mArg = (cb.isChecked()) ? 1 : 0;

                newsList.get(position)[2] = String.valueOf(mArg);   //  スクロールで元に戻るバグを修正

                DatabaseHelper DBH = new DatabaseHelper(parent.getContext());
                int result = DBH.updateVisible(Integer.parseInt(newsList.get(position)[3]), mArg);
                DBH.close();

            }

        });

        return convertView;
    }
}
