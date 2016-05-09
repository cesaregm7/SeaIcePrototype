package com.seaice.csar.seaiceprototype;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.android.internal.http.multipart.MultipartEntity;
import com.google.android.gms.appdatasearch.GetRecentContextCall;

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
public class DialogReport extends DialogFragment {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imagev;
    private Bitmap photo;
    AlertDialog builder;
    Uri tempUri;

    // CALL THIS METHOD TO GET THE ACTUAL PATH
    File finalFile ;

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
        this.imagev = (ImageView) view.findViewById(R.id.imagenPeq);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PUTO", "PUTO");
               /*uploadImage();*/
                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                UploadFile();
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

    public void UploadFile(){
        try {
            // Set your file path here
            FileInputStream fstrm = new FileInputStream(finalFile.getAbsolutePath());

            // Set your server page url (and the file title/description)
            HttpFileUpload hfu = new HttpFileUpload("https://seaice-jayala.rhcloud.com/sendreport", "Pruba1","my file description");

            hfu.Send_Now(fstrm);

        } catch (FileNotFoundException e) {
            // Error: File not found
            Log.d("A","les cuesta va");
        }
    }



    public class HttpFileUpload implements Runnable{
        URL connectURL;
        String responseString;
        String Title;
        String Description;
        byte[ ] dataToServer;
        FileInputStream fileInputStream = null;

        HttpFileUpload(String urlString, String vTitle, String vDesc){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try{
                connectURL = new URL(urlString);
                Title= vTitle;
                Description = vDesc;
                Log.d("A","Enviando arv");
            }catch(Exception ex){
                Log.i("HttpFileUpload","URL Malformatted");
            }
        }

        void Send_Now(FileInputStream fStream){
            fileInputStream = fStream;
            Sending();
        }

        void Sending(){
            String iFileName = finalFile.getAbsolutePath();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            String Tag="fSnd";
            try
            {
                Log.e(Tag,"Starting Http File Sending to URL");

                // Open a HTTP connection to the URL
                HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();
                Log.e(Tag,"Se conecto");

                // Allow Inputs
                conn.setDoInput(true);
                Log.e(Tag, "Se conecto2");
                // Allow Outputs
                conn.setDoOutput(true);
                Log.e(Tag, "Se conecto3");
                // Don't use a cached copy.
                conn.setUseCaches(false);
                Log.e(Tag, "Se conecto4");
                // Use a post method.
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");
                Log.e(Tag, "Se conecto5");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //multipart/form-data
                Log.e(Tag, "Se conecto6");
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                Log.e(Tag, "Bajo DaraOutputStream");

                dos.writeBytes(lineEnd+lineEnd);
                dos.writeBytes("telefono=+50230359588");
                dos.writeBytes(lineEnd);
                //dos.writeBytes(Title);
                //dos.writeBytes(lineEnd);
                //dos.writeBytes(twoHyphens + boundary + lineEnd);
                Log.e(Tag, "Bajo titulo");
/*
                dos.writeBytes("Content-Disposition: form-data; name=\"descripcion\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(Description);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                Log.e(Tag, "bajo description");

                dos.writeBytes("Content-Disposition: form-data; name=\"lat\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("3.00");
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"lng\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("4.00");
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"telefono\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("50230359588");
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
*/
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
/*
   public static String uploadImage(Bitmap bitmap, String urlString) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            if(filename.toLowerCase().endsWith("jpg") || filename.toLowerCase().endsWith("jpeg"))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            if(filename.toLowerCase().endsWith("png"))
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, bos);
            ContentBody contentPart = new ByteArrayBody(bos.toByteArray(), filename);
            ContentBody body1 = new StringBody("something");
            ContentBody body2 = new StringBody("something");
            org.apache.http.entity.mime.MultipartEntity reqEntity = new org.apache.http.entity.mime.MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("image", contentPart);
            reqEntity.addPart("sample1", body1);
            reqEntity.addPart("sample2", body2);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
            conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());
            OutputStream os = conn.getOutputStream();
            reqEntity.writeTo(conn.getOutputStream());
            os.close();
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d("UPLOAD", "HTTP 200 OK.");
                return readStream(conn.getInputStream());
                //This return returns the response from the upload.
            } else {
                Log.d("UPLOAD", "HTTP "+conn.getResponseCode()+" "+conn.getResponseMessage()+".");
                String stream =  readStream(conn.getInputStream());
                //Log.d("UPLOAD", "Response: "+stream);
                return stream;
            }
        } catch (Exception e) {
            Log.d("UPLOAD_ERROR", "Multipart POST Error: " + e + "(" + urlString + ")");
        }
        return null;
    }*/
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
