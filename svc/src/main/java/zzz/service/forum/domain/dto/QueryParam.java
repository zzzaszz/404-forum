//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package zzz.service.forum.domain.dto;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryParam extends BaseQueryParams {
    @JsonProperty("SearchPojo")
    private Object SearchPojo;
    @JsonProperty("DateRange")
    private DateRange DateRange;
    private String scenedata;
    private String searchdata;
    private String filterstr;
    private String tenantid;

    public Object getSearchPojo() {
        return this.SearchPojo;
    }

    public void setSearchPojo(Object searchPojo) {
        this.SearchPojo = searchPojo;
    }

    public DateRange getDateRange() {
        return this.DateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.DateRange = dateRange;
    }

    public String getScenedata() {
        return this.scenedata;
    }

    public void setScenedata(String scenedata) {
        this.scenedata = scenedata;
    }

    public String getSearchdata() {
        return this.searchdata;
    }

    public void setSearchdata(String searchdata) {
        this.searchdata = searchdata;
    }

    public String getFilterstr() {
        return this.filterstr;
    }

    public void setFilterstr(String filterstr) {
        this.filterstr = filterstr;
    }

    public String getTenantid() {
        return this.tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getSceneFilter() {
        String strFilter = "";
        if (this.scenedata != null && !"".equals(this.scenedata)) {
            for(SceneItemPojo item : JSONArray.parseArray(this.scenedata, SceneItemPojo.class)) {
                switch (item.getMath()) {
                    case "equal":
                        strFilter = strFilter + " and " + item.getField() + "='" + item.getValue() + "'";
                        break;
                    case "not equal":
                        strFilter = strFilter + " and " + item.getField() + "!='" + item.getValue() + "'";
                        break;
                    case "like":
                        strFilter = strFilter + " and " + item.getField() + " like '%" + item.getValue() + "%'";
                        break;
                    case "not like":
                        strFilter = strFilter + " and " + item.getField() + " not like '%" + item.getValue() + "%'";
                        break;
                    case "ilike":
                        strFilter = strFilter + " and LOWER(" + item.getField() + ") like '%" + item.getValue().toLowerCase() + "%'";
                        break;
                    case "not ilike":
                        strFilter = strFilter + " and LOWER(" + item.getField() + ") not like '%" + item.getValue().toLowerCase() + "%'";
                        break;
                    case ">":
                        strFilter = strFilter + " and " + item.getField() + ">" + item.getValue();
                        break;
                    case ">=":
                        strFilter = strFilter + " and " + item.getField() + ">=" + item.getValue();
                        break;
                    case "<":
                        strFilter = strFilter + " and " + item.getField() + "<" + item.getValue();
                        break;
                    case "<=":
                        strFilter = strFilter + " and " + item.getField() + "<=" + item.getValue();
                        break;
                    case "=":
                        strFilter = strFilter + " and " + item.getField() + "=" + item.getValue();
                        break;
                    case "!=":
                        strFilter = strFilter + " and " + item.getField() + "!=" + item.getValue();
                        break;
                    case "between":
                        strFilter = strFilter + " and (" + item.getField() + " BETWEEN '" + item.getValue() + "' and '" + item.getValueb() + "')";
                        break;
                    case "today":
                        strFilter = strFilter + " and (" + item.getField() + " BETWEEN CONCAT(CURDATE(),' 00:00:00') AND CONCAT(CURDATE(),' 23:59:59'))";
                        break;
                    case "month":
                        strFilter = strFilter + " and DATE_FORMAT(" + item.getField() + ",'%Y-%m')= DATE_FORMAT(CURDATE(),'%Y-%m') ";
                        break;
                    case "true":
                        strFilter = strFilter + " and " + item.getField() + "=1";
                        break;
                    case "false":
                        strFilter = strFilter + " and " + item.getField() + "=0";
                        break;
                    case "datedifflt":
                        strFilter = strFilter + " and DATEDIFF(" + item.getField() + ",CURDATE()) < " + item.getValue();
                        break;
                    case "datediffgt":
                        strFilter = strFilter + " and DATEDIFF(" + item.getField() + ",CURDATE()) > " + item.getValue();
                        break;
                    case "timedifflt":
                        strFilter = strFilter + " and TIMEDIFF(" + item.getField() + ",NOW()) < " + item.getValue();
                        break;
                    case "timediffgt":
                        strFilter = strFilter + " and TIMEDIFF(" + item.getField() + ",NOW()) > " + item.getValue();
                }
            }
        }

        return strFilter;
    }

    public String getSearchFilter() {
        String strFilter = "";
        if (this.searchdata != null && !"".equals(this.searchdata)) {
            for(SceneItemPojo item : JSONArray.parseArray(this.searchdata, SceneItemPojo.class)) {
                if (strFilter.length() > 0 && item.getFieldtype() < 2) {
                    strFilter = strFilter + " or ";
                }

                switch (item.getFieldtype()) {
                    case 0:
                        switch (item.getMath()) {
                            case "equal":
                                strFilter = strFilter + item.getField() + "='" + item.getValue() + "'";
                                continue;
                            case "ilike":
                                strFilter = strFilter + " LOWER(" + item.getField() + ") like '%" + item.getValue().toLowerCase() + "%'";
                                continue;
                            case "like":
                                strFilter = strFilter + item.getField() + " like '%" + item.getValue() + "%'";
                            default:
                                continue;
                        }
                    case 1:
                        strFilter = strFilter + item.getField() + "=" + item.getValue();
                }
            }
        }

        if (strFilter.length() > 0) {
            strFilter = " and (" + strFilter + ")";
        }

        return strFilter;
    }

    public String getAllFilter() {
        String sceneFilter = this.getSceneFilter();
        String searchFilter = this.getSearchFilter();
        return sceneFilter + searchFilter;
    }
}
