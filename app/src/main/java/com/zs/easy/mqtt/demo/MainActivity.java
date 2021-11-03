package com.zs.easy.mqtt.demo;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import com.zs.easy.mqtt.EasyMqttService;
import com.zs.easy.mqtt.IEasyMqttCallBack;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * @author zhangshun
 */
public class MainActivity extends Activity {
    final String TAG = this.getClass().getSimpleName();

    private EasyMqttService mqttService;
    /**
     * 回调时使用
     */
    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt_dial = (Button) findViewById(R.id.button);
        bt_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttPublishQueryZigBeeVersion();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }

        buildEasyMqttService();

        connect();

        if (isConnected()) {
            //消息内容
            String msg = "{\\\"cmd\\\":\\\"network\\\",\\\"reqid\\\":3,\\\"args\\\":{\\\"mType\\\":1}}";
            //消息主题
            String topic = "/ayla/zigbee/down/commands";
            //消息策略
            int qos = 2;
            //是否保留
            boolean retained = false;
            //发布消息
            publish(msg, topic, qos, retained);
        }

//        disconnect();
//        close();

    }

    public void mqttPublishQueryZigBeeVersion() {
        Log.d(TAG, "### mqttPublishQueryZigBeeVersion!");

        //消息内容
        String msg = "{\"cmd\":\"network\",\"reqid\":3,\"args\":{\"mType\":1}}";
        //消息主题
        String topic = "/ayla/zigbee/down/commands";
        //消息策略
        int qos = 2;
        //是否保留
        boolean retained = false;
        //发布消息
        publish(msg, topic, qos, retained);
    }

    /**
     * 判断服务是否连接
     */
    private boolean isConnected() {
        return mqttService.isConnected();
    }

    /**
     * 发布消息
     */
    private void publish(String msg, String topic, int qos, boolean retained) {
        mqttService.publish(msg, topic, qos, retained);
    }

    /**
     * 断开连接
     */
    private void disconnect() {
        mqttService.disconnect();
    }

    /**
     * 关闭连接
     */
    private void close() {
        mqttService.close();
    }

    /**
     * 订阅主题 这里订阅三个主题分别是"a", "b", "c"
     */
    private void subscribe() {
        String[] topics = new String[]{"/ayla/zigbee/up/network"};
        //主题对应的推送策略 分别是0, 1, 2 建议服务端和客户端配置的主题一致
        // 0 表示只会发送一次推送消息 收到不收到都不关心
        // 1 保证能收到消息，但不一定只收到一条
        // 2 保证收到切只能收到一条消息
        int[] qoss = new int[]{2};
        mqttService.subscribe(topics, qoss);
    }

    /**
     * 连接Mqtt服务器
     */
    private void connect() {
        mqttService.connect(new IEasyMqttCallBack() {
            @Override
            public void messageArrived(String topic, String message, int qos) {
                Log.d(TAG, "### message = " + message);
                Log.d(TAG, "### topic = " + topic);
                Log.d(TAG, "### qos = " + qos);
                //推送消息到达
            }

            @Override
            public void connectionLost(Throwable arg0) {
                //连接断开
                Log.d(TAG, "### connectionLost!");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {
                Log.d(TAG, "### deliveryComplete!");
            }

            @Override
            public void connectSuccess(IMqttToken arg0) {
                //连接成功 订阅一次即可 订阅状态可以保存到sp 这里简单处理了
                Log.d(TAG, "### subscribe!");
                subscribe();
            }

            @Override
            public void connectFailed(IMqttToken arg0, Throwable arg1) {
                //连接失败
                Log.d(TAG, "### connectFailed!");
            }
        });
    }

    /**
     * 构建EasyMqttService对象
     */
    private void buildEasyMqttService() {
        mqttService = new EasyMqttService.Builder()
                //设置自动重连
                .autoReconnect(true)
                //设置不清除回话session 可收到服务器之前发出的推送消息
                .cleanSession(false)
                //唯一标示 保证每个设备都唯一就可以 建议 imei
                .clientId("test")
                //mqtt服务器地址 格式例如：tcp://10.0.261.159:1883
                .serverUrl("tcp://127.0.0.1:1883")
                //心跳包默认的发送间隔
                .keepAliveInterval(60)
                //构建出EasyMqttService 建议用application的context
                .bulid(this.getApplicationContext());
    }
}
