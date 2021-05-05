/*************************************************************************
 *
 * ATOS CONFIDENTIAL
 * __________________
 *
 *  Copyright (2020) Atos Spain SA
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Atos Spain SA and other companies of the Atos group.
 * The intellectual and technical concepts contained
 * herein are proprietary to Atos Spain SA
 * and other companies of the Atos group and may be covered by Spanish regulations
 * and are protected by copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Atos Spain SA.
 */
package com.helios.helios_media_module.messaging;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.helios.helios_media_module.MainActivity;
import com.helios.helios_media_module.dto.MessageDTO;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.h2020.helios_social.core.messaging.HeliosConnectionInfo;
import eu.h2020.helios_social.core.messaging.HeliosIdentityInfo;
import eu.h2020.helios_social.core.messaging.HeliosMessage;
import eu.h2020.helios_social.core.messaging.HeliosMessageListener;
import eu.h2020.helios_social.core.messaging.HeliosTopic;
import eu.h2020.helios_social.core.messaging.ReliableHeliosMessagingNodejsLibp2pImpl;
import eu.h2020.helios_social.core.messaging_nodejslibp2p.HeliosEgoTag;
import eu.h2020.helios_social.core.messaging_nodejslibp2p.HeliosMessagingReceiver;
import eu.h2020.helios_social.core.messaging_nodejslibp2p.HeliosNetworkAddress;
import eu.h2020.helios_social.core.profile.HeliosProfileManager;
import eu.h2020.helios_social.core.profile.HeliosUserData;
import eu.h2020.helios_social.core.security.HeliosCryptoManager;
import eu.h2020.helios_social.core.security.HeliosKeyStoreException;
import eu.h2020.helios_social.core.security.HeliosKeyStoreManager;

public class HeliosMessagingService implements HeliosMessagingReceiver, HeliosMessageListener {
    private static final String TOPIC = "mediastreaming";
    private static final HeliosTopic HELIOS_TOPIC = new HeliosTopic(TOPIC, "h3l10sM3d14");
    private static final String USER_ID = "eu.h2020.helios_social.USER_ID";
    private static final String USERNAME = "eu.h2020.helios_social.USERNAME";
    public static final String VIDEOCALL_ROOM_KEY = "videocallRoom";
    public static final String FILETRANSFER_URL_KEY = "filetransferUrl";
    public static final String MESSAGE_KEY_VALUE_SEPARATOR = ":";
    public static final String PASS_PHRASE = "1234";
    public static final String LOG_TAG = "HeliosMessagingService";
    public static final String PROTOCOL_ID = "/helios/chat/proto/0.0.1";
    public static final String MESSAGE_PAIR_KEY_VALUE_SEPARATOR = ";";
    public static final String VIDEOCALL_TURN_URL_KEY = "turnUrl";
    public static final String VIDEOCALL_TURN_USER_KEY = "turnUser";
    public static final String VIDEOCALL_TURN_CREDENTIAL_KEY = "turnCredential";
    public static final String VIDEOCALL_STUN_URL_KEY = "stunUrl";
    public static final String VIDEOCALL_API_ENDPOINT_KEY = "apiEndpoint";

    private ReliableHeliosMessagingNodejsLibp2pImpl heliosMessagingNodejs;
    private HeliosKeyStoreManager heliosSecurityKeyStore;

    private MainActivity activity;

    private static final String MODULE_NAME_FILETRANSFER = "FileTransfer";
    private static final String MODULE_NAME_VIDEOCALL = "VideoCall";

    public HeliosMessagingService(MainActivity activity) {
        this.activity = activity;
        initHeliosProfile(activity);
        initHeliosMessaging(activity);
        //initHeliosSecurity(activity);
    }

    public void publishToTopic(String message) {
        try {
            heliosMessagingNodejs.publish(HELIOS_TOPIC, new HeliosMessage(message));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Helios Messaging - Publish Exception: " + e.toString(), e);
        }
    }

    public void publishToDirect(String message) {
        /*
        try {
            PrivateKey signingKey = heliosSecurityKeyStore.retrievePrivateKey("USER SIGNING KEY", PASS_PHRASE);
            PrivateKey decryptingKey = heliosSecurityKeyStore.retrievePrivateKey("USER DECRYPTING KEY", PASS_PHRASE);
        } catch (HeliosKeyStoreException e) {
            Log.e(LOG_TAG, "Helios Security - KeyStore Exception: " + e.toString(), e);
        }
        */
        try {
            Log.i(LOG_TAG, "Helios Messaging - Publish Direct PeerId: " + heliosMessagingNodejs.getPeerId());
            Log.i(LOG_TAG, "Helios Messaging - Publish Direct EgoTags: " + heliosMessagingNodejs.getTags().size());
            for (HeliosEgoTag egoTag : heliosMessagingNodejs.getTags()) {
                Log.i(LOG_TAG, "Helios Messaging - Publish Direct EgoTag NetworkId: '" + egoTag.getNetworkId() + "' (" + egoTag.getTag() + ")");
            }

            Optional<HeliosEgoTag> heliosEgoTag = heliosMessagingNodejs.getTags().stream().filter(tag -> !heliosMessagingNodejs.getPeerId().equals(tag.getNetworkId())).findAny();
            if (heliosEgoTag.isPresent()) {
                HeliosNetworkAddress heliosNetworkAddress = new HeliosNetworkAddress();
                heliosNetworkAddress.setNetworkId(heliosEgoTag.get().getNetworkId());
                heliosMessagingNodejs.getDirectMessaging().sendToFuture(heliosNetworkAddress, PROTOCOL_ID, message.getBytes(StandardCharsets.UTF_8));
            } else {
                Log.i(LOG_TAG, "Helios Messaging - Publish Direct not sent because not exists HeliosEgoTag for receiver");
            }
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "Helios Messaging - Publish Direct Exception: " + e.toString(), e);
        }
    }

    @Override
    public void showMessage(HeliosTopic heliosTopic, HeliosMessage heliosMessage) {
        String message = heliosMessage.getMessage();
        Log.i(LOG_TAG, "HeliosMessageListener - showMessage: '" + message + "' (Topic: '" + heliosTopic.getTopicName() + "')");
        processMediastreamingMessage(message);
    }
/*
    private void processMediastreamingMessage(String message) {
        boolean isCall = false;
        int b = message.indexOf(VIDEOCALL_ROOM_KEY);
        if (b >= 0) {   //found
            String key_room = "";
            String value = "";
            String turn_url = "";
            String turn_user = "";
            String turn_credential="";
            String stun_url="";
            String api_endpoint="";
            String[] messageSplittedKeys = message.split(MESSAGE_PAIR_KEY_VALUE_SEPARATOR);
            for (String keyvalues : messageSplittedKeys) {
                String[] messageSplitted = keyvalues.split(MESSAGE_KEY_VALUE_SEPARATOR);
                if (messageSplitted.length < 2) {
                    return;
                }

                String key = messageSplitted[0];
                if (VIDEOCALL_ROOM_KEY.equals(key)) {
                    key_room = key;
                    value = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
                }
                if (VIDEOCALL_TURN_URL_KEY.equals(key)) {
                    turn_url = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
                }
                if (VIDEOCALL_TURN_USER_KEY.equals(key)) {
                    turn_user = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
                }
                if (VIDEOCALL_TURN_CREDENTIAL_KEY.equals(key)) {
                    turn_credential = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
                }
                if (VIDEOCALL_STUN_URL_KEY.equals(key)) {
                    stun_url = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
                }
                if (VIDEOCALL_API_ENDPOINT_KEY.equals(key)) {
                    api_endpoint = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
                }
            }

            //String[] messageSplitted = message.split(MESSAGE_KEY_VALUE_SEPARATOR);
            //if (messageSplitted.length < 2) {
            //    return;
            //}

            //String key = messageSplitted[0];
            //value = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
            if (VIDEOCALL_ROOM_KEY.equals(key_room)) {
                String finalValue = value;
                String finalKey_room = key_room;
                String finalTurn_url = turn_url;
                String finalTurn_user = turn_user;
                String finalTurn_credential = turn_credential;
                String finalStun_url = stun_url;
                String finalApi_endpoint = api_endpoint;
                activity.runOnUiThread(() -> activity.showDialog(finalKey_room, finalValue, () -> activity.startVideoCall(finalValue, finalTurn_url, finalTurn_user, finalTurn_credential, finalStun_url, finalApi_endpoint)));
            }
        }else{          //not found
            String[] messageSplitted = message.split(MESSAGE_KEY_VALUE_SEPARATOR);
            if (messageSplitted.length < 2) {
                return;
            }

            String key = messageSplitted[0];
            String value = String.join(MESSAGE_KEY_VALUE_SEPARATOR, Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
            if (FILETRANSFER_URL_KEY.equals(key)) {
                activity.runOnUiThread(() -> activity.showDialogWithLink(key, value));
            }
        }
    }
*/
    private void processMediastreamingMessage(String message) {
        //Transform a json to java object
        Gson gson = new Gson();
        MessageDTO msg = gson.fromJson(message, MessageDTO.class);

        if (msg.getNameModule().equals(MODULE_NAME_VIDEOCALL)){
            String key_room = VIDEOCALL_ROOM_KEY;
            String value = msg.getRoomName();
            String turn_url = msg.getTurnURL();
            String turn_user = msg.getTurnUser();
            String turn_credential= msg.getTurnCredential();
            String stun_url= msg.getStunURL();
            String api_endpoint= msg.getApiEndpoint();
            long timeMessage = msg.getTimeMillis();

            long diffInSec = differenceTimeMessaging(timeMessage);

            if (diffInSec < 300){
                activity.runOnUiThread(() -> activity.showDialog(key_room, value, () -> activity.startVideoCall(value, turn_url, turn_user, turn_credential, stun_url, api_endpoint)));
            }

        }
        if (msg.getNameModule().equals(MODULE_NAME_FILETRANSFER)){
            String key = FILETRANSFER_URL_KEY;
            String value = msg.getUploadURL();
            long timeMessage = msg.getTimeMillis();

            long diffInSec = differenceTimeMessaging(timeMessage);

            if (diffInSec < 300) {
                activity.runOnUiThread(() -> activity.showDialogWithLink(key, value));
            }
        }

    }

    private long differenceTimeMessaging(long timeMessage){
        long timeNow = System.currentTimeMillis();

        long diff = timeNow - timeMessage;
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);

        return diffInSec;
    }

    private void initHeliosProfile(Activity activity) {
        HeliosProfileManager heliosProfileManager = HeliosProfileManager.getInstance();
        String userId = heliosProfileManager.load(activity, USER_ID, Context.MODE_PRIVATE);
        if (userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
            heliosProfileManager.store(activity, USER_ID, userId, Context.MODE_PRIVATE);
        }

        String username = heliosProfileManager.load(activity, USERNAME);
        if (username.isEmpty()) {
            username = "username-" + userId;
            heliosProfileManager.store(activity, USERNAME, username);
        }
    }

    private void initHeliosMessaging(Activity activity) {
        heliosMessagingNodejs = ReliableHeliosMessagingNodejsLibp2pImpl.getInstance();
        heliosMessagingNodejs.setContext(activity.getApplicationContext());
        try {
            heliosMessagingNodejs.connect(new HeliosConnectionInfo(), new HeliosIdentityInfo(HeliosUserData.getInstance().getValue(USERNAME),
                    HeliosUserData.getInstance().getValue(USER_ID)));

            Log.e(LOG_TAG, "Is connected " + heliosMessagingNodejs.isConnected());
            boolean connected = heliosMessagingNodejs.isConnected();
            heliosMessagingNodejs.subscribe(HELIOS_TOPIC, this);

            //heliosMessagingNodejs.announceTag(TOPIC);
            //heliosMessagingNodejs.observeTag(TOPIC);

            //heliosMessagingNodejs.getDirectMessaging().addReceiver(PROTOCOL_ID, this);
     //   } catch (RuntimeException | HeliosMessagingException e) {
       //     Log.e(LOG_TAG, "Helios Messaging - Connect Exception: " + e.toString(), e);
        //}
    } catch (Exception e) {
        Log.e(LOG_TAG, "Helios Messaging - Connect Exception: " + e.toString(), e);
    }
    }

    private void initHeliosSecurity(Activity activity) {
        HeliosCryptoManager heliosSecurityCrypto = HeliosCryptoManager.getInstance();
        KeyPair signingKeyPair = heliosSecurityCrypto.generateRSAKeyPair();
        KeyPair encryptingKeyPair = heliosSecurityCrypto.generateRSAKeyPair();

        heliosSecurityKeyStore = new HeliosKeyStoreManager(activity.getApplicationContext());
        try {
            heliosSecurityKeyStore.storePrivateKey(signingKeyPair.getPrivate(), "USER SIGNING KEY", PASS_PHRASE);
            heliosSecurityKeyStore.storePrivateKey(encryptingKeyPair.getPrivate(), "USER DECRYPTING KEY", PASS_PHRASE);
        } catch (HeliosKeyStoreException e) {
            Log.e(LOG_TAG, "Helios Security - KeyStore Exception: " + e.toString(), e);
        }
    }

    @Override
    public void receiveMessage(HeliosNetworkAddress address, String protocolId, FileDescriptor fd) {
        Log.i(LOG_TAG, "HeliosMessagingReceiver Direct - receiveMessage FileDescriptor");

        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        try (FileInputStream fileInputStream = new FileInputStream(fd) ){
            int byteRead;
            while ((byteRead = fileInputStream.read()) != -1) {
                ba.write(byteRead);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "MessagingService.direct receiveMessage FileDescriptor");
        }

        receiveMessage(address, protocolId, ba.toByteArray());
    }

    @Override
    public void receiveMessage(HeliosNetworkAddress address, String protocolId, byte[] data) {
        String message = new String(data, StandardCharsets.UTF_8);
        Log.i(LOG_TAG, "HeliosMessagingReceiver Direct - receiveMessage message: '" + message + "' (networkId: '" + address.getNetworkId() + "')");
        processMediastreamingMessage(message);
    }

    public void stop() {
        heliosMessagingNodejs.stop();
    }
}