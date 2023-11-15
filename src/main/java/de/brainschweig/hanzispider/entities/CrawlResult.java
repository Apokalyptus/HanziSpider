package de.brainschweig.hanzispider.entities;

import javax.persistence.*;

@Entity
@Table(name = "results")

public class CrawlResult {
    @Id
    @Column(name = "hanzi",length = 2048)
    private String hanzi;

    public void setHanzi(String hanzi){
        this.hanzi = hanzi;
     }

}
