package com.example.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtAirTemp, txtAirHumidity;
    LabeledSwitch btnLed, btnFan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtAirTemp = findViewById(R.id.txtTemperature);
        txtAirHumidity = findViewById(R.id.txtHumidity);
        btnLed = findViewById(R.id.btnLed);
        btnFan = findViewById(R.id.btnFan);
//        btnFan.setEnabled(false);
//        btnLed.setEnabled(false);

        btnLed.setOnToggledListener((toggleableView, isOn) -> {
            if (isOn) {
                sendData("trungdai/feeds/led", "1");
            } else {
                sendData("trungdai/feeds/led", "0");
            }
        });
        btnFan.setOnToggledListener((toggleableView, isOn) -> {
            if (isOn) {
                sendData("trungdai/feeds/fan", "1");
            } else {
                sendData("trungdai/feeds/fan", "0");
            }
        });

        startMQTT();
    }

    public void sendData(String topic, String value) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(StandardCharsets.UTF_8);
        msg.setPayload(b);

        try {
            if (mqttHelper.mqttAndroidClient.isConnected()) {
                mqttHelper.mqttAndroidClient.publish(topic, msg);
                Log.d("TEST", "Published to " + topic);
            }
        } catch (MqttException e) {
        }

    }

    public void startMQTT() {
        mqttHelper = new MQTTHelper(this);
        MqttCallbackExtended mqttCallBack = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d("TEST", "Reconnect to " + serverURI);
                } else {
                    Log.d("TEST", "Connect to " + serverURI);
                }
//                btnFan.setEnabled(true);
//                btnLed.setEnabled(true);

            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("TEST", "The connection was lost.");
//                btnFan.setEnabled(false);
//                btnLed.setEnabled(false);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + "***" + message.toString());
                if (topic.contains("temp")) {
                    txtAirTemp.setText(message + "°C");
                } else if (topic.contains("humid")) {
                    txtAirHumidity.setText(message + "%");
                } else if (topic.contains("led")) {
                    if (message.toString().equals("1")) {
                        btnLed.setOn(true);
                    } else {
                        btnLed.setOn(false);
                    }
                } else if (topic.contains("fan")) {
                    if (message.toString().equals("1")) {
                        btnFan.setOn(true);
                    } else {
                        btnFan.setOn(false);
                    }
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("TEST", "Delivery complete " + token.toString());
            }
        };


        mqttHelper.setCallback(mqttCallBack);
    }
}