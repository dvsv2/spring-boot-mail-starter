package com.dvsv2.study.tools.mail.services;

import com.dvsv2.study.tools.mail.MyEmail;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangs on 17/5/10.
 */
public class InvokeHandler {
    private Method method;
    private List<Object> params = new ArrayList<Object>();
    private MyEmail myEmail;
    private List<Integer> offsets;
    private Object object;

    public InvokeHandler(Object obj,Method method, List<Integer> offsets, List<Object> params) {
        this.params = params;
        this.object = obj;
        this.method = method;
        this.offsets = offsets;
    }

    public void handle(MyEmail myEmail) {
        for (Integer tmp : offsets) {
            this.params.set(tmp, myEmail);
        }
        try {
            method.invoke(object, params.toArray());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

    public void setOffsets(List<Integer> offsets) {
        this.offsets = offsets;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public MyEmail getMyEmail() {
        return myEmail;
    }

    public void setMyEmail(MyEmail myEmail) {
        this.myEmail = myEmail;
    }
}
