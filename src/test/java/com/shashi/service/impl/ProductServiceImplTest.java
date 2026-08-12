package com.shashi.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.shashi.beans.ProductBean;

public class ProductServiceImplTest {

    @Test
    void testUpdateProductWithDifferentIds() {

        ProductBean oldProduct = new ProductBean();
        oldProduct.setProdId("P001");

        ProductBean newProduct = new ProductBean();
        newProduct.setProdId("P002");

        ProductServiceImpl service = new ProductServiceImpl();

        String result = service.updateProduct(oldProduct, newProduct);

        assertEquals(
                "Both Products are Different, Updation Failed!",
                result);
    }
}