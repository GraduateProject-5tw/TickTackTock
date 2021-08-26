package com.GraduateProject.TimeManagementApp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private String realCode;
    //private DBOpenHelper mDBOpenHelper;
    private Button mBtRegisteractivityRegister;
    private RelativeLayout mRlRegisteractivityTop;
    private ImageView mIvRegisteractivityBack;
    private LinearLayout mLlRegisteractivityBody;
    private EditText mEtRegisteractivityEmail;
    private EditText mEtRegisteractivityPassword1;
    private EditText mEtRegisteractivityPassword2;
    private EditText mEtRegisteractivityPhonecodes;
    private ImageView mIvRegisteractivityShowcode;
    private RelativeLayout mRlRegisteractivityBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();

        //mDBOpenHelper = new DBOpenHelper(this);


    }
    private void initView(){
        mBtRegisteractivityRegister = findViewById(R.id.bt_register_activity_register);
        mRlRegisteractivityTop = findViewById(R.id.rl_register_activity_top);
        mIvRegisteractivityBack = findViewById(R.id.iv_register_activity_back);
        mLlRegisteractivityBody = findViewById(R.id.ll_register_activity_body);
        mEtRegisteractivityEmail = findViewById(R.id.et_register_activity_email);
        mEtRegisteractivityPassword1 = findViewById(R.id.et_register_activity_password1);
        mEtRegisteractivityPassword2 = findViewById(R.id.et_register_activity_password2);



        mIvRegisteractivityBack.setOnClickListener(this);

        mBtRegisteractivityRegister.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_register_activity_back: //返回登入頁面
                Intent intent1 = new Intent(this, LoginActivity.class);
                startActivity(intent1);
                finish();
                break;

            case R.id.bt_register_activity_register:    //註冊按鈕
                //取得使用者输入的信箱、密码、验证码
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                String email = mEtRegisteractivityEmail.getText().toString().trim();
                String password1 = mEtRegisteractivityPassword1.getText().toString().trim() ;
                String password2 = mEtRegisteractivityPassword2.getText().toString().trim();
                if(email.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"清輸入信箱",Toast.LENGTH_SHORT).show();
                }
                else {
                    if (email.matches(emailPattern)) {
                        Toast.makeText(getApplicationContext(),"信箱格式正確",Toast.LENGTH_SHORT).show();

                        if (password1.equals("")||password2.equals("")){	//判断兩次密碼是否為空
                            Toast.makeText(getApplicationContext(),"密碼不能為空",Toast.LENGTH_SHORT).show();
                        }
                        else if(password1.equals(password2)){
                            Toast.makeText(getApplication(),"註冊成功",Toast.LENGTH_SHORT).show();
                            String password = md5(password1);
                        }
                        else{
                            Toast.makeText(getApplication(),"密碼有錯",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"信箱格式錯誤", Toast.LENGTH_SHORT).show();
                    }
                }







                //注册验证
                /*
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)  ) {

                    //将用户名和密码加入到数据库中
                    mDBOpenHelper.add(email, password);
                    Intent intent2 = new Intent(this, MainActivity.class);
                    startActivity(intent2);
                    finish();
                    Toast.makeText(this,  "验证通过，注册成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "未完善信息，注册失败", Toast.LENGTH_SHORT).show();
                }   if(emailId.getText().toString().isEmpty()) {
               Toast.makeText(getApplicationContext(),"enter email address",Toast.LENGTH_SHORT).show();
            }else {
               if (emailId.getText().toString().trim().matches(emailPattern)) {
                  Toast.makeText(getApplicationContext(),"valid email address",Toast.LENGTH_SHORT).show();
               } else {
                  Toast.makeText(getApplicationContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
               }
            }
                break;

                 */
        }

    }
    //密碼hash
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}

