package de.brainschweig.hanzispider.entities;

import javax.persistence.*;

@Entity
@Table(name = "results")

public class Result {
    @Id
    @Column(name = "hanzi",length = 2048, unique=true)
    private String hanzi;

    @Column(name = "redundant")
    private String redundant;

    public void setHanzi(String hanzi){
        this.hanzi = hanzi;
     }

}
