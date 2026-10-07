package com.tradejapan.part1.model;
import jakarta.persistence.*;

@Entity
@Table(name = "banners")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerSize size;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    private String imageUrl;
    private String linkUrl;

    // INTERNAL or EXTERNAL
    private String linkType;

    @Column(name = "button_text")
    private String buttonText;

    // Overlay text shown on the banner
    private String title;
    private String subtitle;

    // NULL = BIG hero, 1 = SMALL left section, 2 = SMALL right section
    @Column(name = "section_num")
    private Integer sectionNum;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    private boolean active = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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