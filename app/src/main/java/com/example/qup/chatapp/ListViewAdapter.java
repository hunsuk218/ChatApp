package com.example.qup.chatapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MrT on 2017-04-06.
 * 주요 기능
 * 1. 리스트 데이터를 다른 클래스에서 사용 가능하도록 ListViewItem 클래스와 사용하는 클래스를 이어주는 클래스
 * 2. 인터넷에서 긁어온거라 다른 부분 말고 addItem()메소드 만 확인하면 됨
 * 3. 나머지 메소드는 세부 주석 확인
 * 4. addItem 친구목록용(매개변수 3개), 대화목록용(매개변수 4개)으로 오버로드 구현
 */

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }   //배열 길이를 반환하는 메소드
    public String[] getAll() {  //리스트 중 이름에 관한 내용을 모은 배열을 반환하는 메소드
        String[] listName=new String[listViewItemList.size()];
        ListViewItem listViewItem;
        for(int i=0;i<listName.length;i++) {
            listViewItem = listViewItemList.get(i);
            listName[i] = listViewItem.getName();
        }
        return listName;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "list_view_item" Layout을 가져와서 convertView가 참조함
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_item, parent, false);
        }

        // 화면에 표시될 convertView(list_view_item.xml)로부터 위젯에 대한 참조
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.img_list_profile) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.txt_list_name) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.txt_list_sub_info) ;
        TextView timeTextView = (TextView) convertView.findViewById(R.id.txt_time);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        titleTextView.setText(listViewItem.getName());
        descTextView.setText(listViewItem.getSub());
        timeTextView.setText(listViewItem.getTime());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }   //item의 인덱스인 position을 반환하는 메소드

    //item의 인덱스인 position의 값인 listViewItem을 반환하는 메소드
    @Override
    public ListViewItem getItem(int position) {
        return listViewItemList.get(position) ;
    }

    public void addItem(Drawable icon, String title, String desc) { //친구목록의 ListViewItem 추가 메소드
        ListViewItem item = new ListViewItem();

        item.setIcon(icon);
        item.setName(title);
        item.setSub(desc);
        item.setTime("");   //시간 부분 설정 안함

        listViewItemList.add(item); //리스트로 보여주기 위해 ListViewItem을 한줄 추가
    }

    public void addItem(Drawable icon, String title, String desc, String time, String board) { //대화목록의 ListViewItem 추가 메소드
        ListViewItem item = new ListViewItem();

        item.setIcon(icon);
        item.setName(title);
        item.setSub(desc);
        item.setTime(time);
        item.setBoard(board);

        listViewItemList.add(item);
    }
}