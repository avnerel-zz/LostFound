package com.avner.lostfound.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.avner.lostfound.Constants;

import java.util.ArrayList;

public class SignalSystem {
    private static SignalSystem ref;
    private static Context ctx;

    private static ArrayList<IUIUpdateInterface> mUIUpdateInterfaces;

    public static void initialize(Context context) {
        if (ref == null) {
            ctx = context;
            ref = new SignalSystem();
        }
    }

    public static SignalSystem getInstance() {
        return ref;
    }

    private SignalSystem() {
        mUIUpdateInterfaces = new ArrayList<>();
    }

    public void registerUIUpdateChange(IUIUpdateInterface aInterface) {
        if (!mUIUpdateInterfaces.contains(aInterface)) {
            mUIUpdateInterfaces.add(aInterface);
        }
    }

    public void unRegisterUIUpdateChange(IUIUpdateInterface aInterface) {
        if (mUIUpdateInterfaces.contains(aInterface)) {
            mUIUpdateInterfaces.remove(aInterface);
        }
    }

    public void fireUpdateChange(final Constants.UIActions uiAction, final boolean bSuccess, final Intent data) {

        Log.d("SIGNAL_SYSTEM", "signaling for uiAction: " + uiAction);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            // We are on the main GUI thread
            int index;
            int iSize;

            iSize = mUIUpdateInterfaces.size();

            for ( index = 0; index < iSize; index++){
                mUIUpdateInterfaces.get(index).onDataChange(uiAction, bSuccess, data);
            }
        }
        else {
            // We are not on the GUI thread, need to switch context
            // Get a handler that can be used to post to the main thread
            Handler mainHandler = new Handler(ctx.getMainLooper());

            Runnable myRunnable = new Runnable() {

                @Override
                public void run() {
                    fireUpdateChange(uiAction, bSuccess, data);
                }
            };

            mainHandler.post(myRunnable);
        }
    }

    public void fireUpdateChange(Constants.UIActions uiAction, boolean bSuccess) {

        fireUpdateChange(uiAction, bSuccess, null);
    }

    public void fireUpdateChange(Constants.UIActions uiAction) {

        fireUpdateChange(uiAction, true, null);
    }
}