package com.example.qup.chatapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 주요 기능
 * 1. 각 방들에서 상대방의 내용을 받을 소켓들 생성
 * 2. 채팅방이 아닐때는 토스트를 통하여 상대의 대화 내용을 확인하도록 구현
 * 주석완료
 */

public class ChatService extends Service {
    String addr = "hunsuk218.iptime.org";
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    Handler handler;
    ToastRunnable runnable;

    Thread clientIn;

    private static final String LOG_TAG = "ChatService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {    //서비스 시작 시 onStartCommand와 함께 실행
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate();
        handler = new Handler();
        runnable = new ToastRunnable();
    }

    @Override
    public void onDestroy() {   //서비스 종료 시 실행
        Log.d(LOG_TAG, "onDestroy()");
        try {
            clientIn.interrupt();
            socket.close();
        }catch (Exception e){
            Log.d("check2", "onDestroy() 에러 : " + e.toString());
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");
        // 소켓을 생성하여 연결을 요청한다.
        final Intent getIntent = intent;
        Thread startThread = new Thread(new Runnable() {
            @Override
            public void run() { //스레드, 소켓 관련 내용은 ChatActivity 참조
                try {
                    Log.d(LOG_TAG, "서버에 연결중입니다. 서버 IP : " + addr);
                    String myid = getIntent.getStringExtra("myid"); //내 아이디를 가져온다
                    ArrayList<String> listBoardid = getIntent.getStringArrayListExtra("listBoardid");   //방들의 아이디를 저장한 배열 가져온다

                    for(int i=0;i<listBoardid.size();i++){ //나의 대화목록의 각 방마다 소켓으로 연결하여 상대방의 입력내용을 받는다
                        socket = new Socket(addr, 8081);
                        Log.d(LOG_TAG, "서버에 연결되었습니다."+listBoardid.get(i)+"번방");
                        in = new DataInputStream(socket.getInputStream());
                        out = new DataOutputStream(socket.getOutputStream());

                        out.writeUTF(listBoardid.get(i)+"");
                        in.readUTF();

                        out.writeUTF(myid);
                        Log.d(LOG_TAG, "내 이름 : " + in.readUTF());

                        //상대방의 입력 내용을 받는 스레드
                        clientIn = new Thread(new ClientReceiver(socket));
                        clientIn.start();
                    }
                } catch (Exception ex) {
                    Log.d(LOG_TAG, "버튼 안 에러 : " + ex.toString());
                }
            }
        });
        startThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

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
            while (in != null && !Thread.currentThread().isInterrupted()) {
                try {
                    String[] array;
                    String receive = in.readUTF();
                    Log.d(LOG_TAG, "clientR에서 받은값 : " + receive);
                    array = receive.split(" ");
                    runnable.setText(array[1]+":"+array[2]); //"보낸사람이름 : 내용"으로 토스트메세지가 뜨도록 한다
                    handler.post(runnable);
                } catch (IOException e) {
                    Log.d(LOG_TAG,e.toString());
                }
            }
        } // run
    }

    private class ToastRunnable implements Runnable {   //동적 위젯 생성을 위해 만든 클래스
        String inputText;

        public void setText(String s) {
            inputText = s;
        }

        @Override
        public void run() {
            Toast.makeText(ChatService.this.getApplicationContext(), inputText, Toast.LENGTH_SHORT).show();
        }
    }
}