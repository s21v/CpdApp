package com.cpd.yuqing.util;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 将不规则矩形拆分成规则矩形
 * Created by s21v on 2018/4/10.
 */
public class SplitRect {

    public static ArrayList<Rect> Split2Rect(LinkedList<Point> points) {
        ArrayList<Rect> rects = new ArrayList<>();
        ArrayList<Point> pointsInRect = new ArrayList<>();  // 存放常规矩形的潜在顶点（预备顶点）
        ArrayList<Integer> edgeInRect = new ArrayList<>();  // 存放常规矩形的边长
        while (!points.isEmpty()) {
            //  将点加入到临时列表中
            pointsInRect.add(points.poll());
            if (pointsInRect.size() > 1) {
                // 当预备顶点中的顶点大于两个点后，可以计算边长
                int lastPointIndexInRect = pointsInRect.size() - 1;
                edgeInRect.add(getDistance(pointsInRect.get(lastPointIndexInRect - 1),
                        pointsInRect.get(lastPointIndexInRect)));
                // 当边的数量大于等于3，就可以计算矩形的走势（内收型，外扩型）
                int lastEdgeIndexInRect = edgeInRect.size() - 1;
                if (edgeInRect.size() >= 3) {
                    // 获取要比较的两条平行的边
                    int edge1 = edgeInRect.get(lastEdgeIndexInRect - 2);    //  要比较的第一条边
                    int edge2 = edgeInRect.get(lastEdgeIndexInRect);        //  要比较的第二条边
                    if (isInward(edge1, edge2)) {     //  内收型，可以分割出常规矩形，或直接获得常规矩形
                        Point first = pointsInRect.get(lastEdgeIndexInRect - 2);    //  第一条边的起点
                        Point end = pointsInRect.get(lastEdgeIndexInRect + 1);      //  第二条边的终点
                        if (Math.abs(edge1) == Math.abs(edge2)) {     // 如果两条边的绝对值相等即认为找到一个常规的矩形
                            // 根据定点确定矩形
                            Rect rect = getRect(pointsInRect);
                            rects.add(rect);
                            // 清除 pointsInRect、 edgeInRect中的数据，以便继续使用
                            pointsInRect.clear();
                            edgeInRect.clear();
                        }
                        //  两条边的绝对值不想等,那么可以在较长的边截取一个新的点（点的平行向坐标值和较短的点的值一致），这个点和剩下的点可以组成一个常规的矩形
                        if (Math.abs(edge1) < Math.abs(edge2)) {    //  第一条边较短
                            Point newPoint;
                            //  按照数据点的出现规律：横、纵、横、纵..... ，所以所在位置为偶数的边为横向边（边的两个定点的y值相同，x不同）
                            if (lastEdgeIndexInRect % 2 == 0) {  // 横向边
                                newPoint = new Point(first.x, end.y);
                            } else {    // 纵向边
                                newPoint = new Point(end.x, first.y);
                            }
                            //  从 预备顶点 中删去无用的点
                            pointsInRect.remove(lastEdgeIndexInRect + 1);
                            //  添加新截取的点到 预备顶点
                            pointsInRect.add(newPoint);
                            //  添加新截取的点到原始点链表队列中去
                            points.add(newPoint);
                            //  并将删去的点添加到原始点链表队列中去(这里两个点的顺序不能乱，也要按顺时针方向添加)
                            points.add(end);
                            //  得到截取到的新矩形
                            Rect rect = getRect(pointsInRect);
                            rects.add(rect);
                            //  清除 pointsInRect、 edgeInRect中的数据，以便继续使用
                            pointsInRect.clear();
                            edgeInRect.clear();
                        } else if (Math.abs(edge1) > Math.abs(edge2)) { // 第二条边较短
                            Point newPoint;
                            if (lastEdgeIndexInRect % 2 == 0) {  // 横向边
                                newPoint = new Point(end.x, first.y);
                            } else {    // 纵向边
                                newPoint = new Point(first.x, end.y);
                            }
                            //  从 预备顶点 中删去无用的点
                            pointsInRect.remove(lastEdgeIndexInRect + 1);
                            // 添加新截取的点到 预备顶点
                            pointsInRect.add(newPoint);
                            // 添加新截取的点到原始点链表队列中去 （注意：此时不用重新添加被删去的顶点，因为新截取的点和被删除的点以及后面的点，3点在一条边上）
                            points.add(newPoint);
                            //  得到截取到的新矩形
                            Rect rect = getRect(pointsInRect);
                            rects.add(rect);
                            //  清除 pointsInRect、 edgeInRect中的数据，以便继续使用
                            pointsInRect.clear();
                            edgeInRect.clear();
                        }
                    } else {    //外扩型图形
                        //  从 预备顶点 中删除无用的点，并将其放回到原始点链表队列中以备后面使用
                        Point backup = pointsInRect.remove(0);
                        points.add(backup);
                        // 删除此顶点相关的边长信息
                        edgeInRect.remove(0);
                    }
                }
            }
        }
        return rects;
    }

    //  根据四个点确定矩形
    private static Rect getRect(ArrayList<Point> pointsInRect) throws IllegalArgumentException {
        if (pointsInRect.size() != 4)
            throw new IllegalArgumentException("坐标点数目异常，pointsInRectSize：" + pointsInRect.size());
        else {
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE,
                    minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            for (Point p : pointsInRect) {
                if (p.x > maxX)
                    maxX = p.x;
                else if (p.x < minX)
                    minX = p.x;
                if (p.y > maxY)
                    maxY = p.y;
                else if (p.y < minY)
                    minY = p.y;
            }
            return new Rect(minX, minY, maxX, maxY);
        }
    }

    //  获得相邻两个点的距离
    private static int getDistance(Point from, Point to) throws IllegalArgumentException {
        if (from.x == to.x)
            return to.y - from.y;
        else if (from.y == to.y)
            return to.x - from.x;
        else
            throw new IllegalArgumentException("无法测出距离，不合理的点：from:" + from + "， to:" + to);
    }

    //  通过两个整数的正负符号是否不同，来判断图形的走势。
    //  正负符号不同测证明是内收型图形，可以分割出一个规则矩形
    //  正负符号相同证明是外扩型图形，此时还不能够分割出一个规则矩形
    private static boolean isInward(int i1, int i2) {
        return (i1 > 0 && i2 < 0) || (i1 < 0 && i2 > 0);
    }
}
