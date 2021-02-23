package com.spider.utils.download;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadFileInfo {

    private int responseCode;

    private long contentLength;

    private String contentType;

    private String etag;
}
