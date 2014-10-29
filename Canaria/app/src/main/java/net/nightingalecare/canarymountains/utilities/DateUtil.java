package net.nightingalecare.canarymountains.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jae on 10/24/14.
 */
public class DateUtil {

    public static Date getDate(String timeStampStr, String format ){


        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(timeStampStr);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;

    }

    public static String convertDateStr(String timeStampStr, String oldFormat, String newFormat ){


        SimpleDateFormat sdf = new SimpleDateFormat(oldFormat);
        SimpleDateFormat sdf1 = new SimpleDateFormat(newFormat);

        Date date = null;
        String timeStamp = null;
        try {
            date = sdf.parse(timeStampStr);
            timeStamp = sdf1.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeStamp;

    }
}
