package org.perpetualnetworks.graphitemetrics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

public class FormatUtils {

    public static String formatObject(Object o) {
        if (o instanceof Float) {
            return format(((Float) o).doubleValue());
        } else if (o instanceof Double) {
            return format((Double) o);
        } else if (o instanceof Byte) {
            return format(((Byte)o).longValue());
        } else if (o instanceof Short) {
            return format(((Short)o).longValue());
        } else if (o instanceof Integer) {
            return format(((Integer)o).longValue());
        } else if (o instanceof Long) {
            return format((Double) o);
        } else if (o instanceof BigInteger) {
            return format(((BigInteger)o).doubleValue());
        } else if (o instanceof BigDecimal) {
            return format(((BigDecimal)o).doubleValue());
        } else {
            return o instanceof Boolean ? format((Boolean)o ? 1L : 0L) : null;
        }
    }

    public static String format(long n) {
        return Long.toString(n);
    }

    public static String format(double v) {
        return String.format(Locale.US, "%2.2f", v);
    }

}
