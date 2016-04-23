package com.jasper.ble;

/**
 * Created by Jasper on 2015/12/10.
 */
public class Package {
    public static void BuildPackage(byte Islock,byte IsMove,byte IsMiss,byte bytes[])
    {
        bytes[0] = 's';
        bytes[1] = Islock;
        bytes[2] = IsMove;
        bytes[3] = IsMiss;
        bytes[4] = 'u';
    }
}
