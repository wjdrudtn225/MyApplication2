package com.example.wjdru.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.redwings.naversigninexam.R;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private OAuthLoginButton naverLogInButton;
    private static OAuthLogin naverLoginInstance;
    Button btnGetApi, btnLogout;
    public static final int REQUEST_CODE_ANOTHER = 1001;

    static final String CLIENT_ID = "3bRGcsPw3gPG5huXBpmz";
    static final String CLIENT_SECRET = "v8BiQdhYr9";
    static final String CLIENT_NAME = "네이버 아이디 로그인";

    TextView tv_mail;
    static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        init_View();
    }



    //초기화
    private void init(){
        context = this;
        naverLoginInstance = OAuthLogin.getInstance();
        naverLoginInstance.init(this,CLIENT_ID,CLIENT_SECRET,CLIENT_NAME);
    }
    //변수 붙이기
    private void init_View(){
        naverLogInButton = (OAuthLoginButton)findViewById(R.id.buttonNaverLogin);

        //로그인 핸들러
        OAuthLoginHandler naverLoginHandler  = new OAuthLoginHandler() {
            @Override
            public void run(boolean success) {
                if (success) {//로그인 성공
                    Toast.makeText(context,"로그인 성공",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                    startActivityForResult(intent,REQUEST_CODE_ANOTHER);
                    finish();
                } else {//로그인 실패
                    String errorCode = naverLoginInstance.getLastErrorCode(context).getCode();
                    String errorDesc = naverLoginInstance.getLastErrorDesc(context);
                    Toast.makeText(context, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                }
            }

        };

        naverLogInButton.setOAuthLoginHandler(naverLoginHandler);
        tv_mail = (TextView)findViewById(R.id.tv_mailaddress);
        btnGetApi = (Button)findViewById(R.id.btngetapi);
        btnGetApi.setOnClickListener(this);
        btnLogout = (Button)findViewById(R.id.btnlogout);
        btnLogout.setOnClickListener(this);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode ==REQUEST_CODE_ANOTHER){
            Toast toast = Toast.makeText(getApplicationContext(), "메소드가 호출됨 : "+ requestCode + "결과 코드 : " + resultCode ,Toast.LENGTH_LONG);
            toast.show();

         if(resultCode ==RESULT_OK){
             String name = intent.getExtras().getString("name");
             toast = Toast.makeText(getApplicationContext(),"응답으로 전달된 name : " +name , Toast.LENGTH_LONG);
             toast.show();
         }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btngetapi){
            new RequestApiTask().execute();//static이 아니므로 클래스 만들어서 시행.
        }
        if(v.getId() == R.id.btnlogout){
            naverLoginInstance.logout(context);
            tv_mail.setText((String) "");//메일 란 비우기
        }
    }


    private class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {//작업이 실행되기 전에 먼저 실행.
            tv_mail.setText((String) "");//메일 란 비우기
        }

        @Override
        protected String doInBackground(Void... params) {//네트워크에 연결하는 과정이 있으므로 다른 스레드에서 실행되어야 한다.
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = naverLoginInstance.getAccessToken(context);
            return naverLoginInstance.requestApi(context, at, url);//url, 토큰을 넘겨서 값을 받아온다.json 타입으로 받아진다.
        }

        protected void onPostExecute(String content) {//doInBackground 에서 리턴된 값이 여기로 들어온다.
            try {
                JSONObject jsonObject = new JSONObject(content);
                JSONObject response = jsonObject.getJSONObject("response");
                String email = response.getString("email");
                tv_mail.setText(email);//메일 란 채우기
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}