package com.example.krith.imagestore;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.krith.imagestore.Database.BulkInsertData;
import com.example.krith.imagestore.Database.DbOpenHelper;
import com.example.krith.imagestore.Events.InsertionCompleteEvent;
import com.example.krith.imagestore.Utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Home extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.start_btn)
    Button startBtn;

    @BindView(R.id.get_btn)
    Button getBtn;

    @BindView(R.id.text_view)
    TextView textView;

    @BindView(R.id.image_view)
    ImageView imageView;

    @BindView(R.id.edit_text)
    EditText editText;

    @BindView(R.id.main_layout)
    CoordinatorLayout mainLayout;

    private int imageTask = 0;
    private static final int REQUEST_CAMERA = 0;
    private static final int SELECT_FILE = 1;
    private String realPath = "";
    private Context context;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        context = this;
        new DbOpenHelper(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Image Store");
        setSupportActionBar(toolbar);
    }

    public ProgressDialog setProgressDialog(int rows) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Inserting the images, please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(rows);
        progressDialog.setProgress(0);
        return progressDialog;
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        selectImage();
    }

    @OnClick(R.id.get_btn)
    public void onGetClick() {
        try {
            if (editText.getText().length() > 0) {
                int rows = Integer.parseInt(editText.getText().toString());
                String path = DbOpenHelper.getInstance(getApplicationContext()).getRowData(rows);
                imageView.setImageURI(Uri.parse(path));
            } else {
                Snackbar.make(mainLayout, "Enter a value", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Snackbar.make(mainLayout, "There seems to be a problem", Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.start_btn)
    public void onStartClick() {
        try {
            if (editText.getText().length() > 0) {
                int rows = Integer.parseInt(editText.getText().toString());
                ProgressDialog progressDialog = setProgressDialog(rows);
                insertData(rows, realPath, progressDialog);
            } else {
                Snackbar.make(mainLayout, "Enter a value", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Snackbar.make(mainLayout, "There seems to be a problem", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(InsertionCompleteEvent event) {
        startBtn.setEnabled(true);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            try {
                if (requestCode == SELECT_FILE) {
                    realPath = Utility.getRealPathFromURI_API19(getApplicationContext(), data.getData());
                    imageView.setImageURI(Uri.parse(realPath));
                } else if (requestCode == REQUEST_CAMERA) {
                    realPath = Utility.convertCameraImageToBitmap(data);
                    imageView.setImageURI(Uri.parse(realPath));
                }
            } catch (Exception e) {
                Snackbar.make(mainLayout, "There seems to be some problem", Snackbar.LENGTH_SHORT).show();
            }
            return;
        }
    }

    public void insertData(int rows, String path, ProgressDialog progressDialog) throws UnsupportedEncodingException {
        startBtn.setEnabled(false);
        SQLiteDatabase db = DbOpenHelper.getInstance(getApplicationContext()).getWritableDatabase();
        new BulkInsertData(rows, path, context, progressDialog, db).execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (imageTask == REQUEST_CAMERA)
                        cameraIntent();
                    else if (imageTask == SELECT_FILE)
                        galleryIntent();
                } else {
                    Snackbar.make(mainLayout, "Needs permission", Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(context);
                switch (item) {
                    case REQUEST_CAMERA:
                        imageTask = item;
                        if (result)
                            cameraIntent();
                        break;

                    case SELECT_FILE:
                        imageTask = item;
                        if (result)
                            galleryIntent();
                        break;
                    default:
                        dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
