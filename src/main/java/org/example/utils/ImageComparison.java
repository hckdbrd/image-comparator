package org.example.utils;

import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class ImageComparison {
    @SneakyThrows
    public static void getImageDifference(File img1, File img2, int pixDistance) {
        ImageIO.write(
                compareImages(
                        ImageIO.read(img1),
                        ImageIO.read(img2),
                        pixDistance),
                "png",
                new File("src/main/resources/difference.png"));
    }
    public static BufferedImage compareImages(BufferedImage img1, BufferedImage img2, int pixDistance) {

        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException("Error: Images' sizes must be equal for comparison!");
        }

        final int w = img1.getWidth();
        final int h = img1.getHeight();

        final int[] pixArr1 = img1.getRGB(0, 0, w, h, null, 0, w);
        final int[] pixArr2 = img2.getRGB(0, 0, w, h, null, 0, w);

        HashMap<Point, Integer> points = getAllPoints(pixArr1, w);
        HashMap<Point, Integer> diffPoints = getDiffPoints(pixArr1, pixArr2, w);

        List<Point> coordinates = new LinkedList<>(diffPoints.keySet());

        List<Group> groups = getGroups(pixDistance, coordinates);
        List<Corners> groupsCorners = getGroupsCorners(groups);
        List<Borders> groupsBorders = getGroupsBorders(groupsCorners);
        drawRectangles(pixArr2, points, groupsBorders);

        return getMarkedImage(w, h, pixArr2);
    }
    private static BufferedImage getMarkedImage(int w, int h, int[] pixArr2) {
        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        out.setRGB(0, 0, w, h, pixArr2, 0, w);
        return out;
    }
    private static void drawRectangles(int[] pixArr2, HashMap<Point, Integer> points, List<Borders> groupsBorders) {
        final int HIGHLIGHT = Color.RED.getRGB();
        for (Borders borders : groupsBorders) {
            for (Point borderPoint : borders) {
                int index = points.get(borderPoint);
                pixArr2[index] = HIGHLIGHT;
            }
        }
    }
    private static List<Borders> getGroupsBorders(List<Corners> groupsCorners) {
        List<Borders> groupBorders = new LinkedList<>();
        for (Corners corners : groupsCorners) {
            Borders borders = getBorders(corners);
            groupBorders.add(borders);
        }
        return groupBorders;
    }
    private static Borders getBorders(Corners corners) {
        Borders borders = new Borders();

        int rectWidth = corners.getBottomRight().getX() - corners.getTopLeft().getX() + 1;
        int rectHeight = corners.getBottomRight().getY() - corners.getTopLeft().getY() + 1;

        int x1 = corners.getTopLeft().getX();
        int y1 = corners.getTopLeft().getY();
        int x2 = corners.getBottomRight().getX();
        int y2 = corners.getBottomRight().getY();

        // Add all width points;
        for (int i = 0; i < rectWidth; i++) {
            borders.add(new Point(x1 + i, y1)); // all points by upper width bound;
            borders.add(new Point(x2 - i, y2)); // all points by bottom width bound;
        }
        // Add all height points;
        for (int i = 0; i < rectHeight; i++) {
            borders.add(new Point(x1, y1 + i)); // all points by left height bound;
            borders.add(new Point(x2, y2 - i)); // all points by right height bound;
        }

        return borders;
    }
    private static List<Corners> getGroupsCorners(List<Group> groups) {
        List<Corners> groupsCorners = new LinkedList<>();
        for (Group group : groups) {
            Corners corners = getCorners(group);
            groupsCorners.add(corners);
        }
        return groupsCorners;
    }
    private static Corners getCorners(Group group) {
        Point topLeft = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Point bottomRight = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (Point point : group) {
            topLeft.setX(Math.min(point.getX(), topLeft.getX()));
            topLeft.setY(Math.min(point.getY(), topLeft.getY()));
            bottomRight.setX(Math.max(point.getX(), bottomRight.getX()));
            bottomRight.setY(Math.max(point.getY(), bottomRight.getY()));
        }
        return new Corners(topLeft, bottomRight);
    }
    private static List<Group> getGroups(int pixDistance, List<Point> list) {
        List<Group> groups = new ArrayList<>();
        while (!list.isEmpty()) {
            Group group = createGroup(list);
            expandGroup(pixDistance, list, group);
            groups.add(group);
        }
        return groups;
    }
    private static Group createGroup(List<Point> list) {
        Group group = new Group();
        group.add(list.get(0));
        list.remove(0);
        return group;
    }
    private static void expandGroup(int pixDistance, List<Point> list, Group group) {
        int pastSize = group.size();
        for (int i = 0; i < list.size(); i++) {
            Point comparable = list.get(i);
            //Check for distance;
            for (Point p : group) {
                double distance = getDistance(p, comparable);
                if (distance <= pixDistance) {
                    group.add(comparable);
                    list.remove(i);
                    break;
                }
            }
        }
        if(!(pastSize == group.size())) expandGroup(pixDistance, list, group);
    }
    private static double getDistance(Point startPoint, Point endPoint) {
        return Math.sqrt(Math.pow(startPoint.getX() - endPoint.getX(), 2)
                + Math.pow(startPoint.getY() - endPoint.getY(), 2));
    }
    private static HashMap<Point, Integer> getAllPoints(int[] img, int width) {

        HashMap<Point, Integer> points = new HashMap<>();

        for (int index = 0; index < img.length; index++) {
            int x = calculateX(index, width);
            int y = calculateY(index, width);
            Point point = new Point(x, y);
            points.put(point, index);
        }

        return points;
    }
    private static HashMap<Point, Integer> getDiffPoints(int[] img1, int[] img2, int width) {

        HashMap<Point, Integer> points = new HashMap<>();

        for (int index = 0; index < img1.length; index++) {
            if (img1[index] != img2[index]) {
                int x = calculateX(index, width);
                int y = calculateY(index, width);
                Point point = new Point(x, y);
                points.put(point, index);
            }
        }

        return points;
    }
    private static int calculateX(int i, int shift) {
        return i % shift;
    }
    private static int calculateY(int i, int shift) {
        return (int) Math.ceil((double) i / shift);
    }
}
