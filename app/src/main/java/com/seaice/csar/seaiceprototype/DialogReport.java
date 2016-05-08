package com.seaice.csar.seaiceprototype;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by PabloJavier on 24-Apr-16.
 */
public class DialogReport extends DialogFragment {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imagev;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_report, null));

        View view = inflater.inflate(R.layout.dialog_report,null);
        Button sendBtn = (Button) view.findViewById(R.id.sendBtn);
        Button tomarFoto = (Button) view.findViewById(R.id.takePic);
        this.imagev = (ImageView) view.findViewById(R.id.imagenPeq);

                sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                //uploadImage(null,null);
                System.out.println("Hola");
            }
        });

        tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        return builder.create();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imagev.setImageBitmap(photo);
        }
    }


   /* public static String uploadImage(Bitmap bitmap, String urlString) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap = Local.resize(bitmap, 512, 512);
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
}
