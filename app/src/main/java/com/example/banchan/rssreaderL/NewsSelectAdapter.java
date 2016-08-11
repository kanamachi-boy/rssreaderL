package com.example.banchan.rssreaderL;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Banchan on 2016/05/18.
 */
public class NewsSelectAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<String[]> newsList;

    public NewsSelectAdapter(Context context, ArrayList<String[]> newsLine) {
        this.context = context;
        this.newsList = newsLine;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        View view = convertView;

        if(view == null){   //  viewの再利用
            view = layoutInflater.inflate(R.layout.inbox_1row2, parent, false);
        }

        TextView tv  = (TextView) view.findViewById(R.id.textView1);
        //  フォントサイズをセット
        tv.setTextSize(Integer.parseInt(Constants.getPrefrenceString(context, Constants.TEXT_SIZE, "14")));
        //  テキストをセット
        tv.setText(newsList.get(position)[1]);
        //  <a>に反応するようセット
        MovementMethod movementmethod = LinkMovementMethod.getInstance();
        tv.setMovementMethod(movementmethod);
        //  ListViewにTextViewを配置するとListViewはOnItemClickを拾えないので
        //  変わりにTextViewからItemClickを発生させる・
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0L);
            }
        });

        return view;
    }
}
