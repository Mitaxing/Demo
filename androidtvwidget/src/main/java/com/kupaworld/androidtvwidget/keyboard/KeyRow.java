package com.kupaworld.androidtvwidget.keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * 按键的列数组.
 */
public class KeyRow {

    private List<SoftKey> mSoftKeys = new ArrayList<SoftKey>();

    public List<SoftKey> getSoftKeys() {
        return mSoftKeys;
    }

    public void setSoftKeys(List<SoftKey> mSoftKeys) {
        this.mSoftKeys = mSoftKeys;
    }

}
