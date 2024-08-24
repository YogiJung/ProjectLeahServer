package Utils;

import com.google.gson.JsonObject;

public class ResponseFormat {
    String data = "";
    int backPressureFlag;
    JsonObject header = new JsonObject();
    //sendingSignal : PAUSE, START
    public ResponseFormat(String endpoint, int backPressureFlag) {
        header.addProperty("endpoint", endpoint);
        this.backPressureFlag = backPressureFlag;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public int getBackPressureFlag() {
        return backPressureFlag;
    }

    public void setBackPressureFlag(int backPressureFlag) {
        this.backPressureFlag = backPressureFlag;
    }
}
