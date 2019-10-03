package com.example.mqtt;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static com.example.mqtt.App.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DEFAULT_HOST = "192.168.2.87";
    private static final String DEFAULT_PORT = "9001";
    private static final String DEFAULT_TOPIC = "test";
    private static final String DEFAULT_MESSAGE = "Hello World!";

    private Context context;
    private NotificationManagerCompat notificationManager;
    private int notificationId;
    private String clientId;
    private MqttAndroidClient client;
    private Button btnSubscribe;
    private Button btnUnsubscribe;
    private Button btnPublish;
    private EditText inputHost, inputPort, inputTopicSubscribe, inputTopicPublish, inputMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        notificationManager = NotificationManagerCompat.from(context);

        clientId = MqttClient.generateClientId();
        Log.d(TAG, "onCreate: Client ID: " + clientId);

        inputHost = findViewById(R.id.input_host);
        inputHost.setText(DEFAULT_HOST);

        inputPort = findViewById(R.id.input_port);
        inputPort.setText(DEFAULT_PORT);

        inputTopicSubscribe = findViewById(R.id.input_topic_subscribe);
        inputTopicSubscribe.setText(DEFAULT_TOPIC);

        inputTopicPublish = findViewById(R.id.input_topic_publish);
        inputTopicPublish.setText(DEFAULT_TOPIC);

        inputMessage = findViewById(R.id.input_message);
        inputMessage.setText(DEFAULT_MESSAGE);

        Button btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        btnSubscribe = findViewById(R.id.btn_subscribe);
        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribe();
            }
        });

        btnUnsubscribe = findViewById(R.id.btn_unsubscribe);
        btnUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unsubscribe();
            }
        });

        btnPublish = findViewById(R.id.btn_publish);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publish();
            }
        });
    }

    private void connect() {
        if (client == null) {
            client = new MqttAndroidClient(this, "ws://" + inputHost.getText().toString() + ":" + inputPort.getText().toString(), clientId);
            try {
                client.connect().setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("Connected to MQTT server")
                                .setContentText("ws://" + inputHost.getText().toString() + ":" + inputPort.getText().toString())
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
                        notificationManager.notify(notificationId++, builder.build());
                        btnSubscribe.setEnabled(true);
                        btnUnsubscribe.setEnabled(true);
                        btnPublish.setEnabled(true);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                        e.printStackTrace();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void disconnect() {
        try {
            client.unregisterResources();
            client.close();
            client.disconnect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: Disconnected from MQTT server");
                    client.setCallback(null);
                    client = null;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        IMqttMessageListener iMqttMessageListener = new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Received message from topic - " + topic)
                        .setContentText(message.toString())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);
                notificationManager.notify(notificationId++, builder.build());
            }
        };
        try {
            client.subscribe(inputTopicSubscribe.getText().toString(), 0, iMqttMessageListener);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Subscribed to topic")
                    .setContentText(inputTopicSubscribe.getText().toString())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            notificationManager.notify(notificationId++, builder.build());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribe() {
        try {
            IMqttToken unsubToken = client.unsubscribe(inputTopicSubscribe.getText().toString());
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Unsubscribed from topic")
                            .setContentText(inputTopicSubscribe.getText().toString())
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE);
                    notificationManager.notify(notificationId++, builder.build());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publish() {
        byte[] payload = inputMessage.getText().toString().getBytes();
        MqttMessage message = new MqttMessage(payload);
        try {
            client.publish(inputTopicPublish.getText().toString(), message);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Published to topic - " + inputTopicPublish.getText().toString())
                    .setContentText(inputMessage.getText().toString())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            notificationManager.notify(notificationId++, builder.build());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}
