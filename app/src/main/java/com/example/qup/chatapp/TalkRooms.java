package com.example.qup.chatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * 주요 기능
 * 1. 두번째 페이지
 * 2. tab 기능으로 친구목록, 대화목록 tab 생성
 * 3. 리스트 관련 클래스 ListViewItem, ListViewAdapter에서 리스트 불러와 사용
 * 4. 친구목록 리스트 클릭시 대화상자로 그 친구와 1:1 대화방 생성 여부 묻기
 * 5. 친구추가 버튼 클릭시 대화상자로 이름 검색으로 친구 찾기
 * 6. 지도검색 버튼 클릭시 액티비티 이동
 * 7. 실행 시 현재 내 대화방 채팅내용을 받는 서비스 실행
 * 8. 로그아웃 버튼으로 로그인 페이지로 돌아가기(서비스 종료)
 * 주석완료
 */
public class TalkRooms extends TabActivity {
    ListView myFrList;  //친구목록 탭의 내 친구목록 리스트
    ListView myBoardList;   //채팅목록 탭의 내 보드 리스트

    Button btnFr; //친구추가 기능
    Button btnMap;//지도 보기 기능
    Button btnRe; //로그아웃 기능

    View frAddDlg;  //친구추가 클릭시 대화상자

    AutoCompleteTextView editFindNameCheck;  //찾을 친구 문자열 입력 부분

    //내 정보 관련 변수들
    String myId;
    TextView txtMyName;
    TextView txtMySub;
    ImageView imgMyProfile;
    Bitmap scaled;

    //MainActivity 참조(동일 방식으로 DB 연결하여 실행)
    String myJSON;
    String JSON_board;
    String JSON_boardid;

    JSONArray jarray = null;
    JSONArray jarray_board = null;
    JSONArray jary = null;

    ListViewAdapter adapter;    //친구목록의 리스트를 만드는 변수
    ListViewAdapter board_adapter;  //대화목록의 리스트를 만드는 변수

    Intent intent;
    ArrayList<String> boardidlist;  //각 대화방의 고유 id를 저장하는 ArrayList

    LinearLayout profileLayout; // 내정보 부분
    View myprofile; // ==== 대화상자 생성 관련 변수
    ImageView myPhoto;         // 프로필 창의 이미지
    Button imageEditBtn;
    EditText myName, myThink; // 프로필 창의 이름, 상태
    String JSON_myinfo;
    JSONArray jarray_myinfo = null;
    String img_uri;

    private int PICK_IMAGE_REQUEST = 1; //이미지 요청을 구별하기 위한 값(여러 사진을 선택하는 기능이 있다면 더 다양하게 필요하지만 하나의 이미지를 가져오기 때문에 하나 사용)

    private static final String LOG_TAG = "TalkRooms";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_room);

        boardidlist = new ArrayList<String>();  //대화목록 방들의 아이디를 저장할 배열 초기화

        //다른 액티비티에서 넘어올때의 키를 통하여 값을 가져온다
        intent = new Intent(this.getIntent());
        myId = intent.getStringExtra("myid");

        TabHost tabHost = getTabHost(); //tab기능 생성을 위한 코드

        //친구목록에 내 정보 관련 위젯들과 연결
        imgMyProfile = (ImageView) findViewById(R.id.img_my_profile);
        txtMyName = (TextView) findViewById(R.id.txt_my_name);
        txtMySub = (TextView) findViewById(R.id.txt_my_sub);

        txtMyName.setText(myId);

        //내 아이디를 통하여 DB에서 내정보관련 데이터들을 가져온다
        getMyFriend(myId);  //내 친구목록
        getMyboard(myId);   //내 대화목록
        getMyinfo(myId);    //내 정보

        profileLayout = (LinearLayout) findViewById(R.id.profileLayout); // 내정보 부분 레이아웃 참조

        /*내 정보 부분을 클릭 시
        * myprofile.xml의 내용과 합쳐진 대화상자를 생성하여 내 정보를 변경할 수 있도록 한다
        * 대화상자 내용을 채우고 확인 클릭 시 DB 연결하여 업데이트*/
        profileLayout.setOnClickListener(new View.OnClickListener() { // ==== 레이아웃 클릭 시 프로필 대화상자 생성 4.28 치훈 추가
            public void onClick(View v) {
                myprofile = View.inflate(TalkRooms.this, R.layout.myprofile, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(TalkRooms.this);
                dlg.setTitle("내 프로필");
                dlg.setView(myprofile);
                //대화상자 내의 각 위젯 참조
                myPhoto = (ImageView) myprofile.findViewById(R.id.myPhoto);
                imageEditBtn = (Button) myprofile.findViewById(R.id.image_edit_btn);
                myName = (EditText) myprofile.findViewById(R.id.myName);
                myThink = (EditText) myprofile.findViewById(R.id.myThink);

                //이미지 수정 버튼 클릭시 이미지 수정
                imageEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadImagefromGallery(); //하단에 구현
                    }
                });
                //수정하기 버튼(확인) 클릭 시
                dlg.setPositiveButton("수정하기", new DialogInterface.OnClickListener() { // ==== 수정하기 클릭시 내 정보 변경 4.28 치훈 추가(DB 연동 부탁)
                    public void onClick(DialogInterface dialog, int which) {
                        //친구목록의 내 정보 수정
                        txtMyName.setText(myName.getText().toString());
                        txtMySub.setText(myThink.getText().toString());
                        imgMyProfile.setImageBitmap(scaled);

                        insertToDatabase(myName.getText().toString(),img_uri);//DB에 수정한 내용 업데이트
                    }
                });
                dlg.setNegativeButton("닫기", null);
                dlg.show();
            }
        }); // 대화상자 출력 부분 4.28 치훈 추가

        //친구목록의 리스트 추가를 위한 부분
        myFrList = (ListView) findViewById(R.id.list_views); //talk_room.xml에 리스트뷰 id 가져옴(페이지에 리스트 띄우기 위해서)
        myBoardList = (ListView) findViewById(R.id.list_room_views);
        //하단의 버튼 위젯과 연결
        btnFr = (Button) findViewById(R.id.btn_friends_add); //친구추가 버튼 연결
        btnMap = (Button) findViewById(R.id.btn_map); //지도 화면 연결추가 버튼 연결
        btnRe = (Button) findViewById(R.id.btn_return); //로그아웃 버튼 연결

        //talk_room.xml에 레이아웃 두개의 id를 받아서 탭 2개 생성
        TabHost.TabSpec tabFriends = tabHost.newTabSpec("TAG1").setIndicator("친구목록");
        tabFriends.setContent(R.id.friends_tab);

        TabHost.TabSpec tabTalkRoom = tabHost.newTabSpec("TAG2").setIndicator("대화목록");
        tabTalkRoom.setContent(R.id.talkroom_tab);

        tabHost.addTab(tabFriends);
        tabHost.addTab(tabTalkRoom);

        //친구목록의 리스트를 클릭 시
        myFrList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ListViewItem frList = adapter.getItem(position);    //친구목록의 각 정보 저장
                /*대화상자를 생성하여 대화방 생성 여부 묻기
                * 확인 시 채팅방인 ChatActivity 클래스로 이동*/
                AlertDialog.Builder dlg = new AlertDialog.Builder(TalkRooms.this);
                dlg.setMessage(frList.getName() + "과(와) 대화하시겠습니까?");  //ListViewItem의 getName으로 이름 가져옴
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String myboardid = null;
                        //현재 친구목록중 클릭한 한 친구 정보에 대하여 실행하는 for문
                        for (int i = 0; i < board_adapter.getCount(); i++) {
                            String Name = board_adapter.getItem(i).getName();
                            if (frList.getName().equals(Name)) {
                                myboardid = board_adapter.getItem(i).getBoardStr(); //(나:친구)의 대화방의 아이디를 저장
                            }
                        }
                        if (myboardid == null) {    //새로 생성하는 대화방 일 때 getBoardid를 통하여 boardid 생성 후 ChatActivity로 이동
                            getBoardid(frList.getName());
                        } else {    //기존에 있는 대화방일때 boardid를 통하여 그에 맞는 기존 방의 ChatActivity로 이동
                            goChatActivity(myboardid);
                        }
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });
        //대화목록의 리스트 클릭 시
        myBoardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*대화상자를 생성하여 대화방 입장 여부 묻기(이미 있는 대화방이기 때문에 생성 여부는 묻지 않는다)
                * 확인 시 채팅방인 ChatActivity 클래스로 이동*/
                final ListViewItem frBoardList = board_adapter.getItem(position); //대화목록의 각 정보 저장

                AlertDialog.Builder dlg = new AlertDialog.Builder(TalkRooms.this);
                dlg.setMessage(frBoardList.getName() + "에 입장하시겠습니까?");
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String myboardid = null;
                        //현재 대화목록중 클릭한 한 대화방 정보에 대하여 실행하는 for문
                        for (int i = 0; i < board_adapter.getCount(); i++) {
                            String Name = board_adapter.getItem(i).getName();
                            if (frBoardList.getName().equals(Name)) {
                                myboardid = board_adapter.getItem(i).getBoardStr();
                            }
                        }
                        if (myboardid == null) {    //대화방 id 없을 시 생성 후 ChatActivity 이동
                            getBoardid(frBoardList.getName());
                        } else {    //대화방 id와 같은 채팅방의 ChatActivity 이동
                            goChatActivity(myboardid);
                        }//myboardid로 접속
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });

        //친구추가 버튼 클릭 시
        btnFr.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*내 정보 부분을 클릭 시
                * fr_add_dlg.xml의 내용과 합쳐진 대화상자를 생성하여 친구를 검색하여 추가하는 기능
                * DB 내에 검색한 친구가 있는지 확인하고 있을 시 내 친구목록에 추가*/
                frAddDlg = View.inflate(TalkRooms.this, R.layout.fr_add_dlg, null);
                android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(TalkRooms.this);
                dlg.setTitle("추가할 친구 검색");
                dlg.setView(frAddDlg);
                editFindNameCheck = (AutoCompleteTextView) frAddDlg.findViewById(R.id.edit_find_name_check);

                editFindNameCheck.setAdapter(new ArrayAdapter<String>(TalkRooms.this, android.R.layout.simple_dropdown_item_1line, adapter.getAll()));   //자동 완성 기능
                dlg.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String myFriend = editFindNameCheck.getText().toString();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (myFriend.equals(adapter.getItem(i).getName())) {    //입력한 id와 내 친구목록의 id가 같으면 실행
                                Toast.makeText(TalkRooms.this, "이미 친추된 사이입니다.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        addFriend(myFriend);    //DB 조회 후 친구 추가 부분
                        //현재 액티비티 재실행 >> 추가된 친구를 다시 화면에 보게 하려면 Talkroom을 새로고침해야 한다
                        Intent intent = new Intent(getApplicationContext(), TalkRooms.class);
                        intent.putExtra("myid",myId);
                        startActivity(intent);
                        finish();
                    }
                });
                dlg.setNegativeButton("취소", null);   //취소버튼은 그냥 취소 기능
                dlg.show();
            }
        });
        //지도 보기 버튼 클릭 시
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MapActivity로 이동, 돌아올 때 내 로그인 정보를 그대로 유지하기 위해 myid를 키로 myId의 값과 함께 이동
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra("myid", myId);
                startActivity(intent);
            }
        });

        btnRe.setOnClickListener(new View.OnClickListener() {
            /*로그아웃 시 액티비티 종료
            * 필요기능
            * - 액티비티 종료시 로그인 정보 없애기 */
            @Override
            public void onClick(View v) {
                //실행한 서비스 종료
                Intent intent = new Intent(getApplicationContext(), ChatService.class);
                stopService(intent);
                //로그인 화면으로 돌아감
                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent2);
                finish();
            }
        });
    }
    public void loadImagefromGallery() { //회원가입의 사진추가 버튼 클릭 후 이벤트들
        Intent intent = new Intent(Intent.ACTION_PICK); // 어떤 앱을 선택하여 진행할지 띄우는 Intent
        intent.setType("image/*"); // 이미지 관련 앱을 보여주고 선택하여 그 앱으로 이동

        startActivityForResult(intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST); //Intent 시작 : 갤러리 열고 원하는 이미지 선택할 수 있다
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //이미지 선택 작업 후의 처리
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) { //이미지를 하나 골랐을 때
                Uri uri = data.getData(); //클릭한 이미지의 절대 경로 uri에 저장

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri); // ConentResolver() : 어플 사이의 data 공유를 도와주는 메소드, 비트맵으로 그 경로의 데이터를 이미지화 한다

                //비트맵 이미지의 크기 조정
                int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
                scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);

                myPhoto.setImageBitmap(scaled); // 크기 조정한 사진을 내 정보 이미지에 적용

            }else {
                Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "로딩에 오류가 있습니다.", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "TalkRooms클래스 onActivityResult()에러 : " + e.toString());
        }
    }
    @Override
    public void onBackPressed() {   //뒤로가기 클릭시 액티비티를 종료하지 않고 백그라운드에서 실행되도록 변경
        moveTaskToBack(true);
        finish();
    }
    //친구목록 불러오기
    public void showList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            jarray = jsonObj.getJSONArray("result");
            adapter = new ListViewAdapter();    //2017.4.13 엄진환 >> ListViewAdapter 클래스 객체화하여 adapter에 저장
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject c = jarray.getJSONObject(i);
                String friendId = c.getString("friendId");

                adapter.addItem(ContextCompat.getDrawable(TalkRooms.this, R.drawable.png), friendId, "상태메시지");
                myFrList.setAdapter(adapter);   //리스트에 각 리스트의 값이 담긴 adapter 추가
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //친구 목록 출력
    public void getMyFriend(String id) {
        class phpdo extends AsyncTask<String, Void, String> {   //DB연동하여 나의 친구목록을 불러온다(MainActivity phpdo 참고)
            ProgressDialog loading;

            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TalkRooms.this, "Please Wait", null, true, true);
                loading.show();
            }

            protected void onPostExecute(String result) {
                loading.dismiss();
                myJSON = result;
                showList();
            }

            protected String doInBackground(String... params) {
                try {
                    String uri = "http://hunsuk218.iptime.org:80/getfriend.php";
                    String id = (String) params[0];

                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

                    BufferedReader bufferedReader = null;
                    URL url = new URL(uri);

                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                        break;
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    Log.d(LOG_TAG, "phpdo클래스 doInBackground()에러 : " + e.toString());
                    return "Exception: " + e.getMessage();
                }
            }
        }
        phpdo g = new phpdo();
        g.execute(id);
    }
    //대화목록 불러오기
    public void showBoardList() {
        try {
            JSONObject jsonObj = new JSONObject(JSON_board);
            jarray_board = jsonObj.getJSONArray("result");
            board_adapter = new ListViewAdapter();
            for (int i = 0; i < jarray_board.length(); i++) {
                JSONObject c = jarray_board.getJSONObject(i);
                String userid = c.getString("userid");
                String boardid = c.getString("boardid");
                String content = c.getString("content");
                String time = c.getString("time");
                String member = c.getString("member");

                board_adapter.addItem(ContextCompat.getDrawable(this, R.drawable.png), member, content, time, boardid);
                myBoardList.setAdapter(board_adapter);

                boardidlist.add(boardid);
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, "showBoardList()에러 : " + e.toString());
        }
        for (int i = 0; i < boardidlist.size(); i++) {
            Log.d("check", "보드아이디" + i + " : " + boardidlist.get(i));
        }
        //서비스 시작하기
        intent = new Intent(this, ChatService.class);
        intent.putExtra("myid", myId);
        intent.putExtra("listBoardid", boardidlist);
        startService(intent);
    }
    //대화목록 출력
    public void getMyboard(String id) {
        class phpdo2 extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TalkRooms.this, "Please Wait", null, true, true);
                loading.show();
            }

            protected void onPostExecute(String result) {
                loading.dismiss();
                JSON_board = result;
                showBoardList();
            }

            protected String doInBackground(String... params) {
                try {
                    String uri = "http://hunsuk218.iptime.org:80/getMyBoard.php";
                    String id = (String) params[0];

                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

                    BufferedReader bufferedReader = null;
                    URL url = new URL(uri);

                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                        break;
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    Log.d(LOG_TAG, "phpdo2클래스 doInBackground()에러 : " + e.toString());
                    return "Exception: " + e.getMessage();
                }
            }
        }
        phpdo2 g = new phpdo2();
        g.execute(id);
    }
    //boardid 생성하여 액티비티 이동 부분 구현
    public void take() {
        try {
            String myboardid = null;
            JSONObject jsonObj = new JSONObject(JSON_boardid);
            jary = jsonObj.getJSONArray("result");
            for (int i = 0; i < jary.length(); i++) {
                JSONObject c = jary.getJSONObject(i);
                myboardid = c.getString("boardid");
                Log.d("logcat", myboardid);
            }
            goChatActivity(myboardid);
        } catch (JSONException e) {
            Log.d(LOG_TAG, "take()에러 : " + e.toString());
        }
    }
    //친구의 이름을 통해서 그 친구와의 boardid를 받아와서 ChatActivity로 이동
    public void getBoardid(String member) {
        class phpdo3 extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TalkRooms.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String result) {
                loading.dismiss();
                JSON_boardid = result;
                take();
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String id = (String) params[0];
                    String member = (String) params[1];

                    String link = "http://hunsuk218.iptime.org/makeBoard.php";
                    String data = URLEncoder.encode("myid", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    data += "&" + URLEncoder.encode("member", "UTF-8") + "=" + URLEncoder.encode(member, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                        break;
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    Log.d(LOG_TAG, "phpdo3클래스 doInBackground()에러 : " + e.toString());
                    return "Exception: " + e.getMessage();
                }
            }
        }
        phpdo3 task = new phpdo3();
        task.execute(myId, member);
    }
    //ChatActivity 이동 부분 구현(중복부분 모은 메소드)
    public void goChatActivity(String myboardid) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        Log.d("check", "친구목록 보드 아이디 : " + myboardid);
        intent.putExtra("myboardid", myboardid);
        intent.putExtra("myid", myId);
        startActivity(intent);
        finish();
    }
    //DB의 내 친구목록에서 친구를 추가하는 부분(insert 문)
    public void addFriend(String friendid) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TalkRooms.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(TalkRooms.this, s, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String friendid = (String) params[0];

                    String link = "http://hunsuk218.iptime.org/addFriend.php";
                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(myId, "UTF-8");
                    data += "&" + URLEncoder.encode("friendid", "UTF-8") + "=" + URLEncoder.encode(friendid, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    Log.d(LOG_TAG, "InsertData클래스 doInBackground()에러 : " + e.toString());
                    return "Exception: " + e.getMessage();
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(friendid);
    }
    //DB에 저장한 이미지 경로 문자열을 가져와서 그 경로의 이미지를 내 사진으로 변경하는 부분
    public void getImg() {  //MainActivity의 onActivityResult() 참조
        try {
            JSONObject jsonObj = new JSONObject(JSON_myinfo);
            jarray_myinfo = jsonObj.getJSONArray("result");

            JSONObject c = jarray_myinfo.getJSONObject(0);
            img_uri = c.getString("img");

            Uri uri = Uri.parse(img_uri); // ==== 데이터에서 절대 경로로 이미지를 가져옴 4.28 치훈

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri); // ==== ConentResolver() : 어플 사이의 data 공유를 도와주는 메소드 4.28 치훈

            int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true); // ==== 비트맵 사진 크기 조절 4.28 치훈

            imgMyProfile.setImageBitmap(scaled); // ==== 크기 조정한 사진을 이미지뷰에 적용 4.28 치훈

        } catch (Exception e) {
            Log.d(LOG_TAG, "getImg()에러 : " + e.toString());
        }
    }
    //내 아이디를 통하여 내 정보를 DB에서 가져오는 메소드
    public void getMyinfo(String id) {
        class phpdo4 extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TalkRooms.this, "Please Wait", null, true, true);
                loading.show();
            }

            protected void onPostExecute(String result) {
                loading.dismiss();
                JSON_myinfo = result;
                getImg();
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String uri = "http://hunsuk218.iptime.org/getMyinfo.php";
                    String id = params[0];

                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

                    BufferedReader bufferedReader = null;
                    URL url = new URL(uri);

                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                        break;
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    Log.d(LOG_TAG, "phpdo3클래스 doInBackground()에러 : " + e.toString());
                    return "Exception: " + e.getMessage();
                }
            }
        }
        phpdo4 g = new phpdo4();
        g.execute(id);
    }
    //DB에 이미지 경로를 업데이트하여 이미지 수정 메소드
    private void insertToDatabase(String name, String img_uri) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TalkRooms.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String name = (String) params[0];
                    String img_uri = (String) params[1];
                    Log.d("check","사진주소임 : "+img_uri);

                    String link = "http://hunsuk218.iptime.org/phptest.php";
                    String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
                    data += "&" + URLEncoder.encode("img", "UTF-8") + "=" + URLEncoder.encode(img_uri, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    Log.d(LOG_TAG, "insertToDatabase클래스 doInBackground()에러 : " + e.toString());
                    return "Exception: " + e.getMessage();
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(name, img_uri);
    }
}