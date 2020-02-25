package com.example.imagelabeller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.protobuf.ByteString;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button capture;
    ImageView previewImage;
    String pathToFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final ImageView previewImage = findViewById(R.id.previewImage);
        capture = findViewById(R.id.capture);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    // take photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        previewImage = findViewById(R.id.previewImage);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) BitmapFactory.decodeFile(pathToFile);
            previewImage.setImageBitmap(imageBitmap);
            try {
                invokeJson(imageBitmap);
            } catch (JSONException e ) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    String currentPhotoPath;
    PrintStream output;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String imageFileName = "JPG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                pathToFile = photoFile.getAbsolutePath();
                try {
                    PrintStream output = new PrintStream(photoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.imagelabeller.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void invokeJson(Bitmap bitmap) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteStream);
        String base64Data = Base64.encodeToString(byteStream.toByteArray(),
                Base64.URL_SAFE);
        String requestURL =
                "https://vision.googleapis.com/v1/images:annotate?key=" +
                        getResources().getString(R.string.mykey);
        // Create an array containing
        // the LABEL_DETECTION feature
        JSONArray features = new JSONArray();
        JSONObject feature = new JSONObject();
        feature.put("type", "LABEL_DETECTION");
        features.put(feature);

    // Create an object containing
    // the Base64-encoded image data
        JSONObject imageContent = new JSONObject();
        imageContent.put("content", base64Data);

    // Put the array and object into a single request
    // and then put the request into an array of requests
        JSONArray requests = new JSONArray();
        JSONObject request = new JSONObject();
        request.put("image", imageContent);
        request.put("features", features);
        requests.put(request);
        JSONObject postData = new JSONObject();
        postData.put("requests", requests);

    // Convert the JSON into a
    // string
        String body = postData.toString();
        detectLocalizedObjects(body, output);
    }

    /**
     * Detects localized objects in the specified local image.
     *
     * @param filePath The path to the file to perform localized object detection on.
     * @param out A {@link PrintStream} to write detected objects to.
     * @throws Exception on errors while closing the client.
     * @throws IOException on Input/Output errors.
     */
    public static void detectLocalizedObjects(String filePath, PrintStream out)
            throws Exception, IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        Image img = Image.newBuilder().setContent(imgBytes).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder()
                        .addFeatures(Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION))
                        .setImage(img)
                        .build();
        requests.add(request);

        // Perform the request
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            // Display the results
            for (AnnotateImageResponse res : responses) {
                for (LocalizedObjectAnnotation entity : res.getLocalizedObjectAnnotationsList()) {
                    out.format("Object name: %s\n", entity.getName());
                    out.format("Confidence: %s\n", entity.getScore());
                    out.format("Normalized Vertices:\n");
                    entity
                            .getBoundingPoly()
                            .getNormalizedVerticesList()
                            .forEach(vertex -> out.format("- (%s, %s)\n", vertex.getX(), vertex.getY()));
                }
            }
        }
    }
}