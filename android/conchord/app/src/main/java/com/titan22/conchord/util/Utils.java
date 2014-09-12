package com.titan22.conchord.util;

import java.util.Random;

public class Utils {

    /* Shout out: http://goo.gl/cyT2JO. */
    public static String getRandomAlphaNumbericString(int length) {
        final String ABC = "23456789abcdefghijkmnpqrstuvwxyz";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ABC.charAt(rnd.nextInt(ABC.length())));
        }
        return sb.toString();
    }

    // TODO is this necessary? make sure devices use same server
    public static final String[] someCaliNtpServers = { "clock.isc.org",
            "ntp-cup.external.hp.com", "clepsydra.dec.com", "clock.via.net",
            "clock.sjc.he.net", "clock.fmt.he.ne", "nist1.symmetricom.com",
            "usno.pa-x.dec.com", "nist1-la.WiTime.net",
            "time.no-such-agency.net", "gps.layer42.net"
    };

}
