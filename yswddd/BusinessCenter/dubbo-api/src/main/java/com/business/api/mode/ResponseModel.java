package com.business.api.mode;

import lombok.Data;
import java.io.Serializable;

@Data
public class ResponseModel implements Serializable {

    private static final long serialVersionUID = 4048457564823262432L;
    private boolean isSuccess;
    private String message;

    public ResponseModel(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
