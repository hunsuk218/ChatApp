package com.example.qup.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * 주요 기능
 * 1. 채팅 페이지
 * 2. tcp 소켓 통신으로 서버와 연결
 * 3. 서버에 방 id와 내 id를 보낸다
 * 4. 자신이 입력하는 내용을 버튼 클릭 시 서버에 보내는 기능
 * 5. 친구가 입력한 내용을 받을 수 있도록 상시 read하도록 기다리는 기능
 * 6. 채팅방에 들어왔을때 DB에서 현재까지 대화기록을 가져와 조회한다
 * 주석완료
 */

public class ChatActivity extends Activity {
    ScrollView scrollTalk;
    LinearLayout outputLayout;    //대화내용 보이는 부분
    LinearLayout inputLayout;       //입력내용 보이는 부분
    EditText editTalk;  //내가 대화내용 입력 부분
    Button btnTalk;     //입력 후 채팅방에 올리기 위한 버튼

    Handler handler;
    TextLogRunnable runnable;   //동적 위젯 생성을 위해 구현한 클래스

    String addr = "hunsuk218.iptime.org";   //서버주소
    String sendStr; //내가 보내는 문자열
    String boardid; //방 id
    String myId;    //내 id

    //tcp통신을 위한 소켓과 데이터 입출력을 위한 변수들
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    //DB에서 채팅기록을 가져오기 위한 변수들
    String JSON_chat;
    JSONArray jarray_chat = null;

    private static final String LOG_TAG = "ChatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //talkRooms에서 받아온 값을 키를 통해 저장
        Intent intent = new Intent(this.getIntent());
        boardid = intent.getStringExtra("myboardid");
        myId = intent.getStringExtra("myid");

        //사용되는 각 위젯 참조
        scrollTalk = (ScrollView) findViewById(R.id.scroll_talk);
        outputLayout = (LinearLayout) findViewById(R.id.output_talk_layout);
        inputLayout = (LinearLayout) findViewById(R.id.input_talk_layout);
        editTalk = (EditText) findViewById(R.id.edit_talk);
        btnTalk = (Button) findViewById(R.id.btn_talk);

        //runnable과 handler은 동적 위젯 생성을 위해 필요하다
        handler = new Handler();
        runnable = new TextLogRunnable();
        Thread startThread = new Thread(new Runnable() { //서버에 입력한 값 전달
            @Override
            public void run() { //스레드 생성 이유 : 통신을 위한 socket은 반드시 다중 스레드를 사용하도록 안드로이드 스튜디오에서 정해둠
                try {
                    Log.d(LOG_TAG, "서버에 연결중입니다. 서버 IP : " + addr);
                    // 소켓을 생성하여 연결을 요청한다.
                    socket = new Socket(addr, 8081);
                    Log.d(LOG_TAG, "서버에 연결되었습니다.");
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF(boardid);  //서버에 방 아이디 보낸다
                    in.readUTF();   //그에 대한 문자열 받음
                    Log.d(LOG_TAG, "서버에 연결" + myId);
                    out.writeUTF(myId); //서버에 내 아이디 보낸다
                    Log.d(LOG_TAG, "내 이름 : " + in.readUTF());

                    //상대방의 대화를 받기 위한 스레트를 생성 및 실행
                    Thread clientIn = new Thread(new ClientReceiver(socket));
                    clientIn.start();
                } catch (Exception ex) {
                    Log.d(LOG_TAG, "startThread 에러 : " + ex.toString());
                }
            }
        });
        startThread.start();

        btnTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //내용 입력하고 서버 보내기
                Log.d(LOG_TAG, "입력한 값 길이 : " + editTalk.getText().length());
                sendStr = editTalk.getText().toString();
                Log.d(LOG_TAG, "입력한 문자열 : " + sendStr);
                if (editTalk.getText().length() > 0) { //입력 시에만 실행
                    setTalkLog(sendStr, true);

                    scrollTalk.postDelayed(new Runnable() {
                        public void run() {
                            scrollTalk.fullScroll(View.FOCUS_DOWN); //스크롤을 현재 페이지의 최 하단으로 가게 하는 메소드
                        }
                    }, 100);    //0.1초 후 실행 >> delay를 주고 실행하는 이유는 setTalkLog() 후 페이지의 최하단의 정보가 최신화 되는것을 기다렸다가 실행하게 하기 위해서이다
                    editTalk.setText("");   //입력한 뒤 다시 내용 공란으로 변경

                    //스레드를 생성하여 서버에 내가 입력한 내용을 보낸다(서버에서 상대방에게 문자열을 전달한다
                    Thread clientOut = new Thread(new ClientSend(socket));
                    clientOut.start();
                }
            }
        });
        editTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //키보드 올라올때 최하단으로 고정하기 위한 부분(실패)
                scrollTalk.postDelayed(new Runnable() {
                    public void run() {
                        scrollTalk.fullScroll(View.FOCUS_DOWN);
                    }
                }, 1000);
            }
        });
    }
    @Override
    public void onStart() { //DB 내용 불러와 화면에 띄운다
        super.onStart();
        getContent(boardid);
    }

    @Override
    public void onBackPressed() {   //뒤로가기 누를 시 내 아이디를 TalkRooms에 보낸다
        Intent intent = new Intent(getApplicationContext(), TalkRooms.class);
        intent.putExtra("myid",myId);
        startActivity(intent);
        finish();
    }
    //자바로 TextView를 생성하는 부분, 채팅시 추가되는 문자열을 띄워주는 메소드
    public void setTalkLog(String s, boolean myTalk) {
        TextView talkLog = new TextView(ChatActivity.this);
        talkLog.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (myTalk) {   //내가 입력했던 기록일 때
            talkLog.setGravity(Gravity.END);
            talkLog.setBackgroundColor(Color.parseColor("#888888"));
        } else {      //다른 사람 입력한 기록일 때
            talkLog.setGravity(Gravity.START);
            talkLog.setBackgroundColor(Color.parseColor("#BBBBBB"));
        }
        talkLog.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);   //글자 크기 조정
        talkLog.setText(s);
        outputLayout.addView(talkLog);  //레이아웃에 텍스트뷰 추가ㅣ
    }
    //setTalkLog의 setText(),addView()하기 위해 핸들러라는 기능을 이용하여 실행하기 위해 만든 클래스
    public class TextLogRunnable implements Runnable {
        String inputText;

        public void setText(String s) {
            inputText = s;
        }
        @Override
        public void run() {
            setTalkLog(inputText, false);
        }
    }
    //버튼 클릭 시 생성된 스레드에서 실행하는 클래스
    private class ClientSend implements Runnable {
        Socket socket;
        DataOutputStream out;

        ClientSend(Socket socket) {
            this.socket = socket;
            try {
                out = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        public void run() {
            // TODO Auto-generated method stub
            try {
                out.writeUTF(sendStr);  //내가 입력한 내용(sendStr)을 서버에 보낸다
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    //서버에서 상대방의 채팅을 듣기 위한 부분
    private class ClientReceiver implements Runnable {
        Socket socket;
        DataInputStream in;

        ClientReceiver(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
            }
        }

        public void run() {
            while (in != null) {
                try {
                    String[] array;
                    String receive = in.readUTF();  //readUTF() 실행 시 서버로부터 값을 받을때까지 그 라인에 멈추므로 사용시 String 변수 생성 후 받아와서 사용해야 한다
                    Log.d("check", "clientR에서 받은값 : " + receive);
                    array = receive.split(" "); //" " 기준으로 문자열을 나누어 배열에 저장 >> 서버에서 "방아이디 보낸사람이름 내용"순으로 보내기 때문
                    Log.d("check", "array[0]:" + array[0] + "boardid:" + boardid);
                    if (boardid.equals(array[0])) { //방아이디가 같을때(즉 같은 방에 사람일 때
                        //동적으로 TextView 위젯을 생성하여 상대방이 보낸 내용이 모바일 화면에 출력된다
                        runnable.setText(array[2]);
                        handler.post(runnable);

                        scrollTalk.postDelayed(new Runnable() {
                            public void run() {
                                scrollTalk.fullScroll(View.FOCUS_DOWN);
                            }
                        }, 100);    //상대방의 내용을 받고 0.1초 뒤 스크롤을 내려 최신의 채팅내용을 보도록 한다
                    } else {

                    }
                } catch (IOException e) {
                }
            }
        } // run
    }
    //DB에서 가져온 내용을 화면에 출력하는 메소드
    public void showContent() {
        try {
            JSONObject jsonObj = new JSONObject(JSON_chat);
            jarray_chat = jsonObj.getJSONArray("result");

            for (int i = 0; i < jarray_chat.length(); i++) {
                JSONObject c = jarray_chat.getJSONObject(i);
                String userid = c.getString("userid");
                String content = c.getString("content");

                if (userid.equals(myId)) {
                    //내가 작성한 내용일 때
                    setTalkLog(content, true);
                } else {
                    //상대방이 작성한 내용일 때
                    setTalkLog(content, false);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        scrollTalk.postDelayed(new Runnable() {
            public void run() {
                scrollTalk.fullScroll(View.FOCUS_DOWN);
            }
        }, 100);

    }
    //DB에서 select문으로 한 채팅내용을 가져오는 부분 >> MainActivity의 getData() 참조
    public void getContent(String boardid) {
        class phpdo extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ChatActivity.this, "Please Wait", null, true, true);
                loading.show();
            }

            protected void onPostExecute(String result) {
                loading.dismiss();
                JSON_chat = result;
                showContent();
            }

            protected String doInBackground(String... params) {
                try {
                    String uri = "http://hunsuk218.iptime.org:80/getContent.php";
                    String boardid = (String) params[0];

                    String data = URLEncoder.encode("boardid", "UTF-8") + "=" + URLEncoder.encode(boardid, "UTF-8");

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
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        phpdo g = new phpdo();
        g.execute(boardid);
    }
}