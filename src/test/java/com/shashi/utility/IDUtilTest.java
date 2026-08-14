package com.shashi.utility;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IDUtilTest {

    @Test
    void testGenerateProductId() {

        String id = IDUtil.generateId();

        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    void testGenerateTransactionId() {

        String id = IDUtil.generateTransId();

        assertNotNull(id);
        assertFalse(id.isEmpty());
    }
}