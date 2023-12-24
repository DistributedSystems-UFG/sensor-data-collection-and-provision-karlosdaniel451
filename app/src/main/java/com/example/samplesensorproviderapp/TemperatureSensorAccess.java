package com.example.samplesensorproviderapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;

import java.nio.ByteBuffer;
import java.util.UUID;

public class TemperatureSensorAccess implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor mLight;
    private TextView sensor_field;
    private Mqtt5BlockingClient mqttClient;

    public TemperatureSensorAccess(SensorManager sm, TextView tv) {
        sensorManager = sm;
        sensor_field = tv;
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        this.mqttClient = MqttClient.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .useMqttVersion5()
                .buildBlocking();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The temperature sensor returns a single value in the Celsius unit.
        float temperatureInCelsius = event.values[0];

        // Show temperature value on the text field.
        sensor_field.setText(String.valueOf(temperatureInCelsius));

        try {
            Mqtt5PublishResult publishResult = mqttClient.publishWith()
                    .topic("celsius_temperature")
                    .contentType("text/plain")
                    .payload(ByteBuffer.allocate(4).putFloat(temperatureInCelsius).array())
                    .send();
        } catch (MqttClientStateException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void finalize() {
        sensorManager.unregisterListener(this);

        mqttClient.disconnect();
    }
}
