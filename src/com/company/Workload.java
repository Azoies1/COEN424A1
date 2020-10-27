package com.company;

public class Workload {

    private int cpuUtil;
    private double networkIn;
    private double networkOut;
    private double memUtil;

    public Workload(int cpuUtil, double networkIn, double networkOut, double memUtil) {
        this.cpuUtil = cpuUtil;
        this.networkIn = networkIn;
        this.networkOut = networkOut;
        this.memUtil = memUtil;
    }

    public int getCpuUtil() {
        return cpuUtil;
    }

    public void setCpuUtil(int cpuUtil) {
        this.cpuUtil = cpuUtil;
    }

    public double getNetworkIn() {
        return networkIn;
    }

    public void setNetworkIn(double networkIn) {
        this.networkIn = networkIn;
    }

    public double getNetworkOut() {
        return networkOut;
    }

    public void setNetworkOut(double networkOut) {
        this.networkOut = networkOut;
    }

    public double getMemUtil() {
        return memUtil;
    }

    public void setMemUtil(double memUtil) {
        this.memUtil = memUtil;
    }
}
