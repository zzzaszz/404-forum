//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package zzz.service.forum.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseQueryParams {
    @JsonProperty("PageNum")
    private Integer PageNum = 1;
    @JsonProperty("PageSize")
    private Integer PageSize = 10;
    @JsonProperty("OrderBy")
    private String OrderBy;
    @JsonProperty("OrderType")
    private Integer OrderType = 0;
    @JsonProperty("SearchType")
    private Integer SearchType = 0;

    public Integer getPageNum() {
        return this.PageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.PageNum = pageNum;
    }

    public Integer getPageSize() {
        return this.PageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.PageSize = pageSize;
    }

    public String getOrderBy() {
        return this.OrderBy;
    }

    public void setOrderBy(String orderBy) {
        this.OrderBy = orderBy;
    }

    public Integer getOrderType() {
        return this.OrderType;
    }

    public void setOrderType(Integer orderType) {
        this.OrderType = orderType;
    }

    public Integer getSearchType() {
        return this.SearchType;
    }

    public void setSearchType(Integer searchType) {
        this.SearchType = searchType;
    }
}
