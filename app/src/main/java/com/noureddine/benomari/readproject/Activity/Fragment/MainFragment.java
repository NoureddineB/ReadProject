package com.noureddine.benomari.readproject.Activity.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.noureddine.benomari.readproject.Activity.Activity.ResultActivity;
import com.noureddine.benomari.readproject.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.getPickImageChooserIntent;


public class MainFragment extends Fragment {

    private InterstitialAd mInterstitialAd;
    private ImageButton button_OCR;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        button_OCR = v.findViewById(R.id.button);
        configureInterstitialAd();
        configureButton();
        return v;
    }


    private void configureButton() {
        button_OCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();
            }

        });

    }


    //CONFIGURE ADS
    private void configureInterstitialAd() {
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-2681791564432660/9381792155");
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
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("TAG", String.valueOf(errorCode));
            }

        });

    }

    //GET IMAGE
    private void startCamera() {
        checkVersion();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }


    private void checkVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 555);
            } catch (Exception ignored) {

            }
        } else {
            pickImage();
        }
    }

    //FOR ACTIVITY RESULT PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 555 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            checkVersion();
        }
    }

    //PICK IMAGE METHOD
    private void pickImage() {
        if (CropImage.isExplicitCameraPermissionRequired(getContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            }
        } else {
            Intent intent = getPickImageChooserIntent(getContext());
            startActivityForResult(intent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
        }
    }

    //CROP REQUEST JAVA
    private void croprequest(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(getContext(), this);
    }

    //FOR ACTIVITY RESULT
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //RESULT FROM SELECTED IMAGE
        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getActivity(), data);
            croprequest(imageUri);
        }

        //RESULT FROM CROPING ACTIVITY
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), result.getUri());
                    startOCRActivityWithExtra(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void startOCRActivityWithExtra(Bitmap bmp) {
        try {

            //Write file
            String filename = "bitmap.png";
            FileOutputStream stream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();
            bmp.recycle();

            //Pop intent
            Intent in1 = new Intent(getActivity(), ResultActivity.class);
            in1.putExtra("image", filename);
            startActivity(in1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}


