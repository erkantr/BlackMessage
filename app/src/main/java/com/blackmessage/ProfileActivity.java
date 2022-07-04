package com.blackmessage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackmessage.Model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener {

    long minTimeMs = 10000;
    float minDistanceM = 1000;
    DatabaseReference reference;
    FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    GoogleMap mGoogleMap;
    FusedLocationProviderClient client;
    SupportMapFragment mapFragment;
    Location location;
    double lat;
    double lng;
    SwipeRefreshLayout swipeRefreshLayout;

    Button guncelle;

    ImageView profile_image;

    TextView fullname, email, tw;

    TextInputEditText kullanici_adi_edit, sifre_edit2, info, telefon_edit, sifre_edit, sifre_edit1;

    int durum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        guncelle = findViewById(R.id.guncelle);
        profile_image = findViewById(R.id.profile_image);
        fullname = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        kullanici_adi_edit = findViewById(R.id.kullanici_adi_edit);
        info = findViewById(R.id.info);
        telefon_edit = findViewById(R.id.telefon_edit);
        sifre_edit = findViewById(R.id.sifre_edit);
        sifre_edit1 = findViewById(R.id.sifre_edit1);
        sifre_edit2 = findViewById(R.id.sifre_edit2);
        tw = findViewById(R.id.konumbilgisi);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client = LocationServices.getFusedLocationProviderClient(this);
            //getLocation();
        } else {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
        //getCurrentLocation();
        try {
            //getLocation1();
            getLocation();
            Geocoder geo = new Geocoder(ProfileActivity.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(lat, lng, 1);
            //if (addresses.isEmpty()) {
            //  Toast.makeText(this, "33Konum bilgisi alınamadı", Toast.LENGTH_SHORT).show();
            //} else {
            // if (addresses.size() > 0) {
            System.out.println(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubLocality() + " Mahallesi, " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());

            String newtext = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
            tw.setText(newtext);
            //} else {
            //  Toast.makeText(this, "konum bilgisi alınamadı33", Toast.LENGTH_SHORT).show();
            //}
            //}
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        // client = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);


        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (ProfileActivity.class == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                if (user.getId().equals(fuser.getUid())) {
                    fullname.setText(user.getUsername());
                    email.setText(fuser.getEmail());
                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.drawable.ic_baseline_account_circle_44);
                    } else {
                        if (MessageActivity.isValidContextForGlide(ProfileActivity.this)) {
                            Glide.with(ProfileActivity.this).load(user.getImageURL()).into(profile_image);
                        }
                    }
                    kullanici_adi_edit.setText(user.getUsername());
                    info.setText(user.getHakkimda());
                    telefon_edit.setText(user.getNumber());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(ProfileActivity.this);
            }
        });

        ImageView logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        /*
        ImageView refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

         */

        /*
        ImageView genislet = findViewById(R.id.genislet);

        genislet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
                //startActivity(intent);
                getLocation();
            }
        });

         */

        guncelle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_name = kullanici_adi_edit.getText().toString();
                String txt_sifre2 = sifre_edit2.getText().toString();
                String txt_info = info.getText().toString();
                String txt_telefon = telefon_edit.getText().toString();
                String txt_sifre = sifre_edit.getText().toString();
                String txt_sifre1 = sifre_edit1.getText().toString();
                durum = 1;

                if (TextUtils.isEmpty(txt_name) || TextUtils.isEmpty(txt_info) || TextUtils.isEmpty(txt_telefon)) {
                    Toast.makeText(ProfileActivity.this, "Tüm alanlar zorunludur!", Toast.LENGTH_SHORT).show();
                } else {
                    reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Users").child(fuser.getUid());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String newPassword = "";

                    if (TextUtils.isEmpty(txt_sifre1) && TextUtils.isEmpty(txt_sifre) && TextUtils.isEmpty(txt_sifre2)) {
                        System.out.println("Şifre yenilenemedi");
                    } else {
                        if (TextUtils.isEmpty(txt_sifre1) || TextUtils.isEmpty(txt_sifre) || TextUtils.isEmpty(txt_sifre2)) {
                            Toast.makeText(ProfileActivity.this, "Şifrenizi değiştirebilmeniz için tüm alanları doldurmanız gerekiyor", Toast.LENGTH_SHORT).show();
                            durum = 0;
                        } else {
                            if (txt_sifre2.equals(txt_sifre1)) {
                                newPassword = txt_sifre1;
                                if (user != null) {
                                    AuthCredential credential = EmailAuthProvider
                                            .getCredential(user.getEmail(), txt_sifre);

                                    String finalNewPassword = newPassword;
                                    user.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        System.out.println("Kullanıcı yeniden giriş yaptı");
                                                        if (!finalNewPassword.equals("") && finalNewPassword.length() >= 6 && durum != 3) {
                                                            user.updatePassword(finalNewPassword)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                System.out.println("Şifre yenileme başarılı");
                                                                            } else {
                                                                                Toast.makeText(ProfileActivity.this, "Bu şifre ile kayıt olamazsınız", Toast.LENGTH_SHORT).show();
                                                                                durum = 0;
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            durum = 0;
                                                            Toast.makeText(ProfileActivity.this, "şifre en az 6 karakterden oluşmalıdır", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        durum = 0;
                                                        Toast.makeText(ProfileActivity.this, "Lütfen şifrenizi doğru girin", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Çok fazla güncelleme yaptınız lütfen çıkış yapıp tekrar deneyin", Toast.LENGTH_SHORT).show();
                                    durum = 3;
                                }
                            } else {
                                durum = 0;
                                Toast.makeText(ProfileActivity.this, "Şifreler birbirleri ile uyuşmuyor!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("hakkimda", txt_info);
                    map.put("number", txt_telefon);
                    map.put("username", txt_name);
                    reference.updateChildren(map);
                    if (durum != 0 || durum != 3) {
                        Toast.makeText(ProfileActivity.this, "Profil bilgileri güncellendi", Toast.LENGTH_SHORT).show();
                    }
                    sifre_edit.setText("");
                    sifre_edit1.setText("");
                    sifre_edit2.setText("");
                }
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance(MainActivity.url).getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", "" + mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ProfileActivity.this, "Upload in preogress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //LatLng latLng = new LatLng(-34, 151);
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        //googleMap.addMarker(new MarkerOptions().position(latLng));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Buradayım!");
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    googleMap.addMarker(markerOptions);
                } else {
                   // Toast.makeText(ProfileActivity.this, "Konum bilgisi alınamadı", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        /*
        Task<Location> ids = client.getLastLocation();
        ids.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Geocoder geo = new Geocoder(ProfileActivity.this.getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    if (location!= null){
                        addresses = geo.getFromLocation(
                                location.getLatitude(),location.getLongitude(),1);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Konum bilgisi kapalı", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (location != null){
                    if (addresses.isEmpty()) {
                    } else {
                        if (addresses.size() > 0) {
                            System.out.println(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubLocality() + " Mahallesi, " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());

                            String newtext = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubLocality() + " Mahallesi, " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                            TextView tw = findViewById(R.id.konumbilgisi);
                            tw.setText(newtext);
                        }
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Konum bilgisi kapalı", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    Geocoder geocoder = new Geocoder(ProfileActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        String newtext = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                        TextView tw = findViewById(R.id.konumbilgisi);
                        tw.setText(newtext);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    tw.setText("Konum bilgisi alınamadı");
                    Toast.makeText(ProfileActivity.this, "Konum bilgisi alınamadı", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    //Toast.makeText(ProfileActivity.this, "Konum bilgisi kapalı", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private final com.google.android.gms.location.LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
        }
    };

    @SuppressLint("MissingPermission")
    public Location getLocation1() {
        try {
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //Toast.makeText(this, "konum bilgisi alınamadı", Toast.LENGTH_SHORT).show();
            } else {
                if (isGPSEnabled) {
                    if (location == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, (LocationListener) this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {
                            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                        }
                    }
                } else {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeMs, minDistanceM, (LocationListener) this);
                    Log.d("Network", "Network");
                    if (mLocationManager != null) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onRefresh() {
        getLocation();
        swipeRefreshLayout.setRefreshing(false);
    }
}