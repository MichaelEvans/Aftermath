package org.michaelevans.aftermath.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.michaelevans.aftermath.Aftermath;
import org.michaelevans.aftermath.OnActivityResult;

public class MainActivity extends AppCompatActivity {

    TextView contactUri;
    TextView photoUri;

    static final int PICK_CONTACT_REQUEST = 1;
    static final int OTHER_REQUSET = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactUri = (TextView) findViewById(R.id.contact_uri);
        photoUri = (TextView) findViewById(R.id.photo_uri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Aftermath.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnActivityResult(PICK_CONTACT_REQUEST)
    public void onContactPicked(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            this.contactUri.setText("" + data.getStringExtra("key"));
        } else {
            this.contactUri.setText(R.string.an_error_has_occurred);
        }
    }

    @OnActivityResult(OTHER_REQUSET)
    public void onOtherRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            this.photoUri.setText("" + data.getStringExtra("key"));
        } else {
            this.photoUri.setText(R.string.an_error_has_occurred);
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
            startActivityForResult(intent, OTHER_REQUSET);
        }
    }
}
