package com.tradejapan.part3.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class InvoiceStorageService {

    private static final long MAX_PDF_SIZE = 5L * 1024L * 1024L;
    private final Path invoiceDirectory = Paths.get("private-invoices").toAbsolutePath().normalize();

    public InvoiceStorageService() {
        try {
            Files.createDirectories(invoiceDirectory);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create private invoice directory", ex);
        }
    }

    public void store(Long purchaseId, MultipartFile file) {
        validatePdf(file);
        Path target = filePath(purchaseId);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Could not save invoice PDF", ex);
        }
    }

    public Resource load(Long purchaseId) {
        Path file = filePath(purchaseId);
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("Invoice PDF has not been uploaded for this purchase");
        }
        try {
            return new UrlResource(file.toUri());
        } catch (Exception ex) {
            throw new RuntimeException("Could not read invoice PDF", ex);
        }
    }

    private Path filePath(Long purchaseId) {
        return invoiceDirectory.resolve("purchase-" + purchaseId + ".pdf").normalize();
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        if (file.getSize() > MAX_PDF_SIZE) {
            throw new IllegalArgumentException("Invoice PDF must be 5 MB or smaller");
        }
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(4);
            boolean pdf = header.length == 4
                    && header[0] == '%'
                    && header[1] == 'P'
                    && header[2] == 'D'
                    && header[3] == 'F';
            if (!pdf) {
                throw new IllegalArgumentException("Only a valid PDF invoice can be uploaded");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not read uploaded file", ex);
        }
    }
}
