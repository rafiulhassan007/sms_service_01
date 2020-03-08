package com.rafiul.sms_sender_service;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView smsView;
    private Button btn_send;
    private final static int PERMISSION_REQUEST_SEND_SMS = 0;
    private final static int PERMISSION_REQUEST_PHONE_STATE = 999;
    private SmsContents smsContents;
    private String SENT = "SMS_Sent";
    private String DELIVERED = "SMS_Delivered";
    private PendingIntent sentPI, deliverPI;
    private BroadcastReceiver sentReceiver, deliveredReceiver;
    private List<SubscriptionInfo> subscriptionInfoList;
    private EditText number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //check permission not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            //check if user denied
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

            } else {
                //show permission request dialog
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
            }
        }

        getSubscriptionList();

        setSMS();
        initView();

        sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(SENT), 0);
        deliverPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(DELIVERED), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {

                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
                        break;

                    //Something went wrong and there's no way to tell what, why or how.
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure!", Toast.LENGTH_SHORT).show();
                        break;

                    //Your device simply has no cell reception. You're probably in the middle of
                    //nowhere, somewhere inside, underground, or up in space.
                    //Certainly away from any cell phone tower.
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service!", Toast.LENGTH_SHORT).show();
                        break;

                    //Something went wrong in the SMS stack, while doing something with a protocol
                    //description unit (PDU) (most likely putting it together for transmission).
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU!", Toast.LENGTH_SHORT).show();
                        break;

                    //You switched your device into airplane mode, which tells your device exactly
                    //"turn all radios off" (cell, wifi, Bluetooth, NFC, ...).
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off!", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered!", Toast.LENGTH_SHORT).show();
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered!", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        };

        registerReceiver(sentReceiver, new IntentFilter(SENT));
        registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));
    }

    private void setSMS() {
        smsContents = new SmsContents("Hi, Test", "01811549195");
    }

    private void initView() {
        smsView = (TextView) findViewById(R.id.smsView);
        btn_send = (Button) findViewById(R.id.btn_send);
        number = (EditText) findViewById(R.id.number);

        smsView.setText(smsContents.getNumber() + "\n\n" + smsContents.getText());

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smsContents.setNumber(number.getText().toString().trim());
                subscriptionSelector();
                //sendSMS();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_SEND_SMS: {
                //check grant result is greater than 0 and equal to PERMISSION_GRANTED
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Thanks for permitting ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Why you denied! LOL", Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSION_REQUEST_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSubscriptionList();
                    Toast.makeText(this, "Thanks for permitting ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Why you denied! LOL", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void sendSMS(int subscriptionId) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.getSmsManagerForSubscriptionId(subscriptionId).sendTextMessage(smsContents.getNumber(), null, smsContents.getText(), sentPI, deliverPI);
//        SmsManager.getSmsManagerForSubscriptionId(subscriptionId).sendTextMessage(String destinationAddress, String scAddress, String text,PendingIntent sentIntent, PendingIntent deliveryIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveredReceiver);
    }

    private void getSubscriptionList() {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_PHONE_STATE);
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
    }

    private void subscriptionSelector() {
        if (subscriptionInfoList != null && subscriptionInfoList.size() > 0) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
            builderSingle.setTitle("Select One");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.select_dialog_item);

            for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                arrayAdapter.add(subscriptionInfo.getCarrierName().toString());
            }
            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    String strName = arrayAdapter.getItem(which);
//                    Log.d("=====", "onClick: " + strName);
                    int subscriptionId = subscriptionInfoList.get(which).getSubscriptionId();
                    sendSMS(subscriptionId);
                }
            });
            builderSingle.show();
        }
    }

}
