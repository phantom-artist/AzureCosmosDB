package com.phantomartist.azure.cosmosdb.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Bean to model some data
 */
public class Product {

	String id;
    int count;
    Date createdDate;
    Timestamp createdTime;
    UUID uuid;
    int refId;
    boolean isFindable;
    String desc;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    public Timestamp getCreatedTime() { return createdTime; }
    public void setCreatedTime(Timestamp createdTime) { this.createdTime = createdTime; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public int getRefId() { return refId; }
    public void setRefId(int refId) { this.refId = refId; }
    public boolean isFindable() { return isFindable; }
    public void setFindable(boolean isFindable) { this.isFindable = isFindable; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
}
