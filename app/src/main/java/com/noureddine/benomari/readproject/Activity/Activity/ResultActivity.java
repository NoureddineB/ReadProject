package com.noureddine.benomari.readproject.Activity.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.noureddine.benomari.readproject.R;


import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.noureddine.benomari.readproject.R.*;

public class ResultActivity extends AppCompatActivity {

    private Bitmap bmp;
    private EditText editText;
    private InterstitialAd mInterstitialAd2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_result);
        configureToolbar();
        configureInterstitialAd2();
        getExtra();
        readImageFromGallery(bmp);
        editText = findViewById(id.edit_text);
        editText.setMovementMethod(new ScrollingMovementMethod());
    }

    //CONFIGURE ADS
    private void configureInterstitialAd2() {
        mInterstitialAd2 = new InterstitialAd(this);
        mInterstitialAd2.setAdUnitId("ca-app-pub-2681791564432660/2314401482");
        mInterstitialAd2.loadAd(new AdRequest.Builder().build());
        mInterstitialAd2.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd2.loadAd(new AdRequest.Builder().build());
            }


        });


    }
    //SHOW AD
    private void showInterstitialAd2() {
        if (mInterstitialAd2.isLoaded()) {
            mInterstitialAd2.show();
        }
    }


    //CONFIGURE TOOLBAR PART
    public void configureToolbar() {
        Toolbar toolbar = findViewById(id.toolbar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_toolbar_menu, menu);
        return true;
    }
    //Option after reading the text : share or make a PDF
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.action_share:
                showInterstitialAd2();
                shareResult(editText);
                return true;
            case id.action_pdf:
                if (editText.getText().toString().equals("")) {
                    showInterstitialAd2();
                    openDialog();
                } else {
                    showInterstitialAd2();
                    pdfDialog();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //GET IMAGE FROM PREVIOUS ACTIVITY
    private void getExtra() {
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //FIREBASE PART
    private void readImageFromGallery(final Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        extractText(image);
    }

    private void extractText(FirebaseVisionImage image) {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                detectText(firebaseVisionText);

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });


    }
    //Detect the text in the picture
    private void detectText(FirebaseVisionText text) {
        String resultText = (text.getText());
        for (FirebaseVisionText.TextBlock block : text.getTextBlocks()) {
            String blockText = block.getText();
            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line : block.getLines()) {
                String lineText = line.getText();
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element : line.getElements()) {
                    String elementText = element.getText();
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }
        if (resultText.equals("")) {
            editText.setText(resultText);
            openDialog();
        } else {
            editText.setText(resultText);
        }


    }

    //TOOLBAR ITEM ACTION
    private void shareResult(EditText editText) {
        String s = editText.getText().toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);
        startActivity(Intent.createChooser(sharingIntent, "Share text via"));
    }
    //Open dialog when there is no text in the picture
    public void openDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();


        // Set Custom Title
        TextView title = new TextView(this);
        // Title Properties
        title.setText(string.DialogTryAgain);
        title.setPadding(40, 40, 10, 10);   // Set Position

        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        alertDialog.setCustomTitle(title);
        TextView msg = new TextView(this);

        msg.setText(string.DialogNoTextFound);
        msg.setPadding(40, 60, 0, 0);
        msg.setTextColor(Color.BLACK);
        msg.setTextSize(16);
        alertDialog.setView(msg);

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        new Dialog(getApplicationContext());
        alertDialog.show();


        final Button okBT = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams neutralBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        okBT.setTextColor(Color.BLUE);
        okBT.setLayoutParams(neutralBtnLP);
    }

    //CREATE PDF PART
    private void createPdf(String maintext, String pdftitle) {
        //StaticLayout
        TextPaint textPaint = new TextPaint();
        StaticLayout staticLayout = StaticLayout.Builder.obtain(maintext, 0, maintext.length(), textPaint, 300).build();
        //Pdf Document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();

        // Page 1
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        textPaint.setColor(Color.BLACK);
        textPaint.setLinearText(true);
        staticLayout.draw(canvas);
        document.finishPage(page);

        //Write the document content
        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Read and Scan [OCR]/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetPdf = directory_path + pdftitle + ".pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
        showPDF(targetPdf);

    }

    private void showPDF(String targetpdf) {
        File file = new File(targetpdf);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }


    //EDIT PDF TITLE
    private void pdfDialog() {
        new android.app.AlertDialog.Builder(ResultActivity.this).setView(layout.pdf_dialog_layout).setPositiveButton(R.string.PdfTitleOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editTextTitle = ((android.app.AlertDialog) dialog).findViewById(id.edit_text_title);
                String pdfTitle = editTextTitle.getText().toString();
                createPdf(editText.getText().toString(), pdfTitle);


            }
        })
                .setNegativeButton(R.string.PdfTitleOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }


}
