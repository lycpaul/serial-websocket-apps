package com.example.yuchung.robocon_apps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

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

    private String[] read_buff;
    private String cat_string_temp;
    private String write_buff = "";

    private String cat_string(String[] a) {
        cat_string_temp = "";
        for (int i = 0; i < a.length; i++)
            cat_string_temp = cat_string_temp + a[i] + "\n";
        return cat_string_temp;
    }

    /*typedef struct __attribute__ ((packed)) {
            uint8_t hat_left_x;
            uint8_t hat_left_y;
            uint8_t hat_right_x;
            uint8_t hat_right_y;

            uint8_t dpad_code :4;
            uint8_t square :1;
            uint8_t cross :1;
            uint8_t circle :1;
            uint8_t triangle :1;

            uint8_t l1 :1;
            uint8_t r1 :1;
            uint8_t l2 :1;
            uint8_t r2 :1;
            uint8_t l3 :1;
            uint8_t r3 :1;
            uint8_t ps :1;
            uint8_t tpad_click :1;

            uint8_t l2_trigger;
            uint8_t r2_trigger;
        } Rx_data;*/
    private byte[] ps4_data = {0,0,0,0,0,0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        key_L = (TextView) findViewById(R.id.key_L);
        key_R = (TextView) findViewById(R.id.key_R);
        value_L = (TextView) findViewById(R.id.value_L);
        value_R = (TextView) findViewById(R.id.value_R);
        debug = (TextView) findViewById(R.id.debug);
        btncon = (Button) findViewById(R.id.btncon);
        scroll = (ScrollView) findViewById(R.id.scrollView);

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
                    client = new WebSocketClient(new URI("ws://192.168.1.100:8887/"), new Draft_17()) {
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
                                    read_buff = msg.split(", ");
                                    if (read_buff[0].equals("VALUE")) {
                                        System.arraycopy(read_buff, 1, value_L_list, 0, 8);
                                        System.arraycopy(read_buff, 9, value_R_list, 0, 8);
                                        value_L.setText(cat_string(value_L_list));
                                        value_R.setText(cat_string(value_R_list));
                                    } else {
                                        debug.append("Get msg: " + msg + "\n");
                                        scroll.fullScroll(View.FOCUS_DOWN);
                                    }
                                    write_buff = Byte.toString(ps4_data[0]) + "," +
                                            Byte.toString(ps4_data[1]) + "," +
                                            Byte.toString(ps4_data[2]) + "," +
                                            Byte.toString(ps4_data[3]);
                                    try {
                                        client.send(write_buff);
                                    } catch (Exception ex) {
                                        debug.append("Exception in sending msg" + "\n");
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
                                    debug.append("Disconnected: " + getURI() + " code: " + code +
                                            " reason: " + reason + "\n");
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

    @Override
    public boolean onGenericMotionEvent(final MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
        && event.getAction() == MotionEvent.ACTION_MOVE) {
            //for joystick data
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InputDevice mInputDevice = event.getDevice();
                    ps4_data[0] = (byte)Math.round(Byte.MAX_VALUE*getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_X, -1));

                    ps4_data[1] = (byte)Math.round(Byte.MAX_VALUE*getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_Y, -1));

                    ps4_data[2] = (byte)Math.round(Byte.MAX_VALUE*getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_Z, -1));

                    ps4_data[3] = (byte)Math.round(Byte.MAX_VALUE*getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_RZ, -1));
                    value_L_list[0] = Byte.toString(ps4_data[0]);
                    value_L_list[1] = Byte.toString(ps4_data[1]);
                    value_L_list[2] = Byte.toString(ps4_data[2]);
                    value_L_list[3] = Byte.toString(ps4_data[3]);
                    value_L.setText(cat_string(value_L_list));
                }
            });
            return true;
        } else if ((event.getSource() & InputDevice.SOURCE_DPAD) != InputDevice.SOURCE_DPAD) {
            //for dpad
            debug.append("get dpad" + "\n");
            scroll.fullScroll(View.FOCUS_DOWN);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    KeyEvent keyEvent = (KeyEvent) (InputEvent) event;
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        ps4_data[4] = 0;
                    } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        ps4_data[4] = 2;
                    } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        ps4_data[4] = 4;
                    } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                        ps4_data[4] = 6;
                    }
                    value_L_list[4] = Byte.toString(ps4_data[4]);
                    value_L.setText(cat_string(value_L_list));
                }
            });
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    private float getCenteredAxis(MotionEvent event,
                                  InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());
        // debouncing
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis) :
                            event.getHistoricalAxisValue(axis, historyPos);
            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }
}
