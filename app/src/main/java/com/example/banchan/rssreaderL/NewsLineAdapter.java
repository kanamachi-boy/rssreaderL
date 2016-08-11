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
public class NewsLineAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<CharSequence> newsList;

    public NewsLineAdapter(Context context, ArrayList<CharSequence> newsLine) {
        this.context = context;
        this.newsList = newsLine;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder{
        //
        TextView tv;

    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;    //  viewholderを実装

        if (convertView == null ){
            //  viewholder は参照の
            convertView = layoutInflater.inflate(R.layout.inbox_1row, parent, false);
            holder = new ViewHolder();
            holder.tv = (TextView) convertView.findViewById(R.id.textView1);

            convertView.setTag(holder);
        }
        else{

            holder = (ViewHolder) convertView.getTag();
        }

        //  フォントサイズをセット
        holder.tv.setTextSize(Integer.parseInt(Constants.getPrefrenceString(context, Constants.TEXT_SIZE, "14")));
        //  テキストをセット
        holder.tv.setText(newsList.get(position));
        //  <a>に反応するようセット
        MovementMethod movementmethod = LinkMovementMethod.getInstance();
        holder.tv.setMovementMethod(movementmethod);
        //  ListViewにTextViewを配置するとListViewはOnItemClickを拾えないので
        //  変わりにTextViewからItemClickを発生させる・
        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0L);
            }
        });

        return convertView;
    }
}
