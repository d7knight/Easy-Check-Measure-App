package com.example.david.easycheckmeasureapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;

public class SendCheckMeasureActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final String TAG = "MAK";//SendCheckMeasureActivity.class.getSimpleName();
    private String checkMeasure;
    private String storageDir = Environment.getExternalStorageDirectory().getPath() + "/easyCheckMeasureApp/";
    private String html_filename;
    private String pdf_filename;
    private String pdf_title;
    private String company;
    private String customer;
    private String author_first_name = "Allan";
    private String author_last_name = "Knight";
    private File pdffile;
    private File htmlfile;
    private int permissionsRequestCode;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode != permissionsRequestCode){
            Log.e(TAG, "Permission result code unexpected.. Weird.");
        }

        int isWriteAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int isReadAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (isReadAllowed == PackageManager.PERMISSION_GRANTED && isWriteAllowed == PackageManager.PERMISSION_GRANTED) {
            File f = new File(storageDir);
            if (!f.exists()) {
                f.mkdir();
            }
            Log.i(TAG, "Writing file to " + storageDir);
            create_html_file();
            create_pdf_file();
        } else {
            Toast.makeText(this, "Please grant permission to access external storage, or else check measure sending will fail", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_check_measure);

        createCheckMeasure();
        WebView wv = (WebView) findViewById(R.id.viewer_html);
        wv.loadData(checkMeasure, "text/html", "utf-8");

        permissionsRequestCode = (int)(Math.random() * 1000.0);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, permissionsRequestCode);
    }

    private void create_pdf_file() {
        pdffile = new File(storageDir + pdf_filename);

        try {
            if (!pdffile.exists()) pdffile.createNewFile();
            FileOutputStream out = new FileOutputStream(pdffile);
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, out);

            document.addAuthor(author_first_name + " " + author_last_name);
            document.addCreator(author_first_name + " " + author_last_name);
            document.addSubject("Check Measure");
            document.addCreationDate();
            document.addTitle(pdf_title);

            //open document
            document.open();


            StringReader in = new StringReader(checkMeasure);

            //get the XMLWorkerHelper Instance
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            //convert to PDF
            worker.parseXHtml(pdfWriter, document, in);

            //close the document
            document.close();
            //close the writer
            pdfWriter.close();

        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());

        } catch (DocumentException e) {
            Log.e(TAG, "Pdf creation failed: " + e.toString());

        } catch (RuntimeWorkerException e) {
            Log.e(TAG, "Pdf creation failed: " + e.toString());
        }
    }

    private void createCheckMeasure() {
        String address;
        String date;
        ArrayList<ArrayList<String>> info = CreateNewCheckMeasureActivity.databaseHandler.getCheckMeasure(CreateNewCheckMeasureActivity.checkMeasureId);
        company = info.get(0).get(0);
        customer = info.get(0).get(1);
        address = info.get(0).get(2);
        date = info.get(0).get(3).toUpperCase();


        html_filename = "CHECK_MEASURE_" + String.valueOf(CreateNewCheckMeasureActivity.checkMeasureId) + "_" + company + "_" + customer + ".html";
        pdf_filename = "CHECK_MEASURE_" + String.valueOf(CreateNewCheckMeasureActivity.checkMeasureId) + "_" + company + "_" + customer + ".pdf";
        Log.i(TAG, "PDF located at: " + pdf_filename);
        pdf_title = company + " check measure for " + customer;

        checkMeasure = "<html>" +
                "" + "<head>"+
                "" + "<style>"+
                ".table{" +
                "background-color:white;" +
                "border:1px:black;"+
                "width:80%;"+
                "border-collapse:collapse;" +
                "}"+
                "p{font-size: 120%;}"+
                "th,tr,td{" +
                "padding:3px" +
                "font-size: 120%;"+
                "}"+
        "th{background-color:orange;color:black;}"+

                "" + "</style>"+
                "" +"</head>"+
                "<body><p>Company: "
                + company + "<br/>" + "Customer: "
                + customer + "<br/>";

        if (address != null && !address.trim().equals("")){
            checkMeasure += "Address: " + address.toUpperCase() + "<br/>";
        }
        checkMeasure += "Date: " + date + "<br/>" + "</p>"
                + "<table border=1px class=table >" +
                "<tr>" +
                "<th >Room</th>" +
                "<th >Width</th>" +
                "<th >Length</th>" +
                "<th >Bracket Type</th>" +
                "<th >Control</th>" +
                "<th >Special Note</th></tr>";
        for (ArrayList<String> row : CreateNewCheckMeasureActivity.databaseHandler.getAllMeasurements(CreateNewCheckMeasureActivity.checkMeasureId)) {
            String room, mount, width, length, control, special_note;
            //String query = "SELECT " + WIDTH + "," + LENGTH + "," + ROOM + "," + CONTROL + "," + MOUNT+ "," + SPECIAL_NOTE + "," + KEY_MEASUREMENT_ID + " " +
            width = row.get(0).toUpperCase();
            length = row.get(1).toUpperCase();
            room = row.get(2).toUpperCase();
            control = row.get(3).toUpperCase();
            mount = row.get(4).toUpperCase();
            special_note = row.get(5).toUpperCase();

            checkMeasure += "<tr><td >" + room + "</td>"
                    + "<td >" + width + "</td>"
                    + "<td >" + length + "</td>"
                    + "<td >" + mount + "</td>"
                    + "<td >" + control + "</td>"
                    + "<td >" + special_note + "</td></tr>";


        }

        checkMeasure += "</table></body></html>";


    }

    private void create_html_file() {
        Log.e(TAG, "---BEGIN HTML--");
        Log.e(TAG, checkMeasure);
        Log.e(TAG, "---END HTML--");
        htmlfile = new File(html_filename);
        try {
            if (!htmlfile.exists()) htmlfile.createNewFile();
            FileOutputStream out = new FileOutputStream(htmlfile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            outputStreamWriter.write(checkMeasure);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());

        }
    }

    public void emailCheckMeasure(View v){
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Check Measure for " + customer);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Hello,\nPlease find your check measure for " + customer
                        + " attached to this email.\n\n" +
                        "Regards,\n" + author_first_name + "\n"
        );
        emailIntent.putExtra(
                android.content.Intent.EXTRA_STREAM,
                Uri.fromFile(pdffile));
        startActivity(emailIntent);
    }


/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_check_measure, menu);
        return true;
    }
*/
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.create_new_cm) {
            Intent i = new Intent(this, CreateNewCheckMeasureActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.send_html) {

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "Check Measure for " + customer);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "Hello,\nPlease find your check measure for " + customer
                            + " attached to this email.\n\n" +
                            "Regards,\n" + author_first_name + "\n"
            );
            emailIntent.putExtra(
                    android.content.Intent.EXTRA_STREAM,
                    Uri.fromFile(htmlfile));
            startActivity(emailIntent);
        } else if (id == R.id.send_pdf) {

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "Check Measure for " + customer);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "Hello,\nPlease find your check measure for " + customer
                            + " attached to this email.\n\n" +
                            "Regards,\n" + author_first_name + "\n"
            );
            emailIntent.putExtra(
                    android.content.Intent.EXTRA_STREAM,
                    Uri.fromFile(pdffile));
            startActivity(emailIntent);

        } else if (id == R.id.view_pdf) {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(pdffile), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } else if (id == R.id.save_to_cloud) {

        } else if (id == R.id.print_pdf) {
            PackageManager pm = getPackageManager();
            if (isAppInstalled(this, "com.pauloslf.cloudprint")) {
                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setPackage("com.pauloslf.cloudprint");

                intent.setDataAndType(Uri.fromFile(pdffile), "text/html");
                startActivity(intent);
            } else {
                Toast.makeText(SendCheckMeasureActivity.this,
                        "Failure printing pdf!",
                        Toast.LENGTH_SHORT).show();
            }
        }


        return super.onOptionsItemSelected(item);
    }
    */

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
