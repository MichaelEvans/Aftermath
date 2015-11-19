package org.michaelevans.aftermath.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.michaelevans.aftermath.Aftermath;
import org.michaelevans.aftermath.OnActivityResult;
import org.michaelevans.aftermath.OnRequestPermissionResult;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    TextView contactUri;
    TextView photoUri;

    static final int PICK_CONTACT_REQUEST = 1;
    static final int OTHER_REQUEST = 2;
    static final int GET_ACCOUNTS_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactUri = (TextView) findViewById(R.id.contact_uri);
        photoUri = (TextView) findViewById(R.id.photo_uri);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission(Manifest.permission.READ_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Aftermath.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Aftermath.onRequestPermissionResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnActivityResult(PICK_CONTACT_REQUEST)
    public void onContactPicked(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            this.contactUri.setText("" + data.getStringExtra("key"));
        } else {
            this.contactUri.setText(R.string.an_error_has_occurred);
        }
    }

    @OnActivityResult(OTHER_REQUEST)
    public void onOtherRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            this.photoUri.setText("" + data.getStringExtra("key"));
        } else {
            this.photoUri.setText(R.string.an_error_has_occurred);
        }
    }

    @OnRequestPermissionResult(GET_ACCOUNTS_PERMISSION_REQUEST)
    public void onGetContactsRequested(String[] permissions, int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.permission_granted_message), Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermission(@NonNull final String permission) {
        // check for permission here
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // show a notification here and check permission again later
                Snackbar.make(findViewById(R.id.container), getString(R.string.accounts_permission_request_information), Snackbar.LENGTH_LONG)
                        .setCallback(new SnackBarCallback(this, permission))
                        .show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, GET_ACCOUNTS_PERMISSION_REQUEST);
            }
        }
    }

    public void startPicker(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    public void startPhotoPicker(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, OTHER_REQUEST);
        }
    }

    private static class SnackBarCallback extends Snackbar.Callback {
        private final WeakReference<MainActivity> mActivityReference;
        private final String mPermission;

        public SnackBarCallback(MainActivity activity, String permission) {
            mActivityReference = new WeakReference<MainActivity>(activity);
            mPermission = permission;
        }

        @Override
        public void onDismissed(Snackbar snackbar, int event) {
            super.onDismissed(snackbar, event);
            if (mActivityReference.get() != null) {
                ActivityCompat.requestPermissions(mActivityReference.get(), new String[]{mPermission}, GET_ACCOUNTS_PERMISSION_REQUEST);
            }
        }

        @Override
        public void onShown(Snackbar snackbar) {
            super.onShown(snackbar);
        }
    }
}


