package com.spider.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Document(collection = "proxyrarbgBT")
public class ProxyrarbgBT {

    private String id;

    private byte[] torrent;

    private String torrentName;

    private String torrentUrl;

    private String magnet;

    private byte[] poster;

    private String posterUrl;

    private String descriptionContent ;

    private String descriptionHtml;

    private List<byte[]> descriptionImages;

    private List<String> descriptionImagesUrl;

    private String mediaInfo;

    private Double Rating;

    private String category;

    private Long size;

    private String sizeStr;

    private Date addDate;

    private String title;

    private String plot;

    private String tags;

    private String releaseName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getTorrent() {
        return torrent;
    }

    public void setTorrent(byte[] torrent) {
        this.torrent = torrent;
    }

    public String getTorrentName() {
        return torrentName;
    }

    public void setTorrentName(String torrentName) {
        this.torrentName = torrentName;
    }

    public String getTorrentUrl() {
        return torrentUrl;
    }

    public void setTorrentUrl(String torrentUrl) {
        this.torrentUrl = torrentUrl;
    }

    public String getMagnet() {
        return magnet;
    }

    public void setMagnet(String magnet) {
        this.magnet = magnet;
    }

    public byte[] getPoster() {
        return poster;
    }

    public void setPoster(byte[] poster) {
        this.poster = poster;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDescriptionContent() {
        return descriptionContent;
    }

    public void setDescriptionContent(String descriptionContent) {
        this.descriptionContent = descriptionContent;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    public List<byte[]> getDescriptionImages() {
        return descriptionImages;
    }

    public void setDescriptionImages(List<byte[]> descriptionImages) {
        this.descriptionImages = descriptionImages;
    }

    public List<String> getDescriptionImagesUrl() {
        return descriptionImagesUrl;
    }

    public void setDescriptionImagesUrl(List<String> descriptionImagesUrl) {
        this.descriptionImagesUrl = descriptionImagesUrl;
    }

    public String getMediaInfo() {
        return mediaInfo;
    }

    public void setMediaInfo(String mediaInfo) {
        this.mediaInfo = mediaInfo;
    }

    public Double getRating() {
        return Rating;
    }

    public void setRating(Double rating) {
        Rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getSizeStr() {
        return sizeStr;
    }

    public void setSizeStr(String sizeStr) {
        this.sizeStr = sizeStr;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }
}
