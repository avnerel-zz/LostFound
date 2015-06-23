package com.avner.lostfound.utils;

import android.content.Intent;

import com.avner.lostfound.Constants;

public interface IUIUpdateInterface {
    void onDataChange(Constants.UIActions action, boolean bSuccess, Intent data);
}