package com.bozorgzad.ali.introducingtvus;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Created by Ali_Dev on 9/11/2017.
 */

public class ActivityFeedback extends ActivityAppBarBackOrCloseButton {

    private final int SCALE_WIDTH_SIZE = 800;
    private final int SCALE_HEIGHT_SIZE = 800;
    private final int COMPRESS_QUALITY = 100;
    private final int MAX_PICTURE_SIZE_IN_BYTE = 410000;  // 400 KB
    private final int RESULT_LOAD_IMAGE = 1;
    private final int MAX_SCREENSHOTS_ITEMS = 3;

    private TextView txtRemoveImage1;
    private TextView txtRemoveImage2;
    private TextView txtRemoveImage3;

    private ImageView imgClicked;
    private Bitmap bitmap;
    private File[] imageFiles = new File[MAX_SCREENSHOTS_ITEMS];

    private ProgressDialog progressDialog;
    private int numberOfImagesMustUpload;
    private boolean isUserCancelTheUpload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Set Toolbar
        setBackOrCloseToolbar(false, getString(R.string.feedback_toolbar_title));

        // Hide keyboard when enter
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Set font for txtIcons
        // we can use Image instead of Font if we have a lot of items
        // but we use font for size of application(don't add plus image)
        txtRemoveImage1 = (TextView) findViewById(R.id.txtRemoveImage1);
        txtRemoveImage2 = (TextView) findViewById(R.id.txtRemoveImage2);
        txtRemoveImage3 = (TextView) findViewById(R.id.txtRemoveImage3);
        setFontForTxtAddAndRemoveIcon();

        // onClick when we want ot remove an image
        onClickForRemoveAnImage();

        // when want to add image
        onClickForAddScreenshot();
    }

    private void setFontForTxtAddAndRemoveIcon(){
        TextView txtAddImage1 = (TextView) findViewById(R.id.txtAddImage1);
        TextView txtAddImage2 = (TextView) findViewById(R.id.txtAddImage2);
        TextView txtAddImage3 = (TextView) findViewById(R.id.txtAddImage3);
        Typeface font = Typeface.createFromAsset( getAssets(), "fonts/fontawesome-webfont.ttf");
        txtAddImage1.setTypeface(font);
        txtAddImage2.setTypeface(font);
        txtAddImage3.setTypeface(font);
        txtRemoveImage1.setTypeface(font);
        txtRemoveImage2.setTypeface(font);
        txtRemoveImage3.setTypeface(font);
    }

    private void onClickForRemoveAnImage(){
        View.OnClickListener removeImageOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txtRemoveImage = (TextView) view;

                // remove the image that click on that
                ViewGroup removeImageParent = (ViewGroup) view.getParent();
                ImageView imgScreenShot = (ImageView) removeImageParent.getChildAt(1);
                imgScreenShot.setImageDrawable(null);

                // get the image that we click on that
                String tag = txtRemoveImage.getTag().toString();
                int txtRemoveNumber = Integer.parseInt(tag);

                // first delete the file and then remove it from the array
                boolean delete = imageFiles[txtRemoveNumber -1].delete();
                imageFiles[txtRemoveNumber -1] = null;

                if(txtRemoveNumber == MAX_SCREENSHOTS_ITEMS){
                    // when we want to remove the last image
                    txtRemoveImage.setVisibility(View.GONE);
                }else{
                    ViewGroup rvParent = (ViewGroup) removeImageParent.getParent();

                    // when we remove one image, all of the images after that SHIFT ONE BACK
                    for(int i=txtRemoveNumber; i < MAX_SCREENSHOTS_ITEMS; i++){
                        RelativeLayout relativeLayoutFirst = (RelativeLayout) rvParent.getChildAt(i-1);
                        ImageView imageViewFirst = (ImageView) relativeLayoutFirst.getChildAt(1);
                        TextView txtRemoveImageFirst = (TextView) relativeLayoutFirst.getChildAt(2);

                        RelativeLayout relativeLayoutSecond = (RelativeLayout) rvParent.getChildAt(i);
                        ImageView imageViewSecond = (ImageView) relativeLayoutSecond.getChildAt(1);
                        TextView txtRemoveImageSecond = (TextView) relativeLayoutSecond.getChildAt(2);

                        if(imageViewSecond.getDrawable() == null){
                            relativeLayoutSecond.setVisibility(View.INVISIBLE);
                            txtRemoveImageFirst.setVisibility(View.GONE);
                            break;
                        }else{
                            String tagImage = imageViewSecond.getTag().toString();
                            int imageNumber = Integer.parseInt(tagImage);
                            if(imageNumber == MAX_SCREENSHOTS_ITEMS){
                                // if last item has image, only remove the image from that
                                txtRemoveImageSecond.setVisibility(View.GONE);
                            }

                            imageViewFirst.setImageDrawable(imageViewSecond.getDrawable());
                            imageViewSecond.setImageDrawable(null);
                            imageFiles[i-1] = imageFiles[i];
                            imageFiles[i] = null;
                        }
                    }
                }
            }
        };
        txtRemoveImage1.setOnClickListener(removeImageOnClickListener);
        txtRemoveImage2.setOnClickListener(removeImageOnClickListener);
        txtRemoveImage3.setOnClickListener(removeImageOnClickListener);
    }

    private void onClickForAddScreenshot(){
        View.OnClickListener imgOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgClicked = (ImageView) view;
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        };
        ImageView imgScreenShot1 = (ImageView) findViewById(R.id.imgScreenShot1);
        ImageView imgScreenShot2 = (ImageView) findViewById(R.id.imgScreenShot2);
        ImageView imgScreenShot3 = (ImageView) findViewById(R.id.imgScreenShot3);
        imgScreenShot1.setOnClickListener(imgOnClickListener);
        imgScreenShot2.setOnClickListener(imgOnClickListener);
        imgScreenShot3.setOnClickListener(imgOnClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                bitmap = getScaledBitmap(selectedImage,SCALE_WIDTH_SIZE,SCALE_HEIGHT_SIZE);
                imgClicked.setImageBitmap(bitmap);

                if(imgClicked.getDrawable() != null){
                    // Visible remove icon
                    ViewGroup imgParent = (ViewGroup) imgClicked.getParent();
                    TextView txtRemoveImage = (TextView) imgParent.getChildAt(2);
                    txtRemoveImage.setVisibility(View.VISIBLE);

                    // Visible next relativeLayout for add another item
                    String tag = imgClicked.getTag().toString();
                    int imgNumber = Integer.parseInt(tag);
                    if(imgNumber != MAX_SCREENSHOTS_ITEMS){
                        ViewGroup rvParent = (ViewGroup) imgParent.getParent();
                        RelativeLayout relativeLayout = (RelativeLayout) rvParent.getChildAt(imgNumber);
                        relativeLayout.setVisibility(View.VISIBLE);
                    }

                    // set quality for Bitmap and set in file
                    // if size of image is to high, then reduce the quality of that
                    try {
                        File file = new File(getFilesDir(), "Image_" +System.currentTimeMillis()+ ".jpg");

                        int i = 0;
                        do{
                            int quality = COMPRESS_QUALITY - i;
                            FileOutputStream out = openFileOutput(file.getName(),MODE_PRIVATE);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                            out.flush();
                            out.close();

                            i += 10;
                        }while(file.length() > MAX_PICTURE_SIZE_IN_BYTE);
                        imageFiles[imgNumber - 1] = file;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getScaledBitmap(Uri selectedImage, int width, int height) throws FileNotFoundException {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, sizeOptions);

        int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = inSampleSize;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, sizeOptions);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_search; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_send_feedback) {
            attemptSendFeedback();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void attemptSendFeedback(){
        EditText edtFeedbackText = (EditText) findViewById(R.id.edtFeedbackText);

        // Reset errors.
        edtFeedbackText.setError(null);

        // Store values at the time of the feedbackText attempt.
        String feedbackText = edtFeedbackText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid feedbackText.
        if (feedbackText.isEmpty() || feedbackText.length() < 15) {
            edtFeedbackText.setError(getString(R.string.feedback_edt_at_least));
            focusView = edtFeedbackText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            sendFeedback(feedbackText);
        }
    }

    private void sendFeedback(String feedbackText) {
        if (!Global.isConnectedToNetwork(this)) {
            Toast.makeText(this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        // count how many images we want to upload
        numberOfImagesMustUpload = 0;
        for(int i=0; i<MAX_SCREENSHOTS_ITEMS; i++){
            if(imageFiles[i] != null){
                numberOfImagesMustUpload++;
            }
        }

        Global.showProgressDialog(getString(R.string.progress_dialog_wait), this);
        String mobileInfo = Build.BRAND +"|"+ Build.MODEL +"|"+ Build.DEVICE +"|"+ Build.VERSION.SDK_INT;
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("feedbackText", feedbackText)
                .appendQueryParameter("mobileInfoBrandModelDeviceSdk", mobileInfo);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/insertFeedbackTextAndGetId", builder) {
            @Override
            public void onPostExecute(final String result) {
                Global.hideProgressDialog();
                if (result != null) {
                    // here: result have a id of feedback record
                    sendFeedbackImages(result, 1);
                } else {
                    Toast.makeText(ActivityFeedback.this, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void sendFeedbackImages(final String feedbackId, final int imageIndex){
        if(imageFiles[imageIndex -1] != null){
            if(imageIndex == 1){
                // create progress bar for first image
                progressDialog = new ProgressDialog(ActivityFeedback.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(true);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            }
            // add listener for when we cancel upload
            isUserCancelTheUpload = false;
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isUserCancelTheUpload = true;
                    Toast.makeText(ActivityFeedback.this, R.string.feedback_progressbar_cancel, Toast.LENGTH_LONG).show();
                }
            });

            // set progress and message at the start of each image
            progressDialog.setProgress(0);
            progressDialog.setMessage(getString(R.string.feedback_progressbar_text) + " (" +imageIndex+ "/" + numberOfImagesMustUpload +")");

            // we set the feedbackId and the image number for the name of the image on the server
            new UploadFile(Global.HOST_ADDRESS + "/webservice/sendFeedbackImages", imageFiles[imageIndex -1], feedbackId+"_" +imageIndex+ ".jpg") {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if(imageIndex == 1){
                        progressDialog.show();
                    }
                }

                @Override
                public void onPostExecute(final String result) {
                    if (result != null) {
                        if(isUserCancelTheUpload){
                            // because the 'move_uploaded_file' function in php takes much time to copy(android send data a bit late)
                            // all of the images form tempPath to specifiedPath
                            // we delete feedback record and images a little bit late
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Do something after 30 sec = 30000ms
                                    Uri.Builder builder = new Uri.Builder()
                                            .appendQueryParameter("feedbackId", feedbackId)
                                            .appendQueryParameter("numberOfImagesMustUpload", String.valueOf(numberOfImagesMustUpload));
                                    new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/deleteFeedbackCancelByUser", builder).execute();
                                }
                            }, 30000);
                        }else{
                            if(imageIndex == MAX_SCREENSHOTS_ITEMS){
                                // when last item uploaded
                                progressDialog.dismiss();
                                Toast.makeText(ActivityFeedback.this, R.string.feedback_send_successfully, Toast.LENGTH_LONG).show();
                                finish();
                            }else{
                                // call sendFeedbackImages for upload the next image
                                sendFeedbackImages(feedbackId, imageIndex +1);
                            }
                        }
                    } else {
                        // if the internet is disconnected while uploading the images, we run these lines
                        // right now we have a feedback without images on database
                        progressDialog.dismiss();
                        Toast.makeText(ActivityFeedback.this, R.string.feedback_loading_failed, Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }else{
                // when there is no image to send to server
                if(imageIndex != 1){
                    progressDialog.dismiss();
                }
                Toast.makeText(ActivityFeedback.this, R.string.feedback_send_successfully, Toast.LENGTH_LONG).show();
                finish();
        }
    }

    private class UploadFile extends AsyncTask<String, Integer, String> {
        private String url = null;
        private File file = null;
        private String fileName = null;

        UploadFile(String urlConnect, File file, String fileName){
            url = urlConnect;
            this.file = file;
            this.fileName = fileName;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"uploaded_file\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = file.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

                int progress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[16];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();

                    progress += bytesRead;
                    // update progress bar
                    publishProgress((int) ((progress * 100) / (file.length())));
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();
                out.close();

                return "uploadedSuccessfully";

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // when we exit form feedback, remove all of the image files
        for(int i=0; i<MAX_SCREENSHOTS_ITEMS; i++){
            if(imageFiles[i] != null){
                boolean delete = imageFiles[i].delete();
            }
        }
    }
}
