package com.example.canteen.orders;

public class DashboardStats {

    private long totalItems;
    private long totalOrders;
    private long pendingOrders;
    private double totalRevenue;

    public DashboardStats(long totalItems, long totalOrders,
                          long pendingOrders, double totalRevenue) {
        this.totalItems = totalItems;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}