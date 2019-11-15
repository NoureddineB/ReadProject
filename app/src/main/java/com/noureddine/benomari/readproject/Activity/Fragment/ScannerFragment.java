package com.noureddine.benomari.readproject.Activity.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.noureddine.benomari.readproject.BuildConfig;
import com.noureddine.benomari.readproject.R;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ScannerFragment extends Fragment {
    private InterstitialAd mInterstitialAd;
    private static final int OPEN_THING = 99;
    private ImageButton button_Scanner_camera;
    private ImageButton button_Scanner_gallery;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scanner, container, false);
        button_Scanner_camera = v.findViewById(R.id.button2);
        button_Scanner_gallery = v.findViewById(R.id.button_gallery);
        configureInterstitialAd();
        configureButton();
        return v;
    }


    private void configureButton() {
        button_Scanner_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScannerCamera();
            }

        });
        button_Scanner_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScannerGallery();
            }

        });

    }

    //CONFIGURE ADS
    private void configureInterstitialAd() {
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-2681791564432660/7110852036");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdLoaded() {

            }

        });

    }


    //FOR ACTIVITY RESULT
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //RESULT FROM SCAN ACTIVITY
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == OPEN_THING) {
                Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    getContext().getContentResolver().delete(uri, null, null);
                    pdfDialog(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void startScannerCamera() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        openCamera();

    }

    private void startScannerGallery() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        openGallery();

    }


    private void openCamera() {
        int preference = ScanConstants.OPEN_CAMERA;
        Intent intent = new Intent(getContext(), ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, OPEN_THING);
    }

    private void openGallery() {
        int preference = ScanConstants.OPEN_MEDIA;
        Intent intent = new Intent(getContext(), ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, OPEN_THING);
    }

    private void createPdf(Bitmap bitmap, String pdftitle) {

        //Pdf Document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();

        // Page 1
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        canvas.drawBitmap(bitmap, 0, 0, paint);
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
        } catch (IOException e) {
            Toast.makeText(getContext(), "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
        showPDF(targetPdf);

    }

    private void showPDF(String targetpdf) {
        File file = new File(targetpdf);
        Uri photoURI = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(photoURI, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void pdfDialog(final Bitmap bitmap) {
        new AlertDialog.Builder(getContext()).setView(R.layout.pdf_dialog_layout).setPositiveButton(R.string.PdfTitleOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editTextTitle = ((AlertDialog) dialog).findViewById(R.id.edit_text_title);
                String pdfTitle = editTextTitle.getText().toString();
                createPdf(bitmap, pdfTitle);


            }
        })
                .setNegativeButton(R.string.PdfTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }


    public static ScannerFragment newInstance() {
        ScannerFragment fragment = new ScannerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}