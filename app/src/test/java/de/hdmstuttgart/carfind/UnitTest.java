package de.hdmstuttgart.carfind;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local unit test, which will execute on the development machine (host).
 * Testing if user-entries in editTexts are not empty in every case-scenario
 */
public class UnitTest {
    AddCarFragment fragment = new AddCarFragment();

    @Test
    public void all_strings_not_empty() {
        assertTrue(fragment.checkUserEntry("Car", "RT SF 1234", "187"));
    }

    @Test
    public void all_strings_empty() {
        assertFalse(fragment.checkUserEntry("", "", ""));
    }

    @Test
    public void spot_empty() {
        assertFalse(fragment.checkUserEntry("Car", "RT SF 1234", ""));
    }

    @Test
    public void licence_empty() {
        assertFalse(fragment.checkUserEntry("Car", "", "187"));
    }

    @Test
    public void model_empty() {
        assertFalse(fragment.checkUserEntry("", "RT SF 1234", "187"));
    }
}
