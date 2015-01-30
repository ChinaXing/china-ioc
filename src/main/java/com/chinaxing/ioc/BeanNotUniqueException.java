package com.chinaxing.ioc;

/**
 * Created by lenovo on 2015/1/29.
 */

public class BeanNotUniqueException extends Exception {
    public BeanNotUniqueException(String message) {
        super(message);
    }
}
