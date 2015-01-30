package com.chinaxing.ioc;

/**
 * Created by lenovo on 2015/1/29.
 */
public class InitializeBeanException extends Exception {

    public InitializeBeanException(Throwable cause) {
        super(cause);
    }

    public InitializeBeanException(String message) {
        super(message);
    }
}
