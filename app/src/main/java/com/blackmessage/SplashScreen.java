package com.blackmessage;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    Thread splashThread;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashThread = new Thread() {

            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(3000);
                    }
                } catch (InterruptedException ex) {

                } finally {

                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (firebaseUser != null) {
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                }

            }
        };
        splashThread.start();

    }
}