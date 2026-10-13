package com.tradejapan.part1.dto;

import com.tradejapan.part1.model.BannerSize;
import com.tradejapan.part1.model.MediaType;

public class BannerRequest {

    private BannerSize size;
    private MediaType mediaType;
    private String imageUrl;
    private String linkUrl;
    private String linkType;      // INTERNAL or EXTERNAL
    private String buttonText;
    private String title;
    private String subtitle;
    private Integer sectionNum;   // null = BIG hero, 1 = SMALL left, 2 = SMALL right
    private Integer sortOrder;
    private boolean active = true;

    public BannerSize getSize() { return size; }
    public void setSize(BannerSize size) { this.size = size; }
    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    public String getButtonText() { return buttonText; }
    public void setButtonText(String buttonText) { this.buttonText = buttonText; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public Integer getSectionNum() { return sectionNum; }
    public void setSectionNum(Integer sectionNum) { this.sectionNum = sectionNum; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}