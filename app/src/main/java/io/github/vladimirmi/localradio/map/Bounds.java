package io.github.vladimirmi.localradio.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Vladimir Mikhalev 20.07.2018.
 */
public class Bounds {

    public final double top;
    public final double right;
    public final double bottom;
    public final double left;

    public double height;
    public double width;

    public Bounds(LatLngBounds bounds) {
        this(
                bounds.northeast.latitude,
                bounds.northeast.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude
        );
    }

    public Bounds(double t, double r, double b, double l) {
        top = MapUtils.round(t);
        right = MapUtils.round(r);
        bottom = MapUtils.round(b);
        left = MapUtils.round(l);

        height = top - bottom;
        width = right > left ? right - left : 360 + right - left;
    }

    public Bounds multiplyBy(double mul) {
        return new Bounds(
                min(90, top + height * mul),
                (540 + right + width * mul) % 360 - 180,
                max(-90, bottom - height * mul),
                (540 + left - width * mul) % 360 - 180
        );
    }

    public List<Bounds> except(Bounds bounds) {
        List<Bounds> list = new ArrayList<>(4);

        if (top > bounds.top) {
            list.add(new Bounds(
                    top,
                    max(right, bounds.right),
                    bounds.top,
                    min(left, bounds.left)
            ));
        }
        if (right > bounds.right) {
            list.add(new Bounds(
                    min(top, bounds.top),
                    right,
                    max(bottom, bounds.bottom),
                    bounds.right
            ));
        }
        if (bottom < bounds.bottom) {
            list.add(new Bounds(
                    bounds.bottom,
                    max(right, bounds.right),
                    bottom,
                    max(left, bounds.left)
            ));
        }
        if (left < bounds.left) {
            list.add(new Bounds(
                    min(top, bounds.top),
                    bounds.left,
                    max(bottom, bounds.bottom),
                    left
            ));
        }
        return list;
    }

    public boolean contains(LatLng point) {
        return point.latitude <= top && point.latitude >= bottom
                && (right > left ? point.longitude >= left && point.longitude <= right
                : point.longitude >= left || point.longitude <= right);
    }

    @Override
    public String toString() {
        return "Bounds{" +
                "t=" + top +
                ", r=" + right +
                ", b=" + bottom +
                ", l=" + left +
                ", h=" + height +
                ", w=" + width +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bounds bounds = (Bounds) o;

        if (Double.compare(bounds.top, top) != 0) return false;
        if (Double.compare(bounds.right, right) != 0) return false;
        if (Double.compare(bounds.bottom, bottom) != 0) return false;
        return Double.compare(bounds.left, left) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(top);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(right);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(bottom);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(left);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
