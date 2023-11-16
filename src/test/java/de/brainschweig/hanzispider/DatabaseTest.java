package de.brainschweig.hanzispider;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatabaseTest {

    Database database;

    @BeforeEach                                         
    void setUp() {
        database = new Database("jdbc:mysql://localhost:3306/crawler?user=crawler&password=crawlerX");
        
    }

    @Test                                               
    @DisplayName("Insert Crawl Result")   
    void testInsertResult() {
        database.insertResult("Ni Hao!");  
    }

    @Test                             
    @DisplayName("Insert a list of hyperlinks to Database")
    void testStoreHyperLinks() {
        Set<String> stringSet = new HashSet<String>();
        stringSet.add("Hallo eins");
        stringSet.add("Hallo zwei");
        stringSet.add("Hallo drei");
        stringSet.add("Hallo vier");
        database.storeHyperLinks(stringSet);

        
    }
}