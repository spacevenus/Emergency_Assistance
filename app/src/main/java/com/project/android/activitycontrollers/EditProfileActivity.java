package com.project.android.activitycontrollers;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.project.android.R;
import com.project.android.database.AppDatabaseHelper;
import com.project.android.model.User;
import com.project.android.utility.AppInstance;
import com.project.android.utility.Constants;
import com.project.android.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity
{
    private EditText mailET, mobileET;
    User currentUser = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        initializeUIComponents();
    }

    public void initializeUIComponents()
    {
        mailET =  findViewById(R.id.mail);
        mobileET = findViewById(R.id.mobile);

        currentUser = ((AppInstance) getApplicationContext()).getCurrentUser();
        mailET.setText(currentUser.getMail());
        mobileET.setText(currentUser.getMobile());
    }

    public void editDetails(View view)
    {
        String mail = mailET.getText().toString().trim();
        String mobile = mobileET.getText().toString().trim();

        AppDatabaseHelper databaseHelper = new AppDatabaseHelper(this);
        ArrayList<String> userNames = databaseHelper.getAllUserNames();

        if (TextUtils.isEmpty(mail))  {
            mailET.setError(Constants.MISSING_MAIL);
            mailET.requestFocus();
        }
        else if(false == android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            mailET.setError(Constants.INVALID_MAIL);
            mailET.requestFocus();
        }
        else if (mobile.length() == 0) {
            mobileET.setError(Constants.MISSING_MOBILE);
            mobileET.requestFocus();
        }
        else if (mobile.length() != 10) {
            mobileET.setError(Constants.INVALID_MOBILE);
            mobileET.requestFocus();
        }
        else
        {
            currentUser.setMail(mail);
            currentUser.setMobile(mobile);

            databaseHelper.updateDetailsForUser(currentUser);
            Toast.makeText(this, Constants.DETAILS_EDITED_SUCCESSFULLY, Toast.LENGTH_SHORT).show();

            // close this activity
            finish();
        }
    }

    public static boolean isValidPassword(final String password)
    {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
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
                helpDialogBuilder.setMessage(Constants.HELP_MESSAGE);
                helpDialogBuilder.create();
                helpDialogBuilder.show();
                return true;

            case R.id.feedback:

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.FEEDBACK_SUBJECT);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.FEEDBACK_MAILID});

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }


                return true;

            case R.id.manual:

                Intent intent = new Intent(this, PDFViewerActivity.class);
                intent.putExtra("File", "sd.pdf");
                startActivity(intent);

                return true;
                case R.id.firstaid:

                    Intent pdfintent = new Intent(this, PDFViewerActivity.class);
                    pdfintent.putExtra("File", "firstaid.pdf");
                    startActivity(pdfintent);
                    return true;

        }
        return false;
    }


}
