package com.project.android.activitycontrollers;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.project.android.model.Alarm;
import com.project.android.database.AppDatabaseHelper;
import com.project.android.utility.AppInstance;
import com.project.android.utility.Constants;
import com.project.android.model.Contact;
import com.project.android.model.ContactsList;
import com.project.android.R;
import com.project.android.model.SMS;
import com.project.android.model.User;
import com.project.android.utility.LocationTracker;

import java.util.ArrayList;

public class UserHomeActivity extends AppCompatActivity {
    AppDatabaseHelper databaseHelper = new AppDatabaseHelper(this);
    // LocationTracker class
    LocationTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        AppDatabaseHelper databaseHelper = new AppDatabaseHelper(this);
        User user = ((AppInstance) getApplicationContext()).getCurrentUser();

        ContactsList.setContacts(databaseHelper.getAllContactsOfUserWithID(user.getUserID()));
        gps = new LocationTracker(this);

    }


    public void viewPoliceStations()
    {
        Intent i = new Intent(this, MapsActivity.class);
        double latitude = 0.0;
        double longitude = 0.0;

        // check if GPS enabled

        if(gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }
        i.putExtra("Display", "police");
        i.putExtra("Latitude", latitude);
        i.putExtra("Longitude", longitude);

        startActivity(i);
    }

    public void viewHospitals()
    {
        Intent i = new Intent(this, MapsActivity.class);
        double latitude = 0.0;
        double longitude = 0.0;

        // check if GPS enabled

        if(gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }
        i.putExtra("Display", "hospital");
        i.putExtra("Latitude", latitude);
        i.putExtra("Longitude", longitude);

        startActivity(i);
    }


    public void sos(View view) {

        ArrayList<Contact> contacts = ContactsList.getContacts();
        if (contacts.size() > 0)
        {
            createSMS();
            callContact();
        }
        else
        {
            Toast.makeText(getApplicationContext(), Constants.NO_CONTACTS_ARE_CONFIGURED, Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, ConfigureContactsActivity.class);
            startActivity(i);
        }
    }

    public void call(View view) {

        ArrayList<Contact> contacts = ContactsList.getContacts();
        if (contacts.size() > 0)
        {
            callContact();
        }
        else {
            Toast.makeText(getApplicationContext(), Constants.NO_CONTACTS_ARE_CONFIGURED, Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, ConfigureContactsActivity.class);
            startActivity(i);
        }

    }

    public void sms(View view) {

        ArrayList<Contact> contacts = ContactsList.getContacts();
        if (contacts.size() > 0)
        {
            createSMS();
        }
        else
        {
            Toast.makeText(getApplicationContext(), Constants.NO_CONTACTS_ARE_CONFIGURED, Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, ConfigureContactsActivity.class);
            startActivity(i);
        }

    }

    public void settings(View view) {
        Intent i = new Intent(this, UserPreferencesActivity.class);
        startActivity(i);
    }

    public void alarm(View view) {
        User user = ((AppInstance) getApplicationContext()).getCurrentUser();

        Alarm alarm = databaseHelper.getAlarmOfUserWithID(user.getUserID());

        MediaPlayer player = null;
        if (alarm == null) {
            player = MediaPlayer.create(this, R.raw.ambulance);
        } else {

            int resID = getResources().getIdentifier(alarm.getName(), "raw", getPackageName());

            player = MediaPlayer.create(this, resID);
        }
        player.start();

    }

    public void createSMS() {
        User user = ((AppInstance) getApplicationContext()).getCurrentUser();

        SMS sms = databaseHelper.getSMSOfUserWithID(user.getUserID());
        String smsText = null;

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            if (sms == null) {
                smsText = Constants.DEFAULT_SMS_TEXT + ". My location is: https://maps.google.com/?q=" + latitude + "," + longitude + "\nSent by Keep Me Safe";
            } else {
                smsText = sms.getText() + ". My location is: https://maps.google.com/?q=" + latitude + "," + longitude + "\nSent by Keep Me Safe";
            }
            ArrayList<Contact> contacts = ContactsList.getContacts();

            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED)
            {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(UserHomeActivity.this,
                        Manifest.permission.SEND_SMS))
                {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                }
                else
                {

                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(UserHomeActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            Constants.MY_PERMISSIONS_REQUEST_SEND_SMS);

                    // MY_PERMISSIONS_REQUEST_SEND_SMS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            else
            {
                for (Contact contact : contacts)
                {
                    sendSms(contact.getNumber(), smsText);
                    // Permission has already been granted
                }
                SharedPreferences mypref = PreferenceManager.getDefaultSharedPreferences(this);

                if (mypref.contains(Constants.CONTACT_NUMBER_KEY)) {
                    sendSms(mypref.getString(Constants.CONTACT_NUMBER_KEY, null), smsText);
                }

                Toast.makeText(getApplicationContext(), Constants.SMS_SENT_TO_CONTACTS_SUCCESSFULLY, Toast.LENGTH_LONG).show();
            }

        }
        else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    User user = ((AppInstance) getApplicationContext()).getCurrentUser();

                    SMS sms = databaseHelper.getSMSOfUserWithID(user.getUserID());
                    String smsText = null;
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    if (sms == null) {
                        smsText = Constants.DEFAULT_SMS_TEXT + ". My location is: https://maps.google.com/?q=" + latitude + "," + longitude + "\nSent by Keep Me Safe";
                    } else {
                        smsText = sms.getText() + ". My location is: https://maps.google.com/?q=" + latitude + "," + longitude + "\nSent by Keep Me Safe";
                    }
                    ArrayList<Contact> contacts = ContactsList.getContacts();

                    for (Contact contact : contacts) {
                        sendSms(contact.getNumber(), smsText);
                    }

                    SharedPreferences mypref = PreferenceManager.getDefaultSharedPreferences(this);

                    if (mypref.contains(Constants.CONTACT_NUMBER_KEY)) {
                        sendSms(mypref.getString(Constants.CONTACT_NUMBER_KEY, null), smsText);
                    }

                    Toast.makeText(getApplicationContext(), Constants.SMS_SENT_TO_CONTACTS_SUCCESSFULLY, Toast.LENGTH_LONG).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case Constants.MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ArrayList<Contact> contacts = ContactsList.getContacts();

                    if (contacts.size() > 0) {
                        String number = contacts.get(0).getNumber();
                        String call = "tel:" + number;

                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse(call));
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(callIntent);
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.


                    // other 'case' lines to check for other
                    // permissions this app might request.
                }
            }
        }
    }

    private void callContact()
    {
        ArrayList<Contact> contacts = ContactsList.getContacts();

        String number = contacts.get(0).getNumber();
        String call = "tel:" + number;

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(UserHomeActivity.this,
                    Manifest.permission.CALL_PHONE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(UserHomeActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        Constants.MY_PERMISSIONS_REQUEST_CALL_PHONE);

                // MY_PERMISSIONS_REQUEST_SEND_SMS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(call));
            startActivity(callIntent);
            // Permission has already been granted
        }

    }

    private void sendSms(String phonenumber, String message)
    {
        SmsManager manager = SmsManager.getDefault();

        int length = message.length();

        if(length > 160)
        {
            ArrayList<String> messagelist = manager.divideMessage(message);

            manager.sendMultipartTextMessage(phonenumber, null, messagelist, null, null);
        }
        else
        {
            manager.sendTextMessage(phonenumber, null, message, null, null);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.fakeCall:
                Intent fakeCallIntent = new Intent(this, FakeCallActivity.class);
                startActivity(fakeCallIntent);
                return true;

            case R.id.viewHospitals:
                viewHospitals();
                return true;

            case R.id.viewPolice:
                viewPoliceStations();
                return true;


            case R.id.firstaid:

                Intent pdfintent = new Intent(this, PDFViewerActivity.class);
                pdfintent.putExtra("File", "firstaid.pdf");
                startActivity(pdfintent);

                return true;


            case R.id.logout:
                ((AppInstance)getApplicationContext()).setCurrentUser(null);
                ContactsList.clearContacts();
                Intent i = new Intent(this, UserLoginActivity.class);
                startActivity(i);
                finish();
                return true;


            case R.id.manual:

                Intent intent = new Intent(this, PDFViewerActivity.class);
                intent.putExtra("File", "sd.pdf");
                startActivity(intent);

                return true;

        }
        return false;
    }

    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

}


