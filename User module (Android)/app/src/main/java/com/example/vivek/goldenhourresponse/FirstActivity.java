package com.example.vivek.goldenhourresponse;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class FirstActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Declare inside class
    TextView tvWelcomeUser;
    Button btnHelp;
    Bitmap bm;
    Double lon,lat;
    GoogleApiClient gac;
    Location loc;
    ImageView ivPreview;
    Uri fileUri;
    byte[] by=new byte[10000];
    Button btupload;
    Bitmap bitmap;
    String imgString,mac,ip;
    String fpath = null;
    // directory name to store captured images is GHR. Image is stored inside internal storage in directory pictures/ghr
    private static final String IMAGE_DIRECTORY_NAME = "GHR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        //Bind inside onCreate()
        tvWelcomeUser = (TextView) findViewById(R.id.tvWelcomeUser);
        btnHelp = (Button) findViewById(R.id.btnHelp);
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        btupload = (Button) findViewById(R.id.uploadButton);
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(LocationServices.API);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        gac = builder.build();

        //Get name from MainActivity
        Intent i = getIntent();
        String name = i.getStringExtra("n");
        tvWelcomeUser.setText("Welcome " + name);

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, 100);
            }
        });

        btupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    WifiInfo info = manager.getConnectionInfo();
                    mac = info.getMacAddress();

                    InsertData(imgString,1,lon,lat,"192.168.1.1",mac);
                }
                catch(Exception e)
                {
                }
            } });

    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir =
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Failed to create directory"
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
            fpath = mediaFile.getPath();
        } else {
            return null;
        }
        return mediaFile;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (gac != null) gac.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gac != null) gac.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                previewCapturedImage();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                    imgString = Base64.encodeToString(getBytesFromBitmap(bitmap), Base64.NO_WRAP);
                } catch (Exception e) {
                }
                by=getBytesFromBitmap(bitmap);


            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void previewCapturedImage() {
        try {
            //Bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            //Downsizing image as it throws OutOfMemory Exception for larger images
            options.inSampleSize = 8;

            bm = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            ivPreview.setImageBitmap(bm);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        loc = LocationServices.FusedLocationApi.getLastLocation(gac);
        if (loc != null) {
             lat = loc.getLatitude();
             lon = loc.getLongitude();
            Toast.makeText(this, "Current location GPS co-ordinates : " + lat + " , " + lon, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    //To display AlertDialog
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Do you really want to exit GHR?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.create();
        builder.show();
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }


    //inster data into http client
    public void InsertData(final String image,final int userid,final Double longi,final Double lati,final String ipAdd,final String macAdd){

        @SuppressLint("StaticFieldLeak")
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

               // String ServerURL = "http://192.168.43.27/extract.php?longi="+lon.toString()+"&&lat="+lat.toString();
                String ServerURL1 = "http://192.168.43.139";
                String imageHolder = image ;
               String longiHolder = longi.toString() ;
                String latHolder = lati.toString() ;
                String uidHolder = ""+userid ;
                String ipHolder = ipAdd ;
                String macHolder = macAdd ;
               ArrayList<NameValuePair> mylist=new ArrayList<NameValuePair>();
               mylist.add(new BasicNameValuePair("img", imageHolder));
               mylist.add(new BasicNameValuePair("uid", uidHolder));
                mylist.add(new BasicNameValuePair("longi", longiHolder));
                mylist.add(new BasicNameValuePair("lat", latHolder));
               mylist.add(new BasicNameValuePair("ip", ipHolder));
                mylist.add(new BasicNameValuePair("mac", macHolder));
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                 //   HttpGet httpGet = new HttpGet(ServerURL);
                    HttpPost httpPost = new HttpPost(ServerURL1);

                   httpPost.setEntity(new UrlEncodedFormEntity(mylist));
                   HttpResponse httpResponse =httpClient.execute(httpPost);
                   //httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();



                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return "Data Inserted Successfully";
            }

            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);

                Toast.makeText(FirstActivity.this, "Data Submit Successfully", Toast.LENGTH_LONG).show();

            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute(image);
    }

}