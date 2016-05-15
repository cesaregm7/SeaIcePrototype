package com.seaice.csar.seaiceprototype;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RecoverySystem;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.internal.http.multipart.MultipartEntity;
import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.maps.model.Marker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by PabloJavier on 24-Apr-16.
 */
@SuppressLint("ValidFragment")
public class DialogReport extends DialogFragment {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imagev;
    private Bitmap photo;
    private Marker marker;
    public AlertDialog builder;
    public Uri tempUri;
    public File finalFile ;
    public HttpFileUpload hfu;
    public boolean enviado = false;

    @SuppressLint("ValidFragment")
    public DialogReport(Marker marker){
        this.marker = marker;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        builder = new AlertDialog.Builder(getActivity()).create();
        builder.setCanceledOnTouchOutside(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_report,null);
        builder.setView(view);
        Button sendBtn = (Button) view.findViewById(R.id.sendBtn);
        Button tomarFoto = (Button) view.findViewById(R.id.takePic);
        final EditText titulo = (EditText) view.findViewById(R.id.title);
        final EditText descripcion = (EditText) view.findViewById(R.id.description);
        this.imagev = (ImageView) view.findViewById(R.id.imagenPeq);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadFile(titulo.getText().toString(),descripcion.getText().toString());
                enviado = true;
                builder.dismiss();
            }
        });

        tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        return builder;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            photo = (Bitmap) data.getExtras().get("data");
            imagev.setImageBitmap(photo);
            tempUri = getImageUri(builder.getContext().getApplicationContext(), photo);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            finalFile = new File(getRealPathFromURI(tempUri));
            Log.d("a",tempUri.toString());
            Log.d("a",finalFile.getAbsolutePath());
        }
    }

    public void UploadFile(String titulo, String descripcion){
        try {
            // Set your file path here
            FileInputStream fstrm = new FileInputStream(finalFile.getAbsolutePath());

            // Set your server page url (and the file title/description)
            hfu = new HttpFileUpload("https://seaice-jayala.rhcloud.com/sendreport", titulo,descripcion);

            hfu.Send_Now(fstrm);

        } catch (FileNotFoundException e) {

        }
    }

    public class HttpFileUpload implements Runnable{
        public URL connectURL;
        public String Title;
        public String Description;
        public FileInputStream fileInputStream = null;

        public HttpFileUpload(String urlString, String vTitle, String vDesc){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try{
                connectURL = new URL(urlString);
                Title= vTitle;
                Description = vDesc;
            }catch(Exception ex){
                Log.i("HttpFileUpload","URL Malformatted");
            }
        }

        void Send_Now(FileInputStream fStream){
            fileInputStream = fStream;
            Sending();
        }

        void Sending(){
            String iFileName = finalFile.getName();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            String Tag="fSnd";
            try
            {
                Log.e(Tag,"Starting Http File Sending to URL");

                // Open a HTTP connection to the URL
                HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();

                // Allow Inputs
                conn.setDoInput(true);
                // Allow Outputs
                conn.setDoOutput(true);
                // Don't use a cached copy.
                conn.setUseCaches(false);
                // Use a post method.
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens+boundary+lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"telefono\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("+50230359588");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"titulo\""+ lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(Title);
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"descripcion\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(Description);
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"lat\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(marker.getPosition().latitude+"");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"lng\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(marker.getPosition().longitude+"");
                dos.writeBytes(lineEnd);


                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"imagen\";filename=\"" + iFileName +"\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e(Tag,"Headers are written");

                // create a buffer of maximum size
                int bytesAvailable = fileInputStream.available();

                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[ ] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0,bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                fileInputStream.close();

                dos.flush();

                Log.e(Tag,"File Sent, Response: "+String.valueOf(conn.getResponseCode()));

                InputStream is = conn.getInputStream();

                // retrieve the response from server
                int ch;

                StringBuffer b =new StringBuffer();
                while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                String s=b.toString();
                Log.i("Response",s);
                dos.close();
            }
            catch (MalformedURLException ex)
            {
                Log.e(Tag, "URL error: " + ex.getMessage(), ex);
            }

            catch (IOException ioe)
            {
                Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
            }
            catch (Exception e){
                Log.e(Tag, "E error: " + e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor =builder.getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }
}
