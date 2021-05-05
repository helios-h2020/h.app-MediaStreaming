package com.helios.helios_media_module.dto;

import java.io.Serializable;

public class MessageDTO implements Serializable {
    private String nameModule;
    private String uploadURL;
    private String roomName;
    private String turnURL;
    private String turnUser;
    private String turnCredential;
    private String stunURL;
    private String apiEndpoint;
    private long timeMillis;

    public String getNameModule() {
        return nameModule;
    }

    public void setNameModule(String nameModule) {
        this.nameModule = nameModule;
    }

    public String getUploadURL() {
        return uploadURL;
    }

    public void setUploadURL(String uploadURL) {
        this.uploadURL = uploadURL;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTurnURL() {
        return turnURL;
    }

    public void setTurnURL(String turnURL) {
        this.turnURL = turnURL;
    }

    public String getTurnUser() {
        return turnUser;
    }

    public void setTurnUser(String turnUser) {
        this.turnUser = turnUser;
    }

    public String getTurnCredential() {
        return turnCredential;
    }

    public void setTurnCredential(String turnCredential) {
        this.turnCredential = turnCredential;
    }

    public String getStunURL() {
        return stunURL;
    }

    public void setStunURL(String stunURL) {
        this.stunURL = stunURL;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }
}
