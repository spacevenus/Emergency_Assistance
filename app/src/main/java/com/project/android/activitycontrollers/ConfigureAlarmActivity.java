package com.project.android.activitycontrollers;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.project.android.model.Alarm;
import com.project.android.database.AppDatabaseHelper;
import com.project.android.utility.AppInstance;
import com.project.android.utility.Constants;
import com.project.android.model.ContactsList;
import com.project.android.R;
import com.project.android.model.User;
import com.project.android.utility.Utility;

import java.io.File;


public class ConfigureAlarmActivity extends AppCompatActivity {

    RadioGroup soundTypeRG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurealarm);
        soundTypeRG = (RadioGroup) findViewById(R.id.soundType);

        User user = ((AppInstance) getApplicationContext()).getCurrentUser();
        AppDatabaseHelper databaseHelper = new AppDatabaseHelper(this);
        Alarm alarm = databaseHelper.getAlarmOfUserWithID(user.getUserID());

        if (alarm != null)
        {
            String name = alarm.getName();
            if (name.equals(Constants.AMBULANCE))
            {
                soundTypeRG.check(R.id.ambulance);
            }
            else if (name.equals(Constants.SIREN))
            {
                soundTypeRG.check(R.id.siren);
            }
            else if (name.equals(Constants.POLICE)) {
                soundTypeRG.check(R.id.police);
            }
        }
        else
        {
            soundTypeRG.check(R.id.ambulance);
        }

    }

    public void playSound(View view)
    {
        int id = soundTypeRG.getCheckedRadioButtonId();
        MediaPlayer player = null;

        switch (id)
        {
            case R.id.ambulance:
                player = MediaPlayer.create(this, R.raw.ambulance);
                break;

            case R.id.police:
                player = MediaPlayer.create(this, R.raw.police);
                break;

            case R.id.siren:
                player = MediaPlayer.create(this, R.raw.siren);
                break;
        }
        player.start();
    }

    public void setAsAlarm(View view)
    {
        User user = ((AppInstance) getApplicationContext()).getCurrentUser();
        String alarmName = null;
        int id = soundTypeRG.getCheckedRadioButtonId();
        switch (id)
        {
            case R.id.ambulance:
                alarmName = Constants.AMBULANCE;
                break;

            case R.id.police:
                alarmName = Constants.POLICE;
                break;

            case R.id.siren:
                alarmName = Constants.SIREN;
                break;
        }

        Alarm alarm = new Alarm(user.getUserID(), alarmName);
        AppDatabaseHelper databaseHelper = new AppDatabaseHelper(this);
        long alarmID = databaseHelper.addAlarm(alarm);
        alarm.setAlarmID(alarmID);
        Toast.makeText(getApplicationContext(), Constants.ALARM_SET_SUCCESSFULLY, Toast.LENGTH_LONG).show();

    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sub_menu, menu);
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

            case R.id.firstaid:

                Intent pdfintent = new Intent(this, PDFViewerActivity.class);
                pdfintent.putExtra("File", "firstaid.pdf");
                startActivity(pdfintent);
                return true;

            case R.id.about:
                android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
                dialogBuilder.setIcon(R.drawable.logo);
                dialogBuilder.setTitle(R.string.app_name);
                dialogBuilder.setMessage(Constants.APP_DESCRIPTION);
                dialogBuilder.create();
                dialogBuilder.show();
                return true;

            case R.id.help:
                android.app.AlertDialog.Builder helpDialogBuilder = new android.app.AlertDialog.Builder(this);
                helpDialogBuilder.setIcon(R.drawable.logo);
                helpDialogBuilder.setTitle(R.string.app_name);
                helpDialogBuilder.setMessage("Please mail to \nkeepmesafefeedback@gmail.com\nfor any help regarding the app.");
                helpDialogBuilder.create();
                helpDialogBuilder.show();
                return true;

            case R.id.feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Keep Me Safe Feedback");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"keepmesafefeedback@gmail.com"});

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
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
}
