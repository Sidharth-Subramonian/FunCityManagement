package com.funcity.ui.dashboard;

import com.funcity.model.DashboardMetrics;
import com.funcity.service.ReportService;
import com.funcity.ui.components.StatCard;
import com.funcity.ui.components.UIHelper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final ReportService reportService;

    private StatCard custCard;
    private StatCard cardCard;
    private StatCard gameCard;
    private StatCard machineCard;
    private StatCard revCard;
    private StatCard sessionCard;

    private JPanel chartsGridPanel;

    public DashboardPanel() {
        this.reportService = new ReportService();
        initUI();
        loadDataAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(UIHelper.BG_LIGHT);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("📊 Arcade Analytics & Performance Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UIHelper.TEXT_DARK);

        JButton refreshBtn = new JButton("🔄 Refresh Data");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadDataAsync());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // Stat cards panel
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 12, 12));
        cardsPanel.setOpaque(false);

        custCard = new StatCard("Total Customers", "-", "Registered accounts", UIHelper.PRIMARY_COLOR);
        cardCard = new StatCard("Active Cards", "-", "Simulated RFID cards", UIHelper.SUCCESS_COLOR);
        gameCard = new StatCard("Active Games", "-", "Game titles catalog", UIHelper.PRIMARY_COLOR);
        machineCard = new StatCard("Operational Machines", "-", "Physical arcade units", UIHelper.SUCCESS_COLOR);
        revCard = new StatCard("Today's Revenue", "$0.00", "Wallet cash recharges", UIHelper.WARNING_COLOR);
        sessionCard = new StatCard("Today's Play Sessions", "-", "Arcade gameplay runs", UIHelper.PRIMARY_COLOR);

        cardsPanel.add(custCard);
        cardsPanel.add(cardCard);
        cardsPanel.add(gameCard);
        cardsPanel.add(machineCard);
        cardsPanel.add(revCard);
        cardsPanel.add(sessionCard);

        // Charts container
        chartsGridPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        chartsGridPanel.setOpaque(false);

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(cardsPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(new JScrollPane(chartsGridPanel), BorderLayout.CENTER);
    }

    public void loadDataAsync() {
        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() {
                DashboardMetrics metrics = reportService.getMetrics();
                Map<String, BigDecimal> dailyRev = reportService.getDailyRevenue(7);
                Map<String, Integer> popularGames = reportService.getPopularGames();
                Map<String, Integer> machineStatus = reportService.getMachineStatusDistribution();
                Map<String, Integer> prizeStats = reportService.getPrizeRedemptionDistribution();
                return new DashboardData(metrics, dailyRev, popularGames, machineStatus, prizeStats);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateUIWithData(data);
                } catch (Exception ex) {
                    // Fail silently or show error in log
                }
            }
        };
        worker.execute();
    }

    private void updateUIWithData(DashboardData data) {
        DashboardMetrics m = data.metrics;
        custCard.setValue(String.valueOf(m.getTotalCustomers()));
        cardCard.setValue(String.valueOf(m.getTotalActiveCards()));
        gameCard.setValue(String.valueOf(m.getTotalGames()));
        machineCard.setValue(m.getOperationalMachines() + " / " + m.getTotalMachines());
        machineCard.setSubText(m.getMachinesUnderMaintenance() + " under maintenance");
        revCard.setValue(UIHelper.formatCurrency(m.getTodayRevenue()));
        sessionCard.setValue(String.valueOf(m.getTotalPlaySessionsToday()));
        sessionCard.setSubText(m.getTicketsIssuedToday() + " tickets won today");

        // Rebuild charts
        chartsGridPanel.removeAll();

        // 1. Daily Revenue Chart
        DefaultCategoryDataset revDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, BigDecimal> e : data.dailyRevenue.entrySet()) {
            revDataset.addValue(e.getValue().doubleValue(), "Revenue ($)", e.getKey());
        }
        JFreeChart revChart = ChartFactory.createBarChart(
                "Daily Revenue (Last 7 Days)", "Date", "USD ($)",
                revDataset, PlotOrientation.VERTICAL, false, true, false
        );
        chartsGridPanel.add(new ChartPanel(revChart));

        // 2. Most Popular Games Chart
        DefaultCategoryDataset gameDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> e : data.popularGames.entrySet()) {
            gameDataset.addValue(e.getValue(), "Plays", e.getKey());
        }
        JFreeChart gameChart = ChartFactory.createBarChart(
                "Most Popular Games", "Game", "Play Count",
                gameDataset, PlotOrientation.HORIZONTAL, false, true, false
        );
        chartsGridPanel.add(new ChartPanel(gameChart));

        // 3. Machine Status Distribution (Pie)
        DefaultPieDataset<String> statusDataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> e : data.machineStatus.entrySet()) {
            statusDataset.setValue(e.getKey(), e.getValue());
        }
        JFreeChart statusChart = ChartFactory.createPieChart(
                "Machine Operational Status", statusDataset, true, true, false
        );
        chartsGridPanel.add(new ChartPanel(statusChart));

        // 4. Prize Redemption Statistics
        DefaultCategoryDataset prizeDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> e : data.prizeStats.entrySet()) {
            prizeDataset.addValue(e.getValue(), "Units Redeemed", e.getKey());
        }
        JFreeChart prizeChart = ChartFactory.createBarChart(
                "Top Redeemed Prizes", "Prize", "Quantity",
                prizeDataset, PlotOrientation.VERTICAL, false, true, false
        );
        chartsGridPanel.add(new ChartPanel(prizeChart));

        chartsGridPanel.revalidate();
        chartsGridPanel.repaint();
    }

    private static class DashboardData {
        final DashboardMetrics metrics;
        final Map<String, BigDecimal> dailyRevenue;
        final Map<String, Integer> popularGames;
        final Map<String, Integer> machineStatus;
        final Map<String, Integer> prizeStats;

        DashboardData(DashboardMetrics metrics, Map<String, BigDecimal> dailyRevenue,
                      Map<String, Integer> popularGames, Map<String, Integer> machineStatus,
                      Map<String, Integer> prizeStats) {
            this.metrics = metrics;
            this.dailyRevenue = dailyRevenue;
            this.popularGames = popularGames;
            this.machineStatus = machineStatus;
            this.prizeStats = prizeStats;
        }
    }
}

