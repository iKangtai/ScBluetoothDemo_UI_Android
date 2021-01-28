package com.example.bledemo.event;

/**
 * Created by Administrator on 2016/7/7.
 */
public class ConnectStateMsg {

    private boolean comefromBindPage;

    private int respCode;

    public ConnectStateMsg(){
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public boolean isComefromBindPage() {
        return comefromBindPage;
    }

    public void setComefromBindPage(boolean comefromBindPage) {
        this.comefromBindPage = comefromBindPage;
    }
}


