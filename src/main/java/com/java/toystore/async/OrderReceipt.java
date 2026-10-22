package com.java.toystore.async;

public record OrderReceipt(Integer orderId, String pdfFileName, String generatedByThread) {
}
