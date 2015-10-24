package com.example.nanochat;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseListAdapter;

public class MainActivity extends ListActivity {

    private String mUsername;
    private Firebase mFireBaseRef;
    private FirebaseListAdapter<ChatMessage> mFireBaseListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);
        mFireBaseRef = new Firebase("https://amber-inferno-2543.firebaseio.com");
        final EditText editText = (EditText) findViewById(R.id.text_edit);
        final Button send = (Button) findViewById(R.id.buttton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = editText.getText().toString();
                final ChatMessage message = new ChatMessage(MainActivity.this.mUsername, text);
                mFireBaseRef.push().setValue(message);
                editText.setText("");
            }
        });
        mFireBaseListAdapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, android.R.layout.two_line_list_item, mFireBaseRef) {
            @Override
            protected void populateView(View view, ChatMessage chatMessage) {
                final TextView userName = (TextView) view.findViewById(android.R.id.text1);
                final TextView userMessage = (TextView) view.findViewById(android.R.id.text2);
                userName.setText(chatMessage.getName());
                userMessage.setText(chatMessage.getText());
                getListView().smoothScrollToPosition(getListView().getMaxScrollAmount());
            }
        };
        setListAdapter(mFireBaseListAdapter);

        final Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Enter your email address and password")
                        .setTitle("Login")
                        .setView(MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_signin, null))
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final AlertDialog dlg = (AlertDialog) dialog;
                                final String email = ((TextView) dlg.findViewById(R.id.email)).getText().toString();
                                final String password = ((TextView) dlg.findViewById(R.id.password)).getText().toString();
                                mFireBaseRef.createUser(email, password, new Firebase.ResultHandler() {
                                    @Override
                                    public void onSuccess() {
                                        mFireBaseRef.authWithPassword(email, password, null);
                                    }

                                    @Override
                                    public void onError(FirebaseError firebaseError) {
                                        mFireBaseRef.authWithPassword(email, password, null);
                                    }
                                });
                            }
                        }).create().show();
            }
        });

        final Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFireBaseRef.unauth();
                findViewById(R.id.login).setVisibility(View.VISIBLE);
            }
        });

        mFireBaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if(authData != null) {
                    mUsername = (String) authData.getProviderData().get("email");
                    findViewById(R.id.login).setVisibility(View.INVISIBLE);
                    findViewById(R.id.logout).setVisibility(View.VISIBLE);
                } else {
                    mUsername = "Guest";
                    findViewById(R.id.login).setVisibility(View.VISIBLE);
                    findViewById(R.id.logout).setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFireBaseListAdapter.cleanup();
    }
}
