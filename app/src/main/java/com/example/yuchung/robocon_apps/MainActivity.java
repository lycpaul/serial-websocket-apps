package com.example.yuchung.robocon_apps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.URISyntaxException;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/**
 * @author Paul
 * @datetime 2017-04-06
 */

public class MainActivity extends AppCompatActivity {

    private String[] value_L_list = {"value1", "value2", "value3", "value4", "value5", "value6", "value7", "value8"};
    private String[] value_R_list = {"value9", "value10", "value11", "value12", "value13", "value14", "value15", "value16"};
    private String[] key_L_list = {"key1:", "key2:", "key3:", "key4:", "key5:", "key6:", "key7:", "key8:"};
    private String[] key_R_list = {"key9:", "key10:", "key11:", "key12:", "key13:", "key14:", "key15:", "key16:"};

    private WebSocketClient client;

    private TextView key_L;
    private TextView key_R;
    private TextView value_L;
    private TextView value_R;
    private TextView debug;
    private Button btncon;
    private ScrollView scroll;

    private String cat_string(String[] a) {
        String temp = "";
        for (int i = 0; i < a.length; i++)
            temp = temp + a[i] + "\n";
        return temp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        key_L = (TextView)findViewById(R.id.key_L);
        key_R = (TextView)findViewById(R.id.key_R);
        value_L = (TextView)findViewById(R.id.value_L);
        value_R = (TextView)findViewById(R.id.value_R);
        debug = (TextView)findViewById(R.id.debug);
        btncon = (Button)findViewById(R.id.btncon);
        scroll = (ScrollView)findViewById(R.id.scrollView);

        key_L.setText(cat_string(key_L_list));
        key_R.setText(cat_string(key_R_list));
        value_L.setText(cat_string(value_L_list));
        value_R.setText(cat_string(value_R_list));
        debug.setText("Debug Zone\n");

        System.setProperty("java.net.preferIPv4Stack", "true");

        btncon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (client != null) {
                    return;
                }
                try {
                    debug.append("Trying to connect to server...\n");
                    scroll.fullScroll(View.FOCUS_DOWN);
                    client = new WebSocketClient(new URI("ws://192.168.1.1:8887/"), new Draft_17()) {
                        @Override
                        public void onOpen(final ServerHandshake serverHandshake) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    debug.append("Connected to " + getURI() + "\n");
                                    scroll.fullScroll(View.FOCUS_DOWN);
                                }
                            });
                        }

                        @Override
                        public void onMessage(final String msg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String[] temp = msg.split(", ");
                                    if (temp[0].equals("VALUE")) {
                                        System.arraycopy(temp, 1, value_L_list, 0, 8);
                                        System.arraycopy(temp, 9, value_R_list, 0, 8);
                                        value_L.setText(cat_string(value_L_list));
                                        value_R.setText(cat_string(value_R_list));
                                    } else {
                                        debug.append("Get msg: " + msg + "\n");
                                        scroll.fullScroll(View.FOCUS_DOWN);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onClose(final int code, final String reason, final boolean remote) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    debug.append("Disconnected: " + getURI() + " code: " + code + " reason: " + reason + "\n");
                                    scroll.fullScroll(View.FOCUS_DOWN);
                                    client.close();
                                    client = null;
                                }
                            });
                        }

                        @Override
                        public void onError(final Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    debug.append("Error: " + e + "\n");
                                    scroll.fullScroll(View.FOCUS_DOWN);
                                }
                            });
                        }
                    };
                    try {
                        client.connect();
                    } catch (Exception ex) {
                        client = null;
                        ex.printStackTrace();
                        debug.append("Error in connection\n");
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                    debug.append("Invalid Websocket connection" + "\n");
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.close();
            client = null;
        }
    }

}
