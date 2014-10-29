package net.nightingalecare.canarymountains.database;

import android.provider.BaseColumns;

public class SensorContract {

    private interface SensorColumns {
        /** Sensor unique MAC address */
        public final static String ADDRESS = "address";
        /** The user defined sensor name */
        public final static String NAME = "name";
        /** 1 if user added the sensor to his/her devices */
        public final static String STORED = "stored";
    }

    private interface SensorDataColumns {
        /** Sensor ID from SENSORS table */
        public final static String SENSOR_ID = "sensor_id";
        /** A temperature value in Celsius */
        public final static String FREQ = "freq";

        public final static String TOTAL_FREQ = "cum_freq";
        /** Battery status (0-100%) */
        public final static String BATTERY = "battery";
        /** The time when data was obtained */
        public final static String TIMESTAMP = "timestamp";
        /** 1 when last data is no more than 5 minutes old, 0 otherwise */
        public final static String RECENT = "recent";
    }

    public final class Sensor implements BaseColumns, SensorColumns {
        private Sensor() {
            // empty
        }

        public final class Data implements BaseColumns, SensorDataColumns {
            private Data() {
                // empty
            }
        }

    }
}