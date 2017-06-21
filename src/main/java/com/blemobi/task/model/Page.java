package com.blemobi.task.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/6 15:13
 */
@Getter
@Setter
@ToString
public class Page<T> {
    private int currentPage;
    private int totPage;
    private List<T> data;
    private int totRecords;
    private int recordPerPage;
}
