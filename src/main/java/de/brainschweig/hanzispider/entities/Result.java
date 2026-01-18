package de.brainschweig.hanzispider.entities;

import javax.persistence.*;

@Entity
@Table(name = "results")

public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "hanzi", length = 2048)
    private String hanzi;

    @Column(name = "redundant")
    private String redundant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHanzi(String hanzi){
        this.hanzi = hanzi;
     }

    public String getHanzi() {
        return hanzi;
    }

    public String getRedundant() {
        return redundant;
    }

    public void setRedundant(String redundant) {
        this.redundant = redundant;
    }
}

