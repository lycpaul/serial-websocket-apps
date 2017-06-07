package com.example.yuchung.robocon_apps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.http.entity.ByteArrayEntity;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

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

    private PS4 ps4 = new PS4();

    private String cat_string(String[] a) {
        cat_string_temp = "";
        for (int i = 0; i < a.length; i++)
            cat_string_temp = cat_string_temp + a[i] + "\n";
        return cat_string_temp;
    }

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
                    client = new WebSocketClient(new URI("ws://192.168.0.100:8887/"), new Draft_17()) {
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
                                    //receive data
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

                                    //send data
                                    try {
                                        client.send(ps4.data_byte());
                                    } catch (Exception ex) {
                                        debug.append("Exception in sending msg" + "\n");
                                        scroll.fullScroll(View.FOCUS_DOWN);
                                    }
                                }
                            });
                        }

//                        @Override
//                        public void onMessage(final ByteBuffer msg) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    //receive data
//                                    try {
//                                        for(int i=0 ; i<8 ; i++){
//                                            value_L_list[i] = Integer.toString(msg.getInt());
//                                        }
//                                        for(int i=0 ; i<8 ; i++){
//                                            value_R_list[i] = Integer.toString(msg.getInt());
//                                        }
//                                    } catch (Exception ex){
//                                        debug.append("ByteBuffer onMessage error\n");
//                                        scroll.fullScroll(View.FOCUS_DOWN);
//                                    }
//
//                                    //send data
//                                    try {
//                                        client.send(ps4.data_byte());
//                                    } catch (Exception ex) {
//                                        debug.append("Exception in sending msg" + "\n");
//                                        scroll.fullScroll(View.FOCUS_DOWN);
//                                    }
//                                }
//                            });
//                        }

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

    @Override
    public boolean onGenericMotionEvent(final MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                && event.getAction() == MotionEvent.ACTION_MOVE) {
            //for joystick data
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InputDevice mInputDevice = event.getDevice();
                    ps4.x = (byte) Math.round(Byte.MAX_VALUE * getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_X, -1));

                    ps4.y = (byte) Math.round(Byte.MAX_VALUE * getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_Y, -1));

                    ps4.x_r = (byte) Math.round(Byte.MAX_VALUE * getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_Z, -1));

                    ps4.y_r = (byte) Math.round(Byte.MAX_VALUE * getCenteredAxis(event, mInputDevice,
                            MotionEvent.AXIS_RZ, -1));

                    if (event.getAxisValue(MotionEvent.AXIS_HAT_Y) == -1) {
                        if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == 1) ps4.dpad_code = 1;
                        else if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == 0) ps4.dpad_code = 0;
                        else if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == -1)
                            ps4.dpad_code = 7;
                    } else if (event.getAxisValue(MotionEvent.AXIS_HAT_Y) == 0) {
                        if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == 1) ps4.dpad_code = 2;
                        else if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == 0) ps4.dpad_code = 8;
                        else if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == -1)
                            ps4.dpad_code = 6;
                    } else if (event.getAxisValue(MotionEvent.AXIS_HAT_Y) == 1) {
                        if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == 1) ps4.dpad_code = 3;
                        else if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == 0) ps4.dpad_code = 4;
                        else if (event.getAxisValue(MotionEvent.AXIS_HAT_X) == -1)
                            ps4.dpad_code = 5;
                    }

                    ps4.l2_trigger = (byte) Math.round(Byte.MAX_VALUE * event.getAxisValue(MotionEvent.AXIS_RX));
                    ps4.r2_trigger = (byte) Math.round(Byte.MAX_VALUE * event.getAxisValue(MotionEvent.AXIS_RY));

                    ps4.tpad_x = (byte) Math.round(254 * event.getAxisValue(MotionEvent.AXIS_GENERIC_2) - 127);
                    ps4.tpad_y = (byte) Math.round(254 * event.getAxisValue(MotionEvent.AXIS_GENERIC_3) - 127);
                }
            });
            return true;
        } else {
            debug.append("Motion event: " + event.getAction() + " " + event.getSource() + "\n");
            scroll.fullScroll(View.FOCUS_DOWN);
            return super.onGenericMotionEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (keyCode == 99) {
                    ps4.triangle = 1;
                } else if (keyCode == 98) {
                    ps4.circle = 1;
                } else if (keyCode == 97) {
                    ps4.cross = 1;
                } else if (keyCode == 96) {
                    ps4.square = 1;
                } else if (keyCode == 100) {
                    ps4.l1 = 1;
                } else if (keyCode == 101) {
                    ps4.r1 = 1;
                } else if (keyCode == 102) {
                    ps4.l2 = 1;
                } else if (keyCode == 103) {
                    ps4.r2 = 1;
                } else if (keyCode == 109) {
                    ps4.l3 = 1;
                } else if (keyCode == 108) {
                    ps4.r3 = 1;
                } else if (keyCode == 106) {
                    ps4.tpad_click = 1;
                } else if (keyCode == 104) {
                    ps4.share = 1;
                } else if (keyCode == 105) {
                    ps4.options = 1;
                } else if (keyCode == 110) {
                    ps4.ps = 1;
                } else {
                    debug.append("KeyDown event: " + event.getAction() + " " + event.getSource() + "\n");
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (keyCode == 99) {
                    ps4.triangle = 0;
                } else if (keyCode == 98) {
                    ps4.circle = 0;
                } else if (keyCode == 97) {
                    ps4.cross = 0;
                } else if (keyCode == 96) {
                    ps4.square = 0;
                } else if (keyCode == 100) {
                    ps4.l1 = 0;
                } else if (keyCode == 101) {
                    ps4.r1 = 0;
                } else if (keyCode == 102) {
                    ps4.l2 = 0;
                } else if (keyCode == 103) {
                    ps4.r2 = 0;
                } else if (keyCode == 109) {
                    ps4.l3 = 0;
                } else if (keyCode == 108) {
                    ps4.r3 = 0;
                } else if (keyCode == 106) {
                    ps4.tpad_click = 0;
                } else if (keyCode == 104) {
                    ps4.share = 0;
                } else if (keyCode == 105) {
                    ps4.options = 0;
                } else if (keyCode == 110) {
                    ps4.ps = 0;
                } else {
                    debug.append("KeyDown event: " + event.getAction() + " " + event.getSource() + "\n");
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
        return true;
    }

    public class PS4 {
        private byte x = 0;     //AXIS_X
        private byte y = 0;     //AXIS_Y
        private byte x_r = 0;   //AXIS_Z
        private byte y_r = 0;   //AXIS_RZ

        private byte package1 = 0;
        private byte dpad_code = 8; //AXIS_HAT_X, AXIS_HAT_Y, 4 bit
        private byte square = 0;    //1 bit
        private byte cross = 0;     //1 bit
        private byte circle = 0;    //1 bit
        private byte triangle = 0;  //1 bit

        private byte package2 = 0;
        private byte l1 = 0;
        private byte r1 = 0;
        private byte l2 = 0;
        private byte r2 = 0;
        private byte l3 = 0;
        private byte r3 = 0;
        private byte ps = 0;
        private byte tpad_click = 0;

        private byte l2_trigger = -127;    //AXIS_RX
        private byte r2_trigger = -127;    //AXIS_RY

        private byte tpad_x = 0;    //AXIS_GENERIC_2
        private byte tpad_y = 0;    //AXIS_GENERIC_3

        private byte package3 = 0;
        private byte options = 0;   //1 bit
        private byte share = 0;     //1 bit
        private byte commands = 0;  //6

        public String data_string() {
            pack();
            return Byte.toString(x) + Byte.toString(y)
                    + Byte.toString(x_r) + Byte.toString(y_r)
                    + Byte.toString(package1) + Byte.toString(package2)
                    + Byte.toString(l2_trigger) + Byte.toString(r2_trigger)
                    + Byte.toString(tpad_x) + Byte.toString(tpad_y)
                    + Byte.toString(package3);
        }

        public ByteBuffer data_byte() {
//            pack();
            return ByteBuffer.wrap(new byte[]{
                    0x53, x, y, x_r, y_r,
                    triangle, circle, cross, square, dpad_code,
                    tpad_click, ps, r3, l3, r2,
                    l2, r1, l1, l2_trigger, r2_trigger,
                    tpad_x, tpad_y, commands, share, options});
        }

        public void display() {
            pack();
            value_L_list[0] = Byte.toString(x);
            value_L_list[1] = Byte.toString(y);
            value_L_list[2] = Byte.toString(x_r);
            value_L_list[3] = Byte.toString(y_r);
            value_L_list[4] = Short.toString(dpad_code);
            value_L_list[5] = Byte.toString(tpad_click);
            value_L_list[6] = Byte.toString(l2_trigger);
            value_L_list[7] = Byte.toString(r2_trigger);

            value_R_list[0] = Byte.toString(tpad_x);
            value_R_list[1] = Byte.toString(tpad_y);
            value_R_list[2] = Byte.toString(package1);
            value_R_list[3] = Byte.toString(package2);
            value_R_list[4] = Byte.toString(package3);
            value_R_list[5] = Byte.toString(l1);
            value_R_list[6] = Byte.toString(r2);
            value_R_list[7] = Byte.toString(options);

            value_L.setText(cat_string(value_L_list));
            value_R.setText(cat_string(value_R_list));
        }

        public void pack() {
            package1 = (byte) (dpad_code << 4);
            package1 = (byte) (package1 | (byte) (square << 3));
            package1 = (byte) (package1 | (byte) (cross << 2));
            package1 = (byte) (package1 | (byte) (circle << 1));
            package1 = (byte) (package1 | triangle);

            package2 = tpad_click;
            package2 = (byte) (package2 | (byte) (l1 << 7));
            package2 = (byte) (package2 | (byte) (r1 << 6));
            package2 = (byte) (package2 | (byte) (l2 << 5));
            package2 = (byte) (package2 | (byte) (r2 << 4));
            package2 = (byte) (package2 | (byte) (l3 << 3));
            package2 = (byte) (package2 | (byte) (r3 << 2));
            package2 = (byte) (package2 | (byte) (ps << 1));

            package3 = commands;
            package3 = (byte) (package3 | (byte) (options << 7));
            package3 = (byte) (package3 | (byte) (share << 6));
        }
    }
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

    uint8_t tpad_x;
    uint8_t tpad_y;

    uint8_t options:1;
    uint8_t share:1;
} Rx_data_ps4;*/
