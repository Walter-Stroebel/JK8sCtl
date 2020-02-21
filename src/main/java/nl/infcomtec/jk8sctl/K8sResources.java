/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

/**
 *
 * @author walter
 */
public class K8sResources {
    
    public double cpuAvail;
    public double memAvail;
    public double dskAvail;
    public double podAvail;
    public double cpuUsed;
    public double memUsed;
    public double dskUsed;
    public double podUsed;

    public int cpu(){
        if (cpuAvail<=0)return 255;
        double d = 255f*cpuUsed/cpuAvail;
        if (d<0)return 0;
        if (d>255)return 255;
        return (int)(Math.round(d));
    }

    public int mem(){
        if (memAvail<=0)return 255;
        double d = 255f*memUsed/memAvail;
        if (d<0)return 0;
        if (d>255)return 255;
        return (int)(Math.round(d));
    }

    public int dsk(){
        if (dskAvail<=0)return 255;
        double d = 255f*dskUsed/dskAvail;
        if (d<0)return 0;
        if (d>255)return 255;
        return (int)(Math.round(d));
    }

    public int pod(){
        if (podAvail<=0)return 255;
        double d = 255f*podUsed/podAvail;
        if (d<0)return 0;
        if (d>255)return 255;
        return (int)(Math.round(d));
    }
    
}
