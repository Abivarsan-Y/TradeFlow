package com.tradejapan.part3.config;

import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.InquiryStatus;
import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.model.PurchaseStatus;
import com.tradejapan.part3.repository.InquiryRepository;
import com.tradejapan.part3.repository.PurchaseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("demo")
public class DemoDataConfig {

    @Bean
    CommandLineRunner loadDemoData(InquiryRepository inquiryRepository,
                                   PurchaseRepository purchaseRepository) {
        return args -> {
            Inquiry inquiry;
            if (inquiryRepository.count() == 0) {
                inquiry = new Inquiry();
                inquiry.setUserId(1L);
                inquiry.setVehicleId(101L);
                inquiry.setSubject("Toyota Prius export inquiry");
                inquiry.setMessage("Please send more details about this vehicle and shipping options.");
                inquiry.setDestinationCountry("Sri Lanka");
                inquiry.setStatus(InquiryStatus.NEW);
                inquiry = inquiryRepository.save(inquiry);
            } else {
                inquiry = inquiryRepository.findAll().get(0);
            }

            if (purchaseRepository.count() == 0) {
                Purchase purchase = new Purchase();
                purchase.setUserId(inquiry.getUserId());
                purchase.setVehicleId(inquiry.getVehicleId());
                purchase.setProcessedBy(2L);
                purchase.setInquiryId(inquiry.getInquiryId());
                purchase.setTotalPrice(new BigDecimal("12500.00"));
                purchase.setAdvancePaid(new BigDecimal("2500.00"));
                purchase.setStatus(PurchaseStatus.ADVANCE_PAID);
                purchase.setNotes("Demo purchase linked to the first inquiry.");
                purchaseRepository.save(purchase);
            }
        };
    }
}
