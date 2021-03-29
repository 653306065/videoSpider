package com.spider.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "pixivImage")
@Getter
@Setter
public class PixivImage {

    private String id;
    private String illustId;
    private String illustTitle;
    private String illustComment;
    private String title;
    private String description;
    private int illustType;
    private String createDate;
    private String uploadDate;
    private Urls urls;
    private int restrict;
    private int xRestrict;
    private int sl;
    private String alt;
    private String userId;
    private String userName;
    private String userAccount;
    private boolean likeData;
    private int width;
    private int height;
    private int pageCount;
    private int bookmarkCount;
    private int likeCount;
    private int commentCount;
    private int responseCount;
    private int viewCount;
    private boolean isHowto;
    private boolean isOriginal;
    private int imageResponseCount;
    private Object pollData;
    private Object seriesNavData;
    private Object descriptionBoothId;
    private Object descriptionYoutubeId;
    private Object comicPromotion;
    private Object fanboxPromotion;
    private boolean isBookmarkable;
    private boolean isUnlisted;
    private List<String> imageUrl;
    private List<String> imageSavePath;

    @Getter
    @Setter
    public static class Urls {
        private String small;
        private String original;
        private String mini;
        private String regular;
        private String thumb;
    }
}
