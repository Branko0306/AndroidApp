package utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.example.jelav.contentdelivery.BuildConfig;
import com.example.jelav.contentdelivery.MainActivity;
import com.example.jelav.contentdelivery.MapsActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import models.Sadrzaj;
import network.NetworkUtils;
import network.QuerySadrzaj;

public class AkcijeUtils {

    private Context mContext;

    public  AkcijeUtils(Context context){
        mContext = context;
    }

    public void OtvoriUrl(Sadrzaj sadrzaj, String instanceID){
        QuerySadrzaj filteri = new QuerySadrzaj();
        filteri.sadrzajID = sadrzaj.PK;
        filteri.instanceID = instanceID;

        String url = NetworkUtils.buildUrl(filteri).toString();

        if(sadrzaj.PDF != ""){
            (new DownloadFile()).execute(url, sadrzaj.PDF, ((Activity)mContext).getFilesDir().toString());
        }else{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ((Activity)mContext).startActivity(browserIntent);
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = strings[2];
            File folder = new File(extStorageDirectory);
            folder.mkdir();

            File pdfFile = new File(folder, fileName);

            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            new FileDownloader().downloadFile(fileUrl, pdfFile);
            return pdfFile.getPath();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
        }

        @Override
        protected void onPostExecute(String path) {

            hidepDialog();
            viewPDF(path);
        }
    }

    public class FileDownloader {
        private static final int  MEGABYTE = 1024 * 1024;

        public void downloadFile(String fileUrl, File directory){
            try {

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(directory);
                int totalSize = urlConnection.getContentLength();

                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                while((bufferLength = inputStream.read(buffer))>0 ){
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void viewPDF(String file){
        File pdfFile = new File(file);
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);

        Uri pdfURI = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", pdfFile);

        pdfIntent.setDataAndType(pdfURI, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(pdfIntent, "Open File");
        try{
            ((Activity)mContext).startActivity(intent);
        }catch(ActivityNotFoundException e){
        }
    }

    public void OtvoriMapu(Sadrzaj sadrzaj){

        Uri uri2 = Uri.parse(String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=w", sadrzaj.LokacijaLatitude, sadrzaj.LokacijaLongitude));
        showMap(uri2);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            ((Activity)mContext).startActivity(intent);
        }
    }

    private void showpDialog() {
        ((ShowDialog)mContext).ShowDialog();
    }

    private void hidepDialog() {
        ((ShowDialog)mContext).HideDialog();
    }
}
