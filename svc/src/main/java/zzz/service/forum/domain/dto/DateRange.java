//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package zzz.service.forum.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class DateRange {
    private String DateColumn;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date StartDate;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date EndDate;

    public String getDateColumn() {
        return this.DateColumn;
    }

    public void setDateColumn(String dateColumn) {
        this.DateColumn = dateColumn;
    }

    public Date getStartDate() {
        return this.StartDate;
    }

    public void setStartDate(Date startDate) {
        this.StartDate = startDate;
    }

    public Date getEndDate() {
        return this.EndDate;
    }

    public void setEndDate(Date endDate) {
        this.EndDate = endDate;
    }

    public DateRange(String dateColumn, Date startDate, Date endDate) {
        this.DateColumn = dateColumn;
        this.StartDate = startDate;
        this.EndDate = endDate;
    }
}
