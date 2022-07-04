package com.blackmessage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackmessage.Model.Background;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.blackmessage.Adapter.MessageAdapter;
import com.blackmessage.Fragments.APIService;
import com.blackmessage.Model.Chat;
import com.blackmessage.Model.User;
import com.blackmessage.Notifications.Client;
import com.blackmessage.Notifications.Data;
import com.blackmessage.Notifications.MyResponse;
import com.blackmessage.Notifications.Sender;
import com.blackmessage.Notifications.Token;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    Intent intent;

    ValueEventListener seenListener;

    String userid;
    String phonenumber;

    APIService apiService;
    int mDefaultColor;
    RelativeLayout relativeLayout;
    String mFilePath;
    private static int RESULT_LOAD_IMG = 1;
    String imgpath, storedpath;
    SharedPreferences sp;
    ImageView myImage;
    int colorr;
    boolean notify = false;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        myImage = findViewById(R.id.arkaplann);

        /*
        sp = getSharedPreferences("setback", MODE_PRIVATE);
        if (sp.contains("imagepath") && sp.getString("imagepath", "") != null) {
            storedpath = sp.getString("imagepath", "");
            myImage.setImageBitmap(BitmapFactory.decodeFile(storedpath));
        } else if (sp.contains("color") && sp.getInt("color", 0) != 1) {
            colorr = sp.getInt("color", 0);
            myImage.setImageResource(0);
            myImage.setBackgroundColor(colorr);
        } else if (sp.contains("image")) {
            myImage.setImageResource(R.drawable.arkaplan);
        } else if (!sp.contains("color") && !sp.contains("imagepath")) {
            myImage.setImageResource(R.drawable.arkaplan);
        }

         */
        mDefaultColor = ContextCompat.getColor(MessageActivity.this, R.color.colorPrimary);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        phonenumber = intent.getStringExtra("phonenumber");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        relativeLayout = findViewById(R.id.layout);
        checkMyPermission1();
        //relativeLayout.setBackgroundResource(R.drawable.arkaplan);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "Boş mesaj gönderemezsiniz", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });


        DatabaseReference reference1 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child(userid);

        reference1.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Background background = snapshot.getValue(Background.class);
                if(background == null){
                    DatabaseReference reference2 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child(userid);
                    HashMap<String, Object> hashMap3 = new HashMap<>();
                    hashMap3.put("imagepath", "default");
                    hashMap3.put("color", null);
                    hashMap3.put("userid", userid);
                    hashMap3.put("myid", fuser.getUid());
                    reference2.updateChildren(hashMap3);
                } else {
                 if (background.getMyid().equals(fuser.getUid()) && background.getUserid().equals(userid)){

                    if (background.getColor() != null){
                    myImage.setImageResource(0);
                    myImage.setBackgroundColor(Math.toIntExact(background.getColor()));
                    } else{
                        if (!background.getImagePath().equals("default")){
                    myImage.setImageBitmap(BitmapFactory.decodeFile(background.getImagePath()));
                        } else {
                            myImage.setImageResource(R.drawable.arkaplan);
                        }
                    }
                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.user1);
                } else {
                    //and this
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                readMesagges(fuser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance(MainActivity.url).getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);


        final DatabaseReference chatRef = FirebaseDatabase.getInstance(MainActivity.url).getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance(MainActivity.url).getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());

        final String msg = message;

        reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotifiaction(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance(MainActivity.url).getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.whiteandblack, username + ": " + message, "Yeni Mesaj",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void readMesagges(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Users").child(fuser.getUid());
         DatabaseReference reference1 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child(fuser
         .getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.call:
                Uri uri = Uri.parse("tel:" + phonenumber);
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
                break;
            case R.id.beyaz:
                int colors = getColor(R.color.acik);
                myImage.setImageResource(0);
                myImage.setBackgroundColor(getColor(R.color.acik));
                DatabaseReference reference3 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child(userid);
                HashMap<String, Object> hashMap3 = new HashMap<>();
                hashMap3.put("imagepath", null);
                hashMap3.put("myid",fuser.getUid());
                hashMap3.put("userid", userid);
                hashMap3.put("color", colors);
                reference3.updateChildren(hashMap3);
                /*
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("imagepath", null);
                edit.putInt("color", colors);
                edit.putString("id", userid);
                edit.commit();
                 */
                break;
            case R.id.koyu:
                //relativeLayout.setBackgroundColor(getColor(R.color.colorPrimary));
                int colors1 = getColor(R.color.colorPrimary);
                myImage.setImageResource(0);
                myImage.setBackgroundColor(getColor(R.color.colorPrimary));
                DatabaseReference reference1 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child(userid);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imagepath", null);
                hashMap.put("myid",fuser.getUid());
                hashMap.put("userid", userid);
                hashMap.put("color", colors1);
                reference1.updateChildren(hashMap);
                break;
            case R.id.galeri:
                loadImagefromGallery(myImage);
                break;
            case R.id.ozel:
                myImage.setImageResource(0);
                openColorPicker();
                break;
            case R.id.varsayilan:

                myImage.setImageResource(R.drawable.arkaplan);
                DatabaseReference reference2 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child(userid);
                HashMap<String, Object> hashMap1 = new HashMap<>();
                hashMap1.put("imagepath", "default");
                hashMap1.put("myid",fuser.getUid());
                hashMap1.put("userid", userid);
                hashMap1.put("color", null);
                reference2.updateChildren(hashMap1);
                /*
                myImage.setImageResource(R.drawable.arkaplan);
                SharedPreferences.Editor edit2 = sp.edit();
                edit2.putInt(userid +"color", 1);
                edit2.putString(userid +"imagepath", null);
                edit2.putInt(userid +"image", R.drawable.arkaplan);
                edit2.commit();

                 */
                break;

        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    public void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor = color;
                myImage.setBackgroundColor(mDefaultColor);
                int colors = color;
                DatabaseReference reference2 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child((userid));
                HashMap<String, Object> hashMap1 = new HashMap<>();
                hashMap1.put("imagepath", null);
                hashMap1.put("myid",fuser.getUid());
                hashMap1.put("userid", userid);
                hashMap1.put("color", colors);
                reference2.updateChildren(hashMap1);
                /*SharedPreferences.Editor edit = sp.edit();
                edit.putString(userid +"imagepath", null);
                edit.putInt(userid +"color", colors);
                edit.commit();

                 */

            }
        });
        colorPicker.show();
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.MediaColumns.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgpath = cursor.getString(columnIndex);
                Log.d("path", imgpath);
                cursor.close();

                DatabaseReference reference2 = FirebaseDatabase.getInstance(MainActivity.url).getReference("Background").child(userid);
                HashMap<String, Object> hashMap1 = new HashMap<>();
                hashMap1.put("myid",fuser.getUid());
                hashMap1.put("imagepath", imgpath);
                hashMap1.put("userid", userid);
                hashMap1.put("color", null);
                reference2.updateChildren(hashMap1);
                /*
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("imagepath", imgpath);
                edit.commit();

                 */


                Bitmap myBitmap = BitmapFactory.decodeFile(imgpath);

                myImage.setImageBitmap(myBitmap);
            } else {
            }
        } catch (Exception e) {
            Toast.makeText(this, "Brişeyler ters gitti", Toast.LENGTH_LONG)
                    .show();
        }
    }


    private void checkMyPermission1() {
        Dexter.withContext(this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                //Intent intent = new Intent();
                //intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //Uri uri = Uri.fromParts("package", getPackageName(), "");
                //intent.setData(uri);
                //startActivity(intent);
                if (ActivityCompat.shouldShowRequestPermissionRationale(MessageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.cancelPermissionRequest();
            }
        }).check();
    }

}
