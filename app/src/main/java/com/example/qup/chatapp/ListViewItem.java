package com.example.qup.chatapp;

import android.graphics.drawable.Drawable;

/**
 * 주요기능
 * 1. 리스트의 데이터 목록
 *    - 프로필사진
 *    - 이름
 *    - 세부 정보 문자열
 *    - 시간 저장 문자열
 *    >>친구목록에서는 시간 저장 문자열 보이지 않도록 설정
 */
//친구목록이나 대화목록에서 쓰이는 각 한 줄의 리스트를 커스텀으로 생성하기 위한 클래스이다
//안드로이드 스튜디오가 제공하는 기본 리스트와 다르게 여러가지를 추가 가능하지만 프로그래밍이 복잡해진다
public class ListViewItem {
    private Drawable iconDrawable ;
    private String nameStr ;
    private String subStr ;
    private String timeStr;
    private String boardStr;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setName(String name) {
        nameStr = name;
    }
    public void setSub(String subInfo) {
        subStr = subInfo;
    }
    public void setTime(String time) {
        timeStr = time;
    }
    public void setBoard(String board) {boardStr = board;}

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getName() {
        return this.nameStr ;
    }
    public String getSub() {
        return this.subStr ;
    }
    public String getTime() {
        return this.timeStr ;
    }
    public String getBoardStr() {return this.boardStr;}
}