package com.windy.sendexceltoweichar;

import org.junit.Test;

import java.util.Calendar;

import static com.windy.sendexceltoweichar.ConstantValues.FILENAME_SUFFIX;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        String monthStr = String.valueOf(month);

        int date = cal.get(Calendar.DAY_OF_MONTH);
        String dateStr = String.valueOf(date);

        String s = ConstantValues.FILENAME_PREFIX + monthStr + dateStr + FILENAME_SUFFIX;

        assertEquals(4, 2 + 2);
    }
}