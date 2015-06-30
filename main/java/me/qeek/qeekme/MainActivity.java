package me.qeek.qeekme;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {

    EditText textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (EditText) findViewById(R.id.editText);
    }

    public void pressButton(View view) {
        final Wocket wocket = new Wocket();

        wocket.On("error", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                if(args != null && args.length > 0)
                    log("Error: " + args[0]);
                else
                    log("Unknown error.");
            }
        });

        wocket.On("close", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                log("Connection closed");
            }
        });

        wocket.On("connected", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                log("Connected!");

                wocket.Emit("createSession");
            }
        });

        wocket.On("sessionCreated", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                //If args doesn't match the requested, return
                if(args == null != args.length < 1) return;

                String room = args[0];

                wocket.Emit("joinSession", "127.0.0.1", room, "LucasAndroid", "mobile");
            }
        });

        wocket.On("sessionJoined", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                log("Session Joined");
            }
        });

        wocket.On("joinError", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                //If args doesn't match the requested, return
                if(args == null != args.length < 1) return;

                log("Error while joining session: " + args[0]);
            }
        });

        wocket.On("NewDevice", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                log("New device:");
                for(String arg : args)
                    log(arg);
            }
        });

        wocket.On("peerDataError", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                log("PeerDataError:");
                for(String arg : args)
                    log(arg);
            }
        });


        wocket.On("peerClosure", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                log("PeerClosure:");
                for(String arg : args)
                    log(arg);
            }
        });

        wocket.On("peerData", new EventHandler() {
            @Override
            public void Fire(String[] args) {
                log("PeerData:");
                for(String arg : args)
                    log(arg);
            }
        });

        log("Connecting...");

        wocket.Connect("ws://192.168.0.109:6002");
    }

    void log(final String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(textView.getText() + "\n" + msg);
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
}
