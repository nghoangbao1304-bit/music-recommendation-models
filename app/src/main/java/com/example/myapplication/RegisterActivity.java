package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.AuthRequest;
import com.example.myapplication.model.AuthResponse;
import com.example.myapplication.model.SupabaseApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://evasrqslsnyuegstaqgg.supabase.co/";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_u829setPIGPBm7F-4EI5XA_MoWyIkoR";

    private EditText edtEmailReg, edtPasswordReg;
    private Button btnRegister;
    private TextView tvBackLogin;
    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmailReg = findViewById(R.id.edtEmailReg);
        edtPasswordReg = findViewById(R.id.edtPasswordReg);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackLogin = findViewById(R.id.tvBackLogin);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SupabaseApi.class);

        btnRegister.setOnClickListener(v -> registerUser());
        
        tvBackLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = edtEmailReg.getText().toString().trim();
        String password = edtPasswordReg.getText().toString().trim();

        if (email.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Email không hợp lệ hoặc Mật khẩu quá ngắn!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("ĐANG TẠO TÀI KHOẢN...");
        
        // KIỂM TRA ĐƯỜNG LINK
        Log.d("RegisterActivity", "Đang gọi URL: " + SUPABASE_URL + "auth/v1/signup");

        AuthRequest request = new AuthRequest(email, password);
        api.signUp(SUPABASE_ANON_KEY, request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("TẠO TÀI KHOẢN");

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Hãy Đăng nhập.", Toast.LENGTH_LONG).show();
                    finish(); 
                } else {
                    try {
                        String errorMsg = response.errorBody().string();
                        Toast.makeText(RegisterActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại. Email đã tồn tại?", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("TẠO TÀI KHOẢN");
                Log.e("RegisterActivity", "Lỗi mạng chi tiết: ", t);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}