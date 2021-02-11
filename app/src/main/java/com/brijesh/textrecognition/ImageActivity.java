package com.brijesh.textrecognition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ImageActivity extends AppCompatActivity {
//    EditText tv;
    ImageView img;
    Button Next;
    String Text;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int REQUEST_CODE = 101;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
//        tv = findViewById(R.id.tv);
        img = findViewById(R.id.img);
        Next = findViewById(R.id.btnNext);
        Next.setClickable(false);

        if(checkSelfPermission(Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogBox();
            }
        });
        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Text == null) {
                    Toast.makeText(getApplicationContext(),"Please select the image",Toast.LENGTH_LONG).show();
                } else if(Text.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"There is no text in this image",Toast.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(ImageActivity.this, TextActivity.class);
                    intent.putExtra("text", Text);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //
                assert data != null;
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                img.setImageURI(resultUri);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) img.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!recognizer.isOperational()){
                    Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
                }else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    for(int i = 0; i < items.size(); i++){
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }
//                    tv.setText(sb.toString());
                    Text = sb.toString();
                }
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(this,""+error,Toast.LENGTH_LONG).show();
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Bundle bundle = data.getExtras();
//        Bitmap bitmap = (Bitmap) bundle.get("data");
//        img.setImageBitmap(bitmap);
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
//        FirebaseVision vision = FirebaseVision.getInstance();
//        FirebaseVisionTextRecognizer textRecognizer = vision.getOnDeviceTextRecognizer();
////        FirebaseVisionTextRecognizer textRecognizer = vision.getCloudTextRecognizer();       // powerful method but need billing account
//        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                String s = firebaseVisionText.getText();
//                tv.setText(s);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
//            }
//        });
//    }

    private void DialogBox(){
        String[] items = {"Camera", "Gallery" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    //Camera
                    if(checkSelfPermission(Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
                        requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE }, CAMERA_REQUEST_CODE);
                    }else {
                        pickCamera();
                    }
                }
                if(which == 1){
                    //Gallery
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                    }else {
                        pickGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean CameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(CameraAccepted && WriteStorageAccepted){
                        pickCamera();
                    }else {
                        Toast.makeText(this, "Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean WriteStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(ReadStorageAccepted && WriteStorageAccepted){
                        pickGallery();
                    }else {
                        Toast.makeText(this, "Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void pickGallery() {
        Intent intentStorage = new Intent(Intent.ACTION_PICK);
        intentStorage.setType("image/*");
        startActivityForResult(intentStorage,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Take Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image To Text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intentCamera,IMAGE_PICK_CAMERA_CODE);
    }
}


//// start picker to get image for cropping and then use the image in cropping activity
//CropImage.activity()
//        .setGuidelines(CropImageView.Guidelines.ON)
//        .start(this);
//
//// start cropping activity for pre-acquired image saved on the device
//        CropImage.activity(imageUri)
//        .start(this);
//
//// for fragment (DO NOT use `getActivity()`)
//        CropImage.activity()
//        .start(getContext(), this);