package com.funcity.model;

import java.math.BigDecimal;

public class DashboardMetrics {
    private int totalCustomers;
    private int totalActiveCards;
    private int totalGames;
    private int totalMachines;
    private int operationalMachines;
    private int machinesUnderMaintenance;
    private BigDecimal todayRevenue = BigDecimal.ZERO;
    private int totalPlaySessionsToday;
    private int ticketsIssuedToday;
    private int prizeRedemptionsToday;

    public DashboardMetrics() {}

    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

    public int getTotalActiveCards() { return totalActiveCards; }
    public void setTotalActiveCards(int totalActiveCards) { this.totalActiveCards = totalActiveCards; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    public int getTotalMachines() { return totalMachines; }
    public void setTotalMachines(int totalMachines) { this.totalMachines = totalMachines; }

    public int getOperationalMachines() { return operationalMachines; }
    public void setOperationalMachines(int operationalMachines) { this.operationalMachines = operationalMachines; }

    public int getMachinesUnderMaintenance() { return machinesUnderMaintenance; }
    public void setMachinesUnderMaintenance(int machinesUnderMaintenance) { this.machinesUnderMaintenance = machinesUnderMaintenance; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO; }

    public int getTotalPlaySessionsToday() { return totalPlaySessionsToday; }
    public void setTotalPlaySessionsToday(int totalPlaySessionsToday) { this.totalPlaySessionsToday = totalPlaySessionsToday; }

    public int getTicketsIssuedToday() { return ticketsIssuedToday; }
    public void setTicketsIssuedToday(int ticketsIssuedToday) { this.ticketsIssuedToday = ticketsIssuedToday; }

    public int getPrizeRedemptionsToday() { return prizeRedemptionsToday; }
    public void setPrizeRedemptionsToday(int prizeRedemptionsToday) { this.prizeRedemptionsToday = prizeRedemptionsToday; }
}

