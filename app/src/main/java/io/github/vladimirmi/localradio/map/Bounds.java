package io.github.vladimirmi.localradio.map;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vladimir Mikhalev 20.07.2018.
 */
public class Bounds {

    public final double top;
    public final double right;
    public final double bottom;
    public final double left;

    public final double height;
    public final double width;

    public Bounds(double top, double right, double bottom, double left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        height = Math.abs(top - bottom);
        width = Math.abs(right - left);
    }

    public Bounds multiplyBy(double mul) {
        return new Bounds(
                Math.max(0, top - height * mul),
                (right + width * mul) % 360,
                Math.min(180, bottom + height * mul),
                (360 + left - width * mul) % 360
        );
    }

    public List<Bounds> except(Bounds bounds) {
        if (width != bounds.width || height != bounds.height) {
            throw new IllegalArgumentException("Bounds sizes must be equal");
        }
        List<Bounds> list = new ArrayList<>(2);

        if (top != bounds.top) {
            list.add(new Bounds(
                    top > bounds.top ? bounds.bottom : top,
                    right,
                    bottom > bounds.bottom ? bottom : bounds.top,
                    left
            ));
        }
        if (right != bounds.right) {
            list.add(new Bounds(
                    top > bounds.top ? top : bounds.top,
                    right > bounds.right ? right : bounds.left,
                    bottom > bounds.bottom ? bounds.bottom : bottom,
                    left > bounds.left ? bounds.right : left
            ));

        }
        return list;
    }

    public boolean contains(Point point) {
        return point.y >= top && point.y <= bottom
                && point.x >= left && point.x <= right;
    }
}
