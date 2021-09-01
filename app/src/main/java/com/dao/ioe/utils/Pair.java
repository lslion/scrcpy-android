package com.dao.ioe.utils;

import java.net.Socket;

public class Pair{

    public Socket socketOutput;
    public Boolean metadataSent;

    public Pair(Socket socketOutput, Boolean metadataSent){
        this.socketOutput = socketOutput;
        this.metadataSent = metadataSent;
    }


    public Socket getSocketOutput() {
        return socketOutput;
    }

    public void setSocketOutput(Socket socketOutput) {
        this.socketOutput = socketOutput;
    }

    public Boolean getMetadataSent() {
        return metadataSent;
    }

    public void setMetadataSent(Boolean metadataSent) {
        this.metadataSent = metadataSent;
    }
}
