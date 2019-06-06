package com.myapplicationdev.android.p07_smsretriever;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentWord extends Fragment {

    Button btnRetrieve, btnEmail;
    TextView tvDisplay;
    EditText etWord;
    String smsBody;

    public FragmentWord() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_word, container, false);

        tvDisplay = view.findViewById(R.id.tvDisplay);
        btnRetrieve = view.findViewById(R.id.btnRetrieve);
        etWord = view.findViewById(R.id.etWord);
        btnEmail = view.findViewById(R.id.btnEmail);

        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = etWord.getText().toString();
                if (word.equalsIgnoreCase("")){
                    Toast.makeText(getContext(), "ITS A MUST to fill in the word", Toast.LENGTH_LONG).show();
                }else{
                    Uri uri = Uri.parse("content://sms");
                    String[] reqCols = new String[]{"date", "address", "body", "type"};
                    String filter = "body LIKE ?";
                    String[] arg =  {"%" + word + "%"};
                    ContentResolver cr = getActivity().getContentResolver();
                    Cursor cursor = cr.query(uri, reqCols, filter, arg, null);
                    if (word.contains(" ")){
                        String[] words = word.split(" ");
                        String[] args = new String[words.length];
                        args[0] = "%" + words[0] + "%";
                        for (int i = 1; i<(words.length); i++){
                            filter += "and body LIKE ?";
                            String fil = "%" + words[i] + "%";
                            args[i] = fil;
                        }

                        cursor = cr.query(uri, reqCols, filter, args, null);
                    }

                    smsBody = "";
                    if (cursor.moveToFirst()){
                        do{
                            android.text.format.DateFormat df = new android.text.format.DateFormat();
                            long dateInMillis = cursor.getLong(0);
                            String date = (String)df.format("dd MMM yyyy h:mm:ss aa", dateInMillis);

                            String address = cursor.getString(1);
                            String body = cursor.getString(2);
                            String type = cursor.getString(3);
                            if (type.equalsIgnoreCase("1")){
                                type = "Inbox:";
                            }else{
                                type = "Sent:";
                            }
                            smsBody += type + " " + address + "\n at " + date + "\n\"" + body + "\"\n\n";

                        }while (cursor.moveToNext());
                    }
                    tvDisplay.setText(smsBody);
                }
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (smsBody.equalsIgnoreCase("")){
                    Toast.makeText(getContext(), "Please have the SMS content before sending:)", Toast.LENGTH_LONG).show();
                }else{
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{"17010411@myrp.edu.sg"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "SMS contents");
                    email.putExtra(Intent.EXTRA_TEXT, smsBody);

                    email.setType("message/rfc822");
                    startActivity(Intent.createChooser(email, "Choose an Email client: "));
                }
            }
        });

        return view;
    }

}
