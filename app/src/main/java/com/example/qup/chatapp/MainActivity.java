package com.example.qup.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

/**
 * 주요 기능
 * 1. 로그인 페이지 activity_main.xml과 연결
 * 2. infodlg.xml 연결로 대화상자 띄워 회원가입 기능
 * 3. 로그인으로 다른 액티비티 이동
 * 주석완료
 */
public class MainActivity extends AppCompatActivity {
    // ==== 5.9 치훈추가
    View dialogView; // 다이얼로그 인플레이트 하기 위해?????
    Button adminlogin; // 관리자로그인 버튼
    String adminID = "a"; // 관리자모드 진입용 아이디
    String adminPW = "a"; // 관리자모드 진입용 비밀번호
    Button Insert, delete, Adelete, getResult; // 관리자모드 대화상자 안의 버튼 변수
    EditText etID, etPW; // 관리자모드 아이디, 패스워드
    TextView result; //  데이터 결과값 보는 곳(테스트 공간)
// ==== 5.9 치훈추가

    //activity_main.xml의 위젯들과 연결하는 변수들
    Button signUp;      //회원가입 버튼
    Button signIn;      //로그인 버튼
    EditText editID;    //아이디 입력 부분
    EditText editPWD;   //비밀번호 입력 부분

    View infodlgView;   //대화상자 생성 관련 변수
    ProgressDialog loading; //DB 연결간 로딩동안 띄우는 ProgressDialog 관련 변수

    //infodlg.xml의 위젯들과 연결하는 변수들
    EditText inputName; //회원가입 대화상자 안의 이름 작성부분
    EditText inputID;   //회원가입 대화상자 안의 아이디 작성부분
    EditText inputPWD;  //회원가입 대화상자 안의 암호 작성부분
    ImageView myimg;    //대화상자 내에서 사진
    Button addPhoto;    //사진 추가하는 버튼

    Uri uri;
    Bitmap bitmap;

    //대화상자의 문자열, 로그인시 문자열 등 값들을 저장하는 변수
    String id;          //아이디 저장
    String password;    //비빌번호 저장
    String img_uri = null;  //이미지의 경로를 저장
    String check_id;    //DB에서의 아이디의 문자열을 저장

    //이미지 요청을 위한 변수
    private int PICK_IMAGE_REQUEST = 1;

    //DB > php > json 으로 가져오는 것 관련 변수
    String myJSON;  //JSONObject와 연결을 돕는 문자열
    JSONArray jarray = null;    //DB 테이블의 각 데이터를 부르기 위해 사용
    private static final String LOG_TAG = "MainAcitivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DBHelper dbHelper = new DBHelper(getApplicationContext(), "SDB.db", null, 1); // ==== 5.9 치훈추가 DBHelper 클래스 객체 생성(아래에 있음)
        dialogView = View.inflate(MainActivity.this, R.layout.adminlogin, null); // ==== 5.9 치훈 추가 다이얼로그 인플레이트...
        etID = (EditText) dialogView.findViewById(R.id.et_id); // ==== 5.9 치훈추가 대화상자 ID 입력칸
        etPW = (EditText) dialogView.findViewById(R.id.et_pw); // ==== 5.9 치훈추가 대화상자 비번 입력칸
        getResult = (Button) dialogView.findViewById(R.id.select); // ==== 5.9 치훈추가 DB 조회하기위한 참조
        Insert = (Button) dialogView.findViewById(R.id.insert); // ==== 5.9 치훈추가 대화상자 안의 DB 데이터 삽입
        delete = (Button) dialogView.findViewById(R.id.delete); // ==== 5.9 치훈추가 대화상자 안의 DB 특정 내용 삭제 버튼
        Adelete = (Button) dialogView.findViewById(R.id.Adelete); // ==== 5.9 치훈추가 대화상자 안의 DB 모든 내용 삭제 버튼
        result = (TextView) dialogView.findViewById(R.id.result); // ==== 5.9 치훈추가 대화상자 안의 텍스트뷰 사용****
        //activity_main의 각 id에 맞는 위젯들을 불러옴(왼쪽 각 변수명에 저장)
        editID = (EditText) findViewById(R.id.edit_id);
        editPWD = (EditText) findViewById(R.id.edit_pwd);
        signUp = (Button) findViewById(R.id.btn_sign_up);
        signIn = (Button) findViewById(R.id.btn_sign_in);
        adminlogin = (Button) findViewById(R.id.adminlogin); // ==== 5.9 치훈추가 관리자모드 버튼 참조
        //infodlg.xml의 각 id에 맞는 위젯들을 불러옴(왼쪽 각 변수명에 저장)
        infodlgView = View.inflate(MainActivity.this, R.layout.infodlg, null);
        inputName = (EditText) infodlgView.findViewById(R.id.input_name);
        inputID = (EditText) infodlgView.findViewById(R.id.input_id);
        inputPWD = (EditText) infodlgView.findViewById(R.id.input_pwd);
        myimg = (ImageView) infodlgView.findViewById(R.id.Myimg); // ==== 대화상자에 적용한 레이아웃의 버튼을 참조 4.28 치훈
        addPhoto = (Button) infodlgView.findViewById(R.id.addPhoto); // ===== infodlg.xml 레이아웃의 버튼 참조 시 방법 4.28 치훈

        editID.setPrivateImeOptions("defaultInputmode=english;");   //아이디를 초기에 키보드를 영어로 바꿔주는 메소드(xml 세팅변경)

        /*로그인 버튼 클릭 시
        * id,password 가져와서 DB의 데이터와 일치하는지 여부 파악
        * 맞으면 talk_room.xml와 연결된 TalkRooms.java로 이동*/
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = editID.getText().toString();
                password = editPWD.getText().toString();
                getData(id, password);
            }


        });
        /*회원가입 버튼 클릭 시
        * infodlg.xml과 연결하는 대화상자 생성하여 실행시킨다
        * 대화상자 안에서 작성한 계정이 DB에 존재하면 계징이 추가 되지 않는다 */
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);   //대화상자 관련 정보 저장한 dlg 객체 생성
                dlg.setTitle("회원가입 제목");
                dlg.setView(infodlgView);
                addPhoto.setOnClickListener(new View.OnClickListener() { // 사진추가 클릭 시 이벤트
                    @Override
                    public void onClick(View v) {
                        loadImagefromGallery(); // 앨범을 불러와 사진 선택하여 대화상자 ImageView에 보여준다
                    }
                });
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() { //대화상자 확인버튼 생성 + 버튼 클릭시
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input_id = inputID.getText().toString();
                        String input_pwd = inputPWD.getText().toString();
                        String input_name = inputName.getText().toString();
                        //대화상자에서 작성한 내용 DB에 저장시키는 함수, img_uri는 loadImagefromGallery()내의 startActivityForResult()에서 앨범 경로 설정
                        insertToDatabase(input_id, input_pwd, input_name, img_uri);
                    }
                });
                dlg.setNegativeButton("취소", null);   //대화상자 취소버튼 생성 + 클릭시 하던 작성하던 회원가입 내용 취소
                dlg.show(); //설정한 대화상자를 화면에 띄워준다
            }
        });

        adminlogin.setOnClickListener(new View.OnClickListener() { //  아래로 ==== 5.9 치훈추가, 관리자모드 버튼 이벤트
            @Override
            public void onClick(View v) {
                id = editID.getText().toString();
                password = editPWD.getText().toString();
                if (id.equals(adminID) && password.equals(adminPW)) { // 아이디, 비밀번호가 관리자일 때
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this); // 대화상자 생성
                    dlg.setTitle("관리자모드");
                    dlg.setView(dialogView);
                    Insert.setOnClickListener(new View.OnClickListener() { // ==== 특정 내용 삽입 이벤트
                        @Override
                        public void onClick(View v) {
                            String ID = etID.getText().toString();
                            String PW = etPW.getText().toString();
                            dbHelper.insert(ID, PW);
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() { // ==== 특정내용 삭제 이벤트
                        @Override
                        public void onClick(View v) {
                            String ID = etID.getText().toString();
                            dbHelper.delete(ID);
                        }
                    });
                    Adelete.setOnClickListener(new View.OnClickListener() { // ==== 테이블 제거 이벤트
                        @Override
                        public void onClick(View v) {
                            dbHelper.Adelete();
                        }
                    });
                    getResult.setOnClickListener(new View.OnClickListener() { // ==== DB 조회 이벤트
                        @Override
                        public void onClick(View v) {
                            Log.d("알림", "조회를 클릭한 부분입니다.");
                            dbHelper.getResult();
                            Log.d("알림", "조회를 실행하였습니다,");
                        }
                    });
                    dlg.show();

                } else {
                    Toast.makeText(getApplicationContext(), "관리자가 아님", Toast.LENGTH_SHORT).show();
                }
            }
        }); // ==== 5.9 까지 치훈 추가


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //메뉴 생성
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu1,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        //선택한 메뉴에 대한 행동
        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);

        switch (item.getItemId()){
            case R.id.clock:    //시간을 선택하면 시계를 가지고 있는 대화상자 나타남
                dialogView = (View) View.inflate(MainActivity.this,R.layout.clockdlg,null);
                dlg.setTitle("시간");
                dlg.setView(dialogView);
                dlg.setPositiveButton("확인",null);
                dlg.show();
                return true;
            case R.id.date:     //날짜를 선택하면 캘린더 뷰를 가지고 있는 대화상자 나타남
                dialogView = (View) View.inflate(MainActivity.this,R.layout.calenderdlg,null);
                dlg.setTitle("날짜");
                dlg.setView(dialogView);
                dlg.setPositiveButton("확인",null);
                dlg.show();
                return true;
        }
        return false;
    }

    //inner 클래스를 정의하고 인스턴스를 만들어 실행한다
    private void insertToDatabase(String id, String password, String name, String img_uri) {
        //AsyncTask 상속하여 DB 연결간 로딩동안의 시간동안 기다리도록 한다
        class InsertData extends AsyncTask<String, Void, String> {

            //가장 먼저 실행되고 doInBackground()를 실행하는 동안 ProgressDialog를 띄우고 있도록 한다
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
            }

            //doInBackground() 완료 후 실행
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }

            //백그라운드 실행, DB에서 JSON을 통해 보낸 문자열을 받는다
            @Override
            protected String doInBackground(String... params) {
                try {
                    //params각 값은 insertToDatabase()을 사용할때 넣은 매개변수들이다
                    String id = params[0];
                    String password = params[1];
                    String name = params[2];
                    String img_uri = params[3];

                    //data 안의 문자열 : "id=아이디&password=비밀번호&name=이름&img=이미지경로"
                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                    data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
                    data += "&" + URLEncoder.encode("img", "UTF-8") + "=" + URLEncoder.encode(img_uri, "UTF-8");

                    //DB와 연결된 웹 php 서버와 연결
                    String link = "http://hunsuk218.iptime.org/phptest.php";//phptest.php와 연결하여 문자열을 주고 받는다
                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);

                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); //서버에 보내기 위한 변수
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));   //서버에 받기 위한 변수
                    StringBuilder sb = new StringBuilder(); //받은 문자열을 StringBuilder의 문자열에 저장

                    wr.write(data); //wr에 보낼 문자열을 넣는다.
                    wr.flush();     //웹 php 서버에 넣은 문자열을 보낸다.

                    String line = null;
                    while ((line = reader.readLine()) != null) {    //서버에서 보낸 line이 없을때까지 반복
                        sb.append(line);    //줄 내용들을 sb 문자열에 추가
                        break;
                    }
                    return sb.toString();   //서버에서 보낸 문자열에 대한 응답을 받아온다
                } catch (Exception e) {
                    Log.d(LOG_TAG, "InsertData클래스 doInBackground()에러 : " + e.toString());
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData(); //phpdo 이너클래스의 객체 생성
        task.execute(id, password, name, img_uri);  //부모클래스의 메소드인 execute 실행
    }

    //getData에서 호출한 기능 부분 >> 아이디를 가져와서 DB에 있는지 여부를 파악한다
    public void checkId() {

        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            jarray = jsonObj.getJSONArray("result");    //json에서 result 부분에 있던 문자열들을 jarray에 저장

            for (int i = 0; i < jarray.length(); i++) {
                JSONObject c = jarray.getJSONObject(i);
                check_id = c.getString("id");   //DB의 아이디를 check_id에 저장
            }
            if (id.equals(check_id) && !id.equals("")) {    //입력한 id와 DB내의 id가 같고 id가 공백이 아닐때
                //TalkRooms.class로 "myid"키의 id값과 함께 이동하고 현재 액티비티를 종료한다
                Intent intent = new Intent(getApplicationContext(), TalkRooms.class);
                intent.putExtra("myid", id);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "로그인실패", Toast.LENGTH_SHORT).show(); //토스트로 실패했다고 보여줌
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, "checkId()에러 : " + e.toString());
            e.printStackTrace();
        }
    }

    //내가 입력한 id, password 와 같은 데이터가 DB에 있는지 확인
    public void getData(String id, String password) {
        //AsyncTask 상속하여 DB 연결간 로딩동안의 시간동안 기다리도록 한다
        class phpdo extends AsyncTask<String, Void, String> {
            //가장 먼저 실행되고 doInBackground()를 실행하는 동안 ProgressDialog를 띄우고 있도록 한다
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
                loading.show();
            }

            //doInBackground() 완료 후 실행
            protected void onPostExecute(String result) {
                loading.dismiss();
                myJSON = result;
                checkId();
            }

            //백그라운드 실행, DB에서 JSON을 통해 보낸 문자열을 받는다
            @Override
            protected String doInBackground(String... params) { //insertToDatabase()의 doInBackground() 참고(거의 유사)
                try {
                    //params각 값은 getData()을 사용할때 넣은 매개변수들이다
                    String id = params[0];
                    String password = params[1];

                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

                    String link = "http://hunsuk218.iptime.org:80/getdata.php"; //getdata.php와 연결하여 문자열을 주고 받는다
                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);

                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = null;
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                        break;
                    }
                    return sb.toString().trim();    //trim() : 문자열 앞뒤 공백 제거
                } catch (Exception e) {
                    Log.d(LOG_TAG, "phpdo클래스 doInBackground()에러 : " + e.toString());
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        phpdo g = new phpdo();  //phpdo 이너클래스의 객체 생성
        g.execute(id, password);//부모클래스의 메소드인 execute 실행
    }

    //Intent를 통하여 앨범으로 이동시키는 함수
    public void loadImagefromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK); // 어떤 앱으로 연결시킬지 묻게 되는 Intent
        intent.setType("image/*"); // 이미지 관련 앱만을 보여주어 선택하도록 한다
        // Intent 시작 : 선택한 앨범 앱으로 이미지 선택할 수 있다,PICK_IMAGE_REQUEST로 이 Intent에서 보낸다는 정보를 확인한다
        startActivityForResult(intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // 이미지 선택 작업 관련 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  //오버라이드 하는 원래 함수의 기능을 그대로 가져온다
        try {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) { // 이미지를 하나 골랐을 때
                //data는 선택한 이미지를 의미한다
                uri = data.getData(); // 선택한 이미지의 경로를 uri에 저장

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri); // Bitmap으로 그 경로의 정보를 통하여 사진으로 변환 저장

                int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth())); //사진조절을 위한 인자(정확하지 않지만 사진 비율을 나타냈다고 생각함)

                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true); // Bitmap 새로 생성하여 사진의 크기를 조절하여 Bitmap에 저장
                myimg.setImageBitmap(scaled); // 크기 조정한 사진을 대화상자 내 ImageView에 적용
            } else {
                Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "로딩에 오류가 있습니다.", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "onActivitiyResult()에러 : " + e.toString());    //하단 LogCat으로 에러 내용 출력
        }
    }
    // ==== 5.9 아래부분 치훈추가

    public class DBHelper extends SQLiteOpenHelper { // ==== 데이터베이스를 생성하고 관리할 수 있도록 도와주는 SQLiteOpenHelper
        public static final String TABLE_NAME = "GDB"; // 테이블 이름
        public static final int TABLE_VERSION = 3; // 테이블 버전
        public static final String ADMIN_ID = "a"; // 관리자 아이디
        public static final String ADMIN_PW = "a"; // 관리자 비밀번호
        String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + "id TEXT, "
                + "pw TEXT"
                + ");";

        // DBHelper 생성자, 관리할 DB 이름과 버전 정보를 받음
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, TABLE_NAME, null, TABLE_VERSION); // ==== 생성자 실행
            Log.d("알림", "DBHelper 생성자를 실행하였습니다,");
        }

        @Override
        public void onCreate(SQLiteDatabase db) { // ==== DB를 새로 생성할 때 호출되는 함수
            Log.d("알림", "DB를 첫 호출을 시작합니다.");
            // 새로운 테이블 생성
            // 이름은 GDB이고, 자동으로 값이 증가하는 _id 정수형 기본 키 컬럼(데이터값과 테이블에서의 각 열) = 속성
            // id, pw, create_at 3개의 컬럼으로 구성된 테이블
            db.execSQL(SQL_CREATE_TABLE);
            Log.d("알림", "DB를 처음 만들었습니다.");
            db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES ('"
                    + ADMIN_ID + "', '"
                    + ADMIN_PW + "');");
            Log.d("알림", "DB에 관리자 정보를 추가하였습니다.");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            Log.d("알림", "DB를 제거하였습니다.");
            onCreate(db);
            Log.d("알림", "DB를 다시 생성하였습니다.");
        }

        public void insert(String id, String pw) { // 문자열 입력할거면 "abc" 가 아닌 'abc' 사용되어야함????
            SQLiteDatabase db = getWritableDatabase();
            Log.d("알림", "db를 쓰기 가능하게 만들었습니다.");
            try {
                if (id.equals("") && id.equals("a")) { // ==== 추가할 아이디가 공백이거나 관리자 아이디 일 때
                    db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES ('"
                            + id + "', '"
                            + pw + "');");
                    Log.d("알림", "insert를 실행하였습니다.");
                    db.close();
                    Log.d("알림", "db를 닫았습니다,");
                    Toast.makeText(getApplicationContext(), "DB 입력 완료", Toast.LENGTH_SHORT).show();
                    Log.d("알림", "입력완료 토스트   메세지를 출력했습니다.");
                } else
                    Log.d("알림", " 삽입 if문 거짓입니다.");
                Toast.makeText(getApplicationContext(), "공백 또는 관리자를 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                db.close();
                Log.d("알림", "db 닫음");
            } catch (SQLiteException e) {
                Log.d("알림", "삽입오류가 발생했습니다.");
                Toast.makeText(getApplicationContext(), "오류입니다.", Toast.LENGTH_SHORT).show();
            }
        }

        public void getResult() { // ==== 5.9 치훈추가 DB내용 조회하기
            SQLiteDatabase db = getReadableDatabase();
            Log.d("알림", "db를 읽기 가능하게 만들었습니다.");
            try {

                Cursor cursor;
                cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
                Log.d("알림", "DB값을 요청했습니다.");
                int i = 0;
                String strInfo = "회원번호" + "\n" + "아이디" + "\n" + "비밀번호" + "\n" + "" + "\r\n" + "==============================" + "\n";
                while (cursor.moveToNext()) {
                    Log.d("알림", "값들 입력중");
                    strInfo += i + "번회원\n" + cursor.getString(0) + "\n" + cursor.getString(1) + "\n" + "==============================" + "\n";
                    i++;
                }
                Log.d("알림", "값 입력 완료");
                result.setText(strInfo);
                Log.d("알림", "결과값이 출력되었습니다.");
                Toast.makeText(getApplicationContext(), "조회하였습니다.", Toast.LENGTH_SHORT).show();
                Log.d("알림", "조회하였습니다. 토스트 메세지 출력");
                cursor.close();
                Log.d("알림", "cursor를 닫음");
                db.close();
                Log.d("알림", "db를 닫음");
            } catch (SQLiteException e) {
                Toast.makeText(getApplicationContext(), "오류입니다.", Toast.LENGTH_SHORT).show();
            }
        }


        public void delete(String id) { // ==== 5.9 치훈추가 특정 내용 삭제 함수
            SQLiteDatabase db = getWritableDatabase();
            try {
                if (!id.equals("") && !id.equals("a")) { // ==== 삭제할 아이디가 공백이거나 관리자 아이디일 때
                    Log.d("알림", "삭제 실행 전");
                    db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE id='" + id + "';");
                    Log.d("알림", "삭제 실행 후");
                    db.close();
                    Log.d("알림", "db 닫음");
                    Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                    Log.d("알림", "삭제완료 토스트 메세지 출력");
                } else
                    Log.d("알림", "삭제 if문 거짓");
                Toast.makeText(getApplicationContext(), "공백 또는 관리자를 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                db.close();
                Log.d("알림", "db 닫음");
            } catch (SQLiteException e) {
                Toast.makeText(getApplicationContext(), "오류입니다.", Toast.LENGTH_SHORT).show();
            }

        }

        public void Adelete() { // 테이블 삭제 후 새로 생성하는 것으로 구현
            SQLiteDatabase db = getWritableDatabase();
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                Log.d("알림", "모두 삭제 실행(테이블 삭제)");
                Toast.makeText(getApplicationContext(), "모두 삭제 완료", Toast.LENGTH_SHORT).show();
                Log.d("알림", "모두삭제완료 토스트 메세지 출력");
                onCreate(db);
                Log.d("알림", "db 테이블 다시 생성됨");
            } catch (SQLiteException e) {
                Log.d("알림", "모두삭제 오류");
                Toast.makeText(getApplicationContext(), "오류입니다.", Toast.LENGTH_SHORT).show();
                db.close();
                Log.d("알림", "db 닫음");
            }
        }
    }

}