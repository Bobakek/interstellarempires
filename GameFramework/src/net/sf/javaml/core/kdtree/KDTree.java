package net.sf.javaml.core.kdtree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Minimal KD-Tree implementation to provide the small API used by Tree2D.
 * This is not a full replacement of Java-ML KDTree, but implements:
 *  - KDTree(int dimensions)
 *  - insert(double[] key, Object value)
 *  - delete(double[] key)
 *  - search(double[] key) -> Object
 *  - range(double[] lower, double[] upper) -> Object[]
 *  - nearest(double[] key) -> Object
 *  - nearest(double[] key, int count) -> Object[]
 */
public class KDTree {

    private static class Node {
        double[] point;
        Object value;
        Node left, right;

        Node(double[] p, Object v) {
            this.point = p;
            this.value = v;
        }
    }

    private final int k;
    private Node root;

    public KDTree(int dimensions) {
        this.k = Math.max(1, dimensions);
        this.root = null;
    }

    public void insert(double[] key, Object value) {
        if (key == null) return;
        root = insertRec(root, key, value, 0);
    }

    private Node insertRec(Node node, double[] key, Object value, int depth) {
        if (node == null) {
            return new Node(key.clone(), value);
        }
        int axis = depth % k;
        if (key[axis] < node.point[axis]) {
            node.left = insertRec(node.left, key, value, depth + 1);
        } else {
            node.right = insertRec(node.right, key, value, depth + 1);
        }
        return node;
    }

    /**
     * Lazy delete: find node with exact same coordinates and set value=null.
     */
    public void delete(double[] key) {
        Node n = findNode(root, key, 0);
        if (n != null) n.value = null;
    }

    private Node findNode(Node node, double[] key, int depth) {
        if (node == null || key == null) return null;
        if (equalsPoint(node.point, key)) return node;
        int axis = depth % k;
        if (key[axis] < node.point[axis]) return findNode(node.left, key, depth + 1);
        return findNode(node.right, key, depth + 1);
    }

    public Object search(double[] key) {
        Node n = findNode(root, key, 0);
        return n == null ? null : n.value;
    }

    public Object[] range(double[] lower, double[] upper) {
        List<Object> out = new ArrayList<>();
        rangeRec(root, lower, upper, 0, out);
        return out.toArray(new Object[0]);
    }

    private void rangeRec(Node node, double[] lower, double[] upper, int depth, List<Object> out) {
        if (node == null) return;
        if (node.value != null && inBounds(node.point, lower, upper)) out.add(node.value);
        int axis = depth % k;
        if (lower[axis] <= node.point[axis]) rangeRec(node.left, lower, upper, depth + 1, out);
        if (upper[axis] >= node.point[axis]) rangeRec(node.right, lower, upper, depth + 1, out);
    }

    public Object nearest(double[] key) {
        Object[] res = nearest(key, 1);
        return (res != null && res.length > 0) ? res[0] : null;
    }

    public Object[] nearest(double[] key, int count) {
        if (key == null || count <= 0) return new Object[0];
        PriorityQueue<Result> pq = new PriorityQueue<>(Comparator.comparingDouble(r -> -r.dist));
        nearestRec(root, key, 0, count, pq);
        List<Object> values = new ArrayList<>();
        while (!pq.isEmpty()) values.add(0, pq.poll().value); // reverse order
        return values.toArray(new Object[0]);
    }

    private static class Result {
        double dist;
        Object value;
        Result(double d, Object v) { dist = d; value = v; }
    }

    private void nearestRec(Node node, double[] key, int depth, int kCount, PriorityQueue<Result> pq) {
        if (node == null) return;
        if (node.value != null) {
            double d = distSquared(node.point, key);
            if (pq.size() < kCount) pq.offer(new Result(d, node.value));
            else if (d < pq.peek().dist) {
                pq.poll();
                pq.offer(new Result(d, node.value));
            }
        }
        int axis = depth % this.k;
        Node nearer = (key[axis] < node.point[axis]) ? node.left : node.right;
        Node farther = (nearer == node.left) ? node.right : node.left;
        nearestRec(nearer, key, depth + 1, kCount, pq);
        // check if we need to search the farther side
        double diff = key[axis] - node.point[axis];
        double bestDist = pq.isEmpty() ? Double.POSITIVE_INFINITY : pq.peek().dist;
        if (pq.size() < kCount || diff * diff < bestDist) {
            nearestRec(farther, key, depth + 1, kCount, pq);
        }
    }

    private static double distSquared(double[] a, double[] b) {
        double s = 0.0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            double d = a[i] - b[i];
            s += d * d;
        }
        return s;
    }

    private static boolean inBounds(double[] p, double[] lower, double[] upper) {
        for (int i = 0; i < Math.min(p.length, lower.length); i++) {
            if (p[i] < lower[i] || p[i] > upper[i]) return false;
        }
        return true;
    }

    private static boolean equalsPoint(double[] a, double[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (Double.compare(a[i], b[i]) != 0) return false;
        return true;
    }
}
