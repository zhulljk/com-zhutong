package com.example.vo;

public class BussinessException extends Exception{
    private String code;
    //private String message;
    private int validId;

    public BussinessException(String code, String message, int validId) {
        super(message);
        this.code = code;
        this.validId = validId;
    }
    /**
     * @param message
    @Override
    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }
     */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getValidId() {
        return validId;
    }

    public void setValidId(int validId) {
        this.validId = validId;
    }
}
