package com.m3.common.core;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

/**
 * JDK UUID allows for IETF UUID representations.
 * However it provides methods to generate only 2 of 4 types
 * Random = type-4 and Name-based = type-3.
 * <p/>
 * We will NOT concern ourselves with DCE security (type-2).
 * <p/>
 * We focus here on UUIDs based on Time (type-1).
 * <p/>
 * Most Time-based UUID generators out there are quite complex 
 * and most if not all rely on some native code.
 * <p/>
 * Here we focus on a pure-JVM simple Time-based UUID generator 
 * making use of the MAC address of the host.
 * <p/>
 * Compatible with JDK 6+
 * 
 * @author museg
 *
 */
public final class TimeBasedUuidGenerator {
    // UUID IETF Magic Numbers
    private static final long _CLOCK_MID_EXTRACTER = 0xFFFF00000000L;
    private static final long _TIME_BASED_TYPE = 0x1000;
    private static final long _CLOCK_HI_EXTRACTER = 0x0FFF;
    private static final int _CLOCK_MID_NBITS = 48;

    // synchronizer object; used to update the clock sequence
    private static final Object _LOCK = new Object();

    // MAC-based host identifier
    private static final long _HOST_IDENTIFIER = getHostId();

    private static long _lastTime = 0L;
    private static long _clockSequence = 0L;

    public final static UUID generate() {
        long currTimeMillis = System.currentTimeMillis();
        synchronized (_LOCK) {
            if (currTimeMillis > _lastTime) {
                _lastTime = currTimeMillis;
                _clockSequence = 0L;
            } else {
                ++_clockSequence;
            }
        }
        long time = currTimeMillis << 32; // low time
        time |= ((currTimeMillis & _CLOCK_MID_EXTRACTER) >> 16); // mid time
        time |= _TIME_BASED_TYPE | ((currTimeMillis >> _CLOCK_MID_NBITS) & _CLOCK_HI_EXTRACTER);
        return new UUID(time, generateLsb());
    }

    private static long generateLsb() {
        long clockSeq = _clockSequence;
        clockSeq <<= _CLOCK_MID_NBITS;
        long lsb = clockSeq | _HOST_IDENTIFIER;
        return lsb;
    }

    // finds the first non-loopback NIC
    private static long getHostId() {
        byte[] _eth_data = null;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface nint = en.nextElement();
                if (!nint.isLoopback()) {
                    byte[] data = nint.getHardwareAddress();
                    if (data != null && data.length == 6) {
                        _eth_data = data;
                        break; // found one that is not loopback
                    }
                }
            }
        } catch (SocketException se) {
            // fine; take it as a signal of not having any network interfaces
        }
        // if all NICs are exhausted and nothing seems to work, we generate a 
        // random sequence to represent a (fake) multicast address
        if (_eth_data == null) {
            _eth_data = new byte[6];
            M3CoreHelper.generateRandom(_eth_data);
            _eth_data[0] |= (byte)0x01;
        }
        long macAddressAsLong = _eth_data[0] & 0xFF;
        for (int ix = 1; ix < 6; ix++) {
            macAddressAsLong = (macAddressAsLong << 8) | (_eth_data[ix] & 0xFF);
        }
        return macAddressAsLong;
    }
}
