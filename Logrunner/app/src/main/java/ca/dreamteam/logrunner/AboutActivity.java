package ca.dreamteam.logrunner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView buildText = (TextView) findViewById(R.id.build);
        buildText.setText("Version " + getString(R.string.version) + " Beta Build");

        TextView feedbackText = (TextView) findViewById(R.id.feedback);
        feedbackText.setTypeface(feedbackText.getTypeface(), Typeface.ITALIC);

        TextView problemText = (TextView) findViewById(R.id.problem);
        problemText.setTypeface(problemText.getTypeface(), Typeface.ITALIC);

        TextView sendFeedbackText = (TextView) findViewById(R.id.send_feedback);
        SpannableString content = new SpannableString(sendFeedbackText.getText());
        content.setSpan(new UnderlineSpan(), 0, sendFeedbackText.length(), 0);
        sendFeedbackText.setText(content);
        sendFeedbackText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"feedback@logrunner.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "User Feedback");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(AboutActivity.this, "There are no email clients installed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView reportProblemText = (TextView) findViewById(R.id.report_problem);
        content = new SpannableString(reportProblemText.getText());
        content.setSpan(new UnderlineSpan(), 0, reportProblemText.length(), 0);
        reportProblemText.setText(content);
        reportProblemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"bugtracker@logrunner.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "bug/problem report");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(AboutActivity.this, "There are no email clients installed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView termsText = (TextView) findViewById(R.id.terms_and_services);
        content = new SpannableString(termsText.getText());
        content.setSpan(new UnderlineSpan(), 0, termsText.length(), 0);
        termsText.setText(content);
        termsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, "Use this app in whatever way you want!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
