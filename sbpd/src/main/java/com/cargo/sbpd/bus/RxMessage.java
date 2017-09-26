package com.cargo.sbpd.bus;

import android.support.annotation.StringDef;

import com.cargo.sbpd.model.dao.LocalFileDao;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by leb on 14/09/2017.
 */

public class RxMessage {

    @StringDef({Type.REQUEST, Type.RESPONSE, Type.BROADCAST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        String REQUEST = "request";
        String RESPONSE = "response";
        String BROADCAST = "broadcast";
    }

    private String mSender;
    private String mRequest;
    private Object mObject;
    private String mType;

    public RxMessage(String sender, String request, @Type String type, Object object) {
        mSender = sender;
        mRequest = request;
        mObject = object;
        mType = type;
    }

    public String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        mSender = sender;
    }

    public String getRequest() {
        return mRequest;
    }

    public void setRequest(String request) {
        mRequest = request;
    }

    public Object getObject() {
        return mObject;
    }

    public void setObject(Object object) {
        mObject = object;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    @Override
    public String toString() {
        return "RxMessage{" +
                "mSender='" + mSender + '\'' +
                ", mRequest='" + mRequest + '\'' +
                ", mObject=" + mObject +
                ", mType='" + mType + '\'' +
                '}';
    }
}
