package com.shashi.beans;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductBeanTest {

    @Test
    void testProductBean() {

        ProductBean product = new ProductBean();

        product.setProdId("P001");
        product.setProdName("Laptop");
        product.setProdType("Electronics");
        product.setProdInfo("Test Laptop");
        product.setProdPrice(50000);
        product.setProdQuantity(10);

        assertEquals("P001", product.getProdId());
        assertEquals("Laptop", product.getProdName());
        assertEquals("Electronics", product.getProdType());
        assertEquals("Test Laptop", product.getProdInfo());
        assertEquals(50000, product.getProdPrice());
        assertEquals(10, product.getProdQuantity());
    }
}