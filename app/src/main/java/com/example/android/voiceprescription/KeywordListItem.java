package com.example.android.voiceprescription;

public class KeywordListItem {

    private String mItemKeyword;
    private String mItemData;

    public KeywordListItem(String itemKeyword, String itemData){
        mItemData = itemData;
        mItemKeyword = itemKeyword;
    }

    public void setItemData(String mItemData) {
        this.mItemData = mItemData;
    }

    public String getItemKeyword() {
        return mItemKeyword;
    }

    public String getItemData() {
        return mItemData;
    }
}
