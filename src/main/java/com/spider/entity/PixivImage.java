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
    private Integer illustType;
    private String createDate;
    private String uploadDate;
    private Urls urls;
    private Integer restrict;
    private Integer xRestrict;
    private Integer sl;
    private String alt;
    private String userId;
    private String userName;
    private String userAccount;
    private boolean likeData;
    private Integer width;
    private Integer height;
    private Integer pageCount;
    private Integer bookmarkCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer responseCount;
    private Integer viewCount;
    private boolean isHowto;
    private boolean isOriginal;
    private Integer imageResponseCount;
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
    private List<String> md5List;

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
