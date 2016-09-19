package net.ddns.woodhouse.busywidget;

public class BusyModel {

    private static BusyModel ourInstance = new BusyModel();

    public static BusyModel getInstance() {
        return ourInstance;
    }

    private BusyModel() {
        isBusy = false;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    private boolean isBusy;

}
