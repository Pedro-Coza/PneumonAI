package com.pneumonai.pneumonai;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;
import java.util.ListIterator;

public class Singleton {
    private static Singleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    private Singleton(Context context){
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if(requestQueue==null){
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized Singleton getInstance(Context context){
        if(mInstance==null){
            mInstance = new Singleton(context);
        }
        return mInstance;
    }
    public<T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);  
    }

}
