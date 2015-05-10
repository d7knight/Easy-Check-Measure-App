package com.example.david.easycheckmeasureapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;

public class SendCheckMeasureActivity extends Activity {
    private final String TAG = SendCheckMeasureActivity.class.getSimpleName();
    private String checkMeasure;
    private String storageDir = Environment.getExternalStorageDirectory().getPath()+"/easyCheckMeasureApp/";
    private String html_filename;
    private String pdf_filename;
    private String pdf_title;
    private String company;
    private String customer;
    private String author_first_name="Allan";
    private String author_last_name="Knight";
    private File pdffile;
    private File htmlfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_check_measure);
        createCheckMeasure();
        WebView wv = (WebView) findViewById(R.id.viewer_html);
        wv.loadData(checkMeasure, "text/html", "utf-8");
        File f = new File(storageDir);
            if (!f.exists()){
                f.mkdir();}
        create_html_file();
        create_pdf_file();



    }

    private void create_pdf_file() {
        pdffile = new File(storageDir+pdf_filename);

        try {
            if (!pdffile.exists()) pdffile.createNewFile();
            FileOutputStream out = new FileOutputStream(pdffile);
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, out);

            document.addAuthor(author_first_name+" "+author_last_name);
            document.addCreator(author_first_name+" "+author_last_name);
            document.addSubject("Check Measure");
            document.addCreationDate();
            document.addTitle(pdf_title);

            //open document
            document.open();



            StringReader in = new StringReader(checkMeasure);

            //get the XMLWorkerHelper Instance
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            //convert to PDF
            worker.parseXHtml(pdfWriter, document,in);

            //close the document
            document.close();
            //close the writer
            pdfWriter.close();

        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());

        }
        catch (DocumentException e) {
            Log.e(TAG, "Pdf creation failed: " + e.toString());

        }
        catch(RuntimeWorkerException e){
            Log.e(TAG, "Pdf creation failed: " + e.toString());
        }
    }

    private void createCheckMeasure(){
        String address;
        String date;
        ArrayList<ArrayList<String>> info= CreateNewCheckMeasureActivity.db.getCheckMeasure(CreateNewCheckMeasureActivity.cm_id);
     company =info.get(0).get(0).toUpperCase();
     customer =info.get(0).get(1).toUpperCase();
     address=info.get(0).get(2).toUpperCase();
     date=info.get(0).get(3).toUpperCase();


     html_filename="CHECK_MEASURE_"+String.valueOf(CreateNewCheckMeasureActivity.cm_id)+"_"+ company +"_"+ customer +".html";
     pdf_filename= "CHECK_MEASURE_"+String.valueOf(CreateNewCheckMeasureActivity.cm_id)+"_"+ company +"_"+ customer +".pdf";
     pdf_title= company + " check measure for " + customer;

     checkMeasure ="<html><body><p>Company: "
                + company + "</br>" + "Customer: "
                + customer + "</br>" + "Address: " + address + "</br>"
                + "Date: "+ date + "</br>"  + "</p>"
                +"<table border=1 style=background-color:white;"
                +"border:1px:black;width:80%;border-collapse:collapse;>"+
                "<tr style=background-color:orange;color:black;>"+
             "<th style=padding:3px;>Room</th>"+
             "<th style=padding:3px;>Bracket Type</th>" +
             "<th style=padding:3px;>Width</th>"+
             "<th style=padding:3px;>Length</th>"+
             "<th style=padding:3px;>Control</th>" +
             "<th style=padding:3px;>Special Note</th></tr>";
     for (ArrayList<String> row: CreateNewCheckMeasureActivity.db.getAllMeasurements(CreateNewCheckMeasureActivity.cm_id)){
         String room,mount,width,length,control,special_note;

         room=row.get(0).toUpperCase();
         mount=row.get(1).toUpperCase();
         width=row.get(2).toUpperCase();
         length=row.get(3).toUpperCase();
         control=row.get(4).toUpperCase();
         special_note=row.get(5).toUpperCase();

         checkMeasure+="<tr><td style=padding:3px;>"+room+"</td>"
         +"<td style=padding:3px;>"+mount+"</td>"
         +"<td style=padding:3px;>"+width+"</td>"
         +"<td style=padding:3px;>"+length+"</td>"
         +"<td style=padding:3px;>"+control+"</td>"
         +"<td style=padding:3px;>"+special_note+"</td></tr>";


     }

        checkMeasure+="</table></body></html>";



    }

    private void create_html_file() {
        htmlfile = new File(html_filename);
        try {
            if (!htmlfile.exists()) htmlfile.createNewFile();
            FileOutputStream out = new FileOutputStream(htmlfile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            outputStreamWriter.write(checkMeasure);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            out.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_check_measure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.create_new_cm){
            Intent i = new Intent(this,CreateNewCheckMeasureActivity.class);
            startActivity(i);
            finish();
        }
        else if(id == R.id.send_html){

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "Check Measure for " + customer);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "Hello,\nPlease find your check measure for "+customer
                            + " attached to this email.\n\n" +
                            "Regards,\n" + author_first_name+"\n"
            );
            emailIntent.putExtra(
                    android.content.Intent.EXTRA_STREAM,
                    Uri.fromFile(htmlfile));
            startActivity(emailIntent);
        }
        else if(id == R.id.send_pdf){

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "Check Measure for " + customer);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "Hello,\nPlease find your check measure for "+customer
                            + " attached to this email.\n\n" +
                            "Regards,\n" + author_first_name+"\n"
            );
            emailIntent.putExtra(
                    android.content.Intent.EXTRA_STREAM,
                    Uri.fromFile(pdffile));
            startActivity(emailIntent);

        }
        else if(id == R.id.view_pdf){

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(pdffile), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
        else if(id == R.id.save_to_cloud){

        }
        else if(id == R.id.print_pdf){
            PackageManager pm = getPackageManager();
            if(isAppInstalled(this, "com.pauloslf.cloudprint")) {
                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setPackage("com.pauloslf.cloudprint");

                intent.setDataAndType(Uri.fromFile(pdffile), "text/html");
                startActivity(intent);
            }
            else{
                Toast.makeText(SendCheckMeasureActivity.this,
                        "Failure printing pdf!",
                        Toast.LENGTH_SHORT).show();
            }
        }


        return super.onOptionsItemSelected(item);
    }
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
