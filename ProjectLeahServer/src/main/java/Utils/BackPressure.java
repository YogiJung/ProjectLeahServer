package Utils;

public class BackPressure {
//    backPressureFlag = 0 (receive), 1(pause)
    public static int backPressureFlag;

    public static void setBackPressureFlag(int backPressureFlag1) {
        backPressureFlag = backPressureFlag1;
    }

    public static int getBackPressureFlag() {
        return backPressureFlag;
    }

}
