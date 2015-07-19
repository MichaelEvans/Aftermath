package org.michaelevans.aftermath.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.michaelevans.aftermath.Aftermath;
import org.michaelevans.aftermath.OnActivityResult;

public class MainActivity extends AppCompatActivity {

    static final int PICK_CONTACT_REQUEST = 1;
    static final int OTHER_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Aftermath.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnActivityResult(PICK_CONTACT_REQUEST) //resultCode defaults to RESULT_OK
    public void onContactPickedSuccessfully(Intent data) {
        Toast.makeText(this, "Contact picked: " + data.getData(), Toast.LENGTH_SHORT).show();
    }

    @OnActivityResult(value = PICK_CONTACT_REQUEST)
    public void onContactPickedFailed() { //Intent parameter is optional
        Toast.makeText(this, "Result wasn't okay", Toast.LENGTH_SHORT).show();
    }

    @OnActivityResult(OTHER_REQUEST)
    public void onOtherRequest(Intent data) {
        Toast.makeText(this, "Some other request", Toast.LENGTH_SHORT).show();
    }

    public void startPicker(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }
}
