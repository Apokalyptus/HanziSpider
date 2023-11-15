package de.brainschweig.hanzispider.entities;

import javax.persistence.*;

/*
 * CREATE TABLE `status` (
  `idstatus` int(11) NOT NULL AUTO_INCREMENT,
  `url_idurl` int(11) NOT NULL,
  `status` varchar(45) NOT NULL,
  `mtimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mtime` datetime NOT NULL,
  PRIMARY KEY (`idstatus`),
  KEY `fk_status_url_idx` (`url_idurl`),
  CONSTRAINT `fk_status_url` FOREIGN KEY (`url_idurl`) REFERENCES `url` (`idurl`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=659869 DEFAULT CHARSET=utf8;

 */

@Entity
@Table(name = "status")

public class Status {
    @Id
    @Column(name = "idstatus")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int idstatus;

    @Column(name = "url_idurl")
    private int urlId;

    @Column(name = "status")
    private String status;

    @Column(name = "mtimestamp")
    private java.sql.Timestamp mtimestamp;

    public int getIdStatus() {
        return this.idstatus;
    }

    public void setIdStatus(int idstatus) {
        this.idstatus = idstatus;
    }

    public int getUrlId() {
        return this.urlId;
    }

    public void setUrlId(int urlId) {
        this.urlId = urlId;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.sql.Timestamp getMTimeStamp() {
        return this.mtimestamp;
    }

    public void setMTimeStamp(java.sql.Timestamp mtimestamp) {
        this.mtimestamp = mtimestamp;
    }

}
