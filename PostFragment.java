package com.the360lifechange.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.the360lifechange.R;
import com.the360lifechange.activity.SplashScreen;
import com.the360lifechange.api.API;
import com.the360lifechange.utils.BitmapProcessor;
import com.the360lifechange.utils.Pref;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by thanhcs94 on 6/7/2016.
 */
public class PostFragment extends Fragment {
    Dialog mDialogUpLoad;
    private static final int IMAGE_PICK 	= 1;
    private static final int IMAGE_CAPTURE 	= 2;
    private Uri mImageUri;
    String URL_IMAGE = "";
    ImageView imgPost;
    File photo = null;
    LinearLayout llImage;
    Button btUpload;
    Pref mPref;
    public static final String IMAGE_DIRECTORY_NAME = "360LifeChange";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private final OkHttpClient client = new OkHttpClient();
    public PostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPref = new Pref(getActivity());
        View v = inflater.inflate(R.layout.fragment_post, container, false);
        imgPost = (ImageView) v.findViewById(R.id.img_post);
        llImage = (LinearLayout) v.findViewById(R.id.llImage);
        btUpload = (Button) v.findViewById(R.id.btUpload);
        mDialogUpLoad=  chooseImageDialog();
        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog d = chooseImageDialog();
                d.show();
            }
        });
        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SplashScreen.showToast(getActivity(), URL_IMAGE);
                new PostImg().execute();
            }
        });
        return v;
    }

    public Dialog chooseImageDialog(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.chooseimagedialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //Log.wtf("TAG" , mImageUri.toString());
            }
        });
        ImageView imCam =  (ImageView) dialog.findViewById(R.id.imageView6);
        ImageView allery =  (ImageView) dialog.findViewById(R.id.imageView8);
        imCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseFromCamera();
                dialog.dismiss();
            }
        });
        allery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFromSD();
                dialog.dismiss();

            }
        });
        return dialog;
    }

    private void choseFromCamera() {

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        try
        {
            // place where to store camera taken picture
            photo = createTemporaryFile("picture", ".jpg");
            photo.delete();
        }
        catch(Exception e)
        {
            Log.v("TAG", "Can't create file to take picture!");
            Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
        }
        mImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
       startActivityForResult(intent, IMAGE_CAPTURE);
    }

    private void chooseFromSD() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
       startActivityForResult(Intent.createChooser(intent, "Escolha uma Foto"), IMAGE_PICK);
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                //Log.wtf("TAG", "Oops! Failed create "
                //        + AppConfig.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    /**
     * Image result from gallery
     * @param resultCode
     * @param data
     */

    /**
     * Receive the result from the startActivity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case IMAGE_PICK:
                    Uri selectedImage = data.getData();
                    String [] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();
                    URL_IMAGE =filePath;
                    Log.wtf("PATH", URL_IMAGE);
                    Bitmap image = BitmapFactory.decodeFile(URL_IMAGE);
                    BitmapProcessor bm = new BitmapProcessor(image, 130 , 130);
                    addImage(bm.getBitmap());
                    break;
                case IMAGE_CAPTURE:
                    URL_IMAGE = photo.getAbsolutePath();
                    Log.wtf("PATH", photo.getAbsolutePath() + "");
                    Bitmap image2 = BitmapFactory.decodeFile(photo.getAbsolutePath());
                    BitmapProcessor bm2 = new BitmapProcessor(image2, 130 , 130);
                    addImage(bm2.getBitmap());
                    break;
                default:
                    break;
            }
        }
    }

    private void addImage(Bitmap image) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.item_select_image, null, false);
        RoundedImageView img = (RoundedImageView)v.findViewById(R.id.img_select);
        img.setImageBitmap(image);
        llImage.addView(v);
    }

    public class PostImg extends AsyncTask<Void, Void , Void>{
        Response response = null;
        @Override
        protected Void doInBackground(Void... params) {

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("post_title", "Square Logo")
                    .addFormDataPart("post_description", "Square Logo")
                    .addFormDataPart("post_type", "Square Logo")
                    .addFormDataPart("user_token",mPref.getUSER_ID())
                    .addFormDataPart("post_long", "Square Logo")
                    .addFormDataPart("post_lat", "Square Logo")
                    .addFormDataPart("post_thumb[]", "FILE",
                            RequestBody.create(MEDIA_TYPE_PNG, new File(URL_IMAGE)))
                    .build();

            Request request = new Request.Builder()
                    .url(API.MEW_POST)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!response.isSuccessful()) try {
                throw new IOException("Unexpected code " + response);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                SplashScreen.showToast(getActivity(), response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }
    }
}
