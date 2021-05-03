package com.helios.helios_media_module.dto;

import java.io.Serializable;

public class MessageDTO implements Serializable {
    private String moduleName;
    private String uploadURL;
    private String roomName;
    private String turnURL;
    private String turnUser;
    private String turnCredential;
    private String stunURL;
    private String apiEndpoint;

    public String getModuleName() {
        return moduleName;
    }

    public String getUploadURL() {
        return uploadURL;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getTurnURL() {
        return turnURL;
    }

    public String getTurnUser() {
        return turnUser;
    }

    public String getTurnCredential() {
        return turnCredential;
    }

    public String getStunURL() {
        return stunURL;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setUploadURL(String uploadURL) {
        this.uploadURL = uploadURL;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setTurnURL(String turnURL) {
        this.turnURL = turnURL;
    }

    public void setTurnUser(String turnUser) {
        this.turnUser = turnUser;
    }

    public void setTurnCredential(String turnCredential) {
        this.turnCredential = turnCredential;
    }

    public void setStunURL(String stunURL) {
        this.stunURL = stunURL;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
}
