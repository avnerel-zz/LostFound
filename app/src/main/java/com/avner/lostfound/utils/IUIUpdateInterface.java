package com.avner.lostfound.utils;

import android.content.Intent;

import com.avner.lostfound.Constants;

/**
 * Created by user on 5/13/2015.
 */
public interface IUIUpdateInterface {
    void onDataChange(Constants.UIActions action, boolean bSuccess, Intent data);
}