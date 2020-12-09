package com.spider.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@Data
@Document(collection = "codexInfo")
public class CodexInfo {


    private String id;
    private String context;
    private String type;
    private String dateCreated;
    private String datePublished;
    private String dateModified;
    private String headline;
    private String name;
    private String keywords;
    private String url;
    private String description;
    private String copyrightYear;
    private PublisherDTO publisher;
    private SourceOrganizationDTO sourceOrganization;
    private CopyrightHolderDTO copyrightHolder;
    private MainEntityOfPageDTO mainEntityOfPage;
    private AuthorDTO author;
    private String articleSection;
    private String articleBody;
    private ImageDTO image;
    private String magnet;

    @NoArgsConstructor
    @Data
    public static class PublisherDTO {
        private String id;
        private String type;
        private String name;
        private LogoDTO logo;
        private List<String> sameAs;

        @NoArgsConstructor
        @Data
        public static class LogoDTO {
            private String type;
            private String url;
        }
    }

    @NoArgsConstructor
    @Data
    public static class SourceOrganizationDTO {
        private String id;
    }

    @NoArgsConstructor
    @Data
    public static class CopyrightHolderDTO {
        private String id;
    }

    @NoArgsConstructor
    @Data
    public static class MainEntityOfPageDTO {
        private String type;
        private String id;
    }

    @NoArgsConstructor
    @Data
    public static class AuthorDTO {

        private String type;
        private String name;
        private String url;
    }

    @NoArgsConstructor
    @Data
    public static class ImageDTO {


        private String type;
        private String url;
        private Integer width;
        private Integer height;
    }
}
