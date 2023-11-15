package de.brainschweig.hanzispider.entities;

import javax.persistence.*;

/*
  CREATE TABLE `url` (
    `idurl` int(11) NOT NULL AUTO_INCREMENT,
    `url` text NOT NULL,
    `md5sum` varchar(45) NOT NULL,
    `mtimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `mtime` datetime NOT NULL,
    PRIMARY KEY (`idurl`),
    UNIQUE KEY `md5sum_UNIQUE` (`md5sum`)
  ) ENGINE=InnoDB AUTO_INCREMENT=5171133 DEFAULT CHARSET=utf8;
 */

@Entity
@Table(name = "url")

public class Url {
    @Id
    @Column(name = "idurl")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int idurl;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "md5sum", length = 45, unique=true)
    private String md5sum;

    @Column(name = "mtimestamp")
    private java.sql.Timestamp mtimestamp;

    public int getIdUrl() {
        return this.idurl;
    }

    public void setIdStatus(int idurl) {
        this.idurl = idurl;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5Sum() {
        return this.md5sum;
    }

    public void setMd5Sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public java.sql.Timestamp getMTimeStamp() {
        return this.mtimestamp;
    }

    public void setMTimeStamp(java.sql.Timestamp mtimestamp) {
        this.mtimestamp = mtimestamp;
    }

}
