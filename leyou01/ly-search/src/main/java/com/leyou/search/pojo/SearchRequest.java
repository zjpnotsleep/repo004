package com.leyou.search.pojo;

public class SearchRequest {
    private String key;     //搜索字段
    private Integer page;  //当前页
    private static final int DEFAULT_SIZE = 20;
    private static final int DEFAULT_PAGE = 1;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getPage() {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        //获取页码时做一些校验，不能小于1
        return Math.max(DEFAULT_PAGE, page);
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return DEFAULT_SIZE;
    }
}
