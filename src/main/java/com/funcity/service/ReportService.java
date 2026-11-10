package com.funcity.service;

import com.funcity.dao.ReportDAO;
import com.funcity.model.DashboardMetrics;

import java.math.BigDecimal;
import java.util.Map;

public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public DashboardMetrics getMetrics() {
        return reportDAO.getDashboardMetrics();
    }

    public Map<String, BigDecimal> getDailyRevenue(int days) {
        return reportDAO.getDailyRevenue(days);
    }

    public Map<String, Integer> getPopularGames() {
        return reportDAO.getMostPopularGames();
    }

    public Map<String, Integer> getMachineStatusDistribution() {
        return reportDAO.getMachineStatusDistribution();
    }

    public Map<String, Integer> getPrizeRedemptionDistribution() {
        return reportDAO.getPrizeRedemptionDistribution();
    }
}

