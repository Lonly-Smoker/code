package com.xu1900.code.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户下载实体类
 * @JsonIgnoreProperties将这个注解写在类上之后，会忽略类中不存在的字段
 */
@Entity
@Table(name = "userDownload")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","hander","fieldHandler"})
public class UserDownload implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userDownloadId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date downloadDate;  //下载时间

    @ManyToOne
    @JoinColumn(name="articleId")
    private Article article;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    public Integer getuserDownloadId() {
        return userDownloadId;
    }

    public void setuserDownloadId(Integer userDownloadId) {
        this.userDownloadId = userDownloadId;
    }

    public Date getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(Date downloadDate) {
        this.downloadDate = downloadDate;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserDownload{" +
                "userDownloadId=" + userDownloadId +
                ", downloadDate=" + downloadDate +
                ", article=" + article +
                ", user=" + user +
                '}';
    }
}

