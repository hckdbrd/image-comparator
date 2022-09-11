package org.example;

import java.io.File;
import static org.example.utils.ImageComparison.*;

public class Main {
    public static void main(String[] args) {
        File img1 = new File("src/main/resources/test1.jpg");
        File img2 = new File("src/main/resources/test2.jpg");
        getImageDifference(img1, img2, 50);
    }
}