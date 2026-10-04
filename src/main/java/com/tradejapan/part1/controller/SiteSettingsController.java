package com.tradejapan.cms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Website settings that the public pages need. They come from application.properties / local.properties. */
@RestController
@RequestMapping("/api/settings")
public class SiteSettingsController {

    @Value("${app.whatsapp-number:}")
    private String whatsappNumber;

    /** The WhatsApp number for the vehicle page button (empty = the button is hidden). */
    @GetMapping("/public")
    public Map<String, String> publicSettings() {
        return Map.of("whatsappNumber", whatsappNumber == null ? "" : whatsappNumber.trim());
    }
}
