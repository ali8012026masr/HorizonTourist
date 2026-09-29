package com.example.horizontourist;

import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.util.Formatters;

import org.junit.Test;

import static org.junit.Assert.*;

public class FormattersTest {

    @Test
    public void testFormatMoney() {
        assertEquals("BDT 1,200", Formatters.formatMoney(1200));
        assertEquals("BDT 0", Formatters.formatMoney(0));
        assertEquals("BDT 50.5", Formatters.formatMoney(50.5));
        assertEquals("BDT 1,234,567.89", Formatters.formatMoney(1234567.89));
    }

    @Test
    public void testCapitalizeEnum() {
        assertEquals("Mobile banking", Formatters.capitalizeEnum("MOBILE_BANKING"));
        assertEquals("Pending", Formatters.capitalizeEnum("PENDING"));
        assertEquals("Amusement park", Formatters.capitalizeEnum("AMUSEMENT_PARK"));
        assertEquals("", Formatters.capitalizeEnum(null));
        assertEquals("", Formatters.capitalizeEnum(""));
    }

    @Test
    public void testFormatDate() {
        String formatted = Formatters.formatDate("2026-10-15");
        assertEquals("Oct 15, 2026", formatted);

        assertEquals("", Formatters.formatDate(null));
        assertEquals("", Formatters.formatDate(""));
    }

    @Test
    public void testConstantsValues() {
        assertEquals("TOURIST", Constants.ROLE_TOURIST);
        assertEquals("OPEN", Constants.SLOT_STATUS_OPEN);
        assertEquals("CONFIRMED", Constants.BOOKING_STATUS_CONFIRMED);
        assertEquals("ACCEPTED", Constants.GUIDE_BOOKING_STATUS_ACCEPTED);
        assertEquals("FILTER_SLOTS", Constants.AI_ACTION_FILTER_SLOTS);
        assertEquals("RECOMMEND_BUNDLE", Constants.AI_ACTION_RECOMMEND_BUNDLE);
    }
}
